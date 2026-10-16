package com.talentmarketplace.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.talentmarketplace.dto.request.CreateAllocationRequest;
import com.talentmarketplace.dto.request.RejectAllocationRequest;
import com.talentmarketplace.dto.response.AllocationResponse;
import com.talentmarketplace.dto.response.AvailabilityAssessmentResponse;
import com.talentmarketplace.entity.AllocationStatus;
import com.talentmarketplace.entity.DemandSkill;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeSkill;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.ResourceAllocation;
import com.talentmarketplace.exception.BusinessRuleValidationException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;

@Service
@Transactional
public class ResourceAllocationServiceImpl implements ResourceAllocationService {

    private final ResourceAllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectDemandRepository demandRepository;
    private final AvailabilityService availabilityService;
    private final EmployeeSkillRepository employeeSkillRepository;

    public ResourceAllocationServiceImpl(ResourceAllocationRepository allocationRepository,
                                         EmployeeRepository employeeRepository,
                                         ProjectDemandRepository demandRepository,
                                         AvailabilityService availabilityService,
                                         EmployeeSkillRepository employeeSkillRepository) {
        this.allocationRepository = allocationRepository;
        this.employeeRepository = employeeRepository;
        this.demandRepository = demandRepository;
        this.availabilityService = availabilityService;
        this.employeeSkillRepository = employeeSkillRepository;
    }

    @Override
    public AllocationResponse createAllocation(CreateAllocationRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleValidationException("Allocation start date cannot be after end date");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        ProjectDemand demand = demandRepository.findById(request.getDemandId())
                .orElseThrow(() -> new ResourceNotFoundException("ProjectDemand", "id", request.getDemandId()));

        if (demand.getStatus() == DemandStatus.CANCELLED) {
            throw new BusinessRuleValidationException("Cannot allocate resources to a cancelled demand");
        }

        // Prevent duplicate active allocations (PENDING or CONFIRMED) for the same employee and demand
        if (allocationRepository.existsActiveAllocationForEmployeeAndDemand(
                employee.getId(), demand.getId(), List.of(AllocationStatus.PENDING, AllocationStatus.CONFIRMED))) {
            throw new BusinessRuleValidationException(String.format(
                    "An active allocation request (PENDING or CONFIRMED) already exists for employee %s on demand %s",
                    employee.getName(), demand.getDemandCode()
            ));
        }

        // Pre-validate availability
        AvailabilityAssessmentResponse assessment = availabilityService.assessEmployeeAvailability(
                employee, request.getStartDate(), request.getEndDate(), request.getAllocationPercent()
        );

        if (!assessment.isAvailable()) {
            throw new BusinessRuleValidationException(
                    "Employee cannot be allocated: " + assessment.getReason()
            );
        }

        ResourceAllocation allocation = new ResourceAllocation(
                employee,
                demand,
                request.getStartDate(),
                request.getEndDate(),
                request.getAllocationPercent(),
                AllocationStatus.PENDING,
                request.getAllocatedBy() != null ? request.getAllocatedBy() : "System"
        );

        ResourceAllocation saved = allocationRepository.save(allocation);

        if (request.isAutoConfirm()) {
            return confirmAllocation(saved.getId(), request.getAllocatedBy());
        }

        return AllocationResponse.fromEntity(saved);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AllocationResponse confirmAllocation(Long allocationId, String confirmedBy) {
        ResourceAllocation allocation = allocationRepository.findByIdWithDetails(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceAllocation", "id", allocationId));

        if (allocation.getStatus() == AllocationStatus.CONFIRMED) {
            return AllocationResponse.fromEntity(allocation);
        }

        if (allocation.getStatus() == AllocationStatus.CANCELLED || allocation.getStatus() == AllocationStatus.REJECTED) {
            throw new BusinessRuleValidationException(
                    "Cannot confirm an allocation with status " + allocation.getStatus()
            );
        }

        Long employeeId = allocation.getEmployee().getId();

        // Database-level concurrency protection: Acquire pessimistic write lock on the Employee row.
        // This strictly serializes all concurrent allocation confirmations for this employee.
        Employee employee = employeeRepository.findByIdForUpdate(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        // Re-read allocation state inside critical section to guard against concurrent mutations (e.g. concurrent cancellation)
        allocation = allocationRepository.findByIdWithDetails(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceAllocation", "id", allocationId));

        if (allocation.getStatus() == AllocationStatus.CONFIRMED) {
            return AllocationResponse.fromEntity(allocation);
        }

        if (allocation.getStatus() == AllocationStatus.CANCELLED || allocation.getStatus() == AllocationStatus.REJECTED) {
            throw new BusinessRuleValidationException(
                    "Cannot confirm an allocation with status " + allocation.getStatus()
            );
        }

        ProjectDemand demand = allocation.getDemand();
        if (demand.getStatus() == DemandStatus.CANCELLED) {
            throw new BusinessRuleValidationException("Cannot confirm allocation for a cancelled project demand");
        }

        // 1. Concurrency-Safe Eligibility Revalidation inside transaction:
        // Employee must be actively employed
        if (employee.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new BusinessRuleValidationException(
                    "Allocation confirmation failed: Employee is not active (current status: " + employee.getEmploymentStatus() + ")"
            );
        }

        // Must meet all mandatory skill requirements of the demand
        ProjectDemand demandWithSkills = demandRepository.findByIdWithSkills(demand.getId()).orElse(demand);
        List<DemandSkill> requiredSkills = demandWithSkills.getRequiredSkills();
        if (requiredSkills != null && !requiredSkills.isEmpty()) {
            List<EmployeeSkill> empSkills = employeeSkillRepository.findByEmployeeIdWithDetails(employee.getId());
            Map<Long, EmployeeSkill> empSkillMap = empSkills.stream()
                    .filter(es -> es.getSkill() != null)
                    .collect(Collectors.toMap(es -> es.getSkill().getId(), es -> es, (a, b) -> a));

            for (DemandSkill ds : requiredSkills) {
                if (ds.isMandatory()) {
                    EmployeeSkill es = empSkillMap.get(ds.getSkill().getId());
                    if (es == null) {
                        throw new BusinessRuleValidationException(
                                "Allocation confirmation failed: Employee lacks mandatory skill requirement: " + ds.getSkill().getName()
                        );
                    }
                    if (!es.getProficiencyLevel().meetsMinimumRequirement(ds.getMinimumProficiency())) {
                        throw new BusinessRuleValidationException(String.format(
                                "Allocation confirmation failed: Employee proficiency in mandatory skill '%s' (%s) does not meet minimum requirement (%s)",
                                ds.getSkill().getName(), es.getProficiencyLevel(), ds.getMinimumProficiency()
                        ));
                    }
                }
            }
        }

        // 2. Concurrency-Safe Capacity & Availability Revalidation: Recalculate capacity after obtaining the lock,
        // reading all newly committed overlapping confirmed allocations under READ_COMMITTED.
        AvailabilityAssessmentResponse assessment = availabilityService.assessEmployeeAvailabilityExcludingAllocation(
                employee, allocation.getStartDate(), allocation.getEndDate(), allocation.getAllocationPercent(), allocation.getId()
        );

        if (!assessment.isAvailable()) {
            throw new BusinessRuleValidationException(
                    "Allocation confirmation failed: " + assessment.getReason()
            );
        }

        allocation.setStatus(AllocationStatus.CONFIRMED);
        allocation.setConfirmedBy(confirmedBy != null ? confirmedBy : "Manager");
        allocation.setConfirmedAt(LocalDateTime.now());

        ResourceAllocation confirmed = allocationRepository.save(allocation);

        // Check if total confirmed allocations for demand meet the requested percentage
        Integer totalConfirmedPercent = allocationRepository.sumConfirmedAllocationsForDemand(demand.getId());
        if (totalConfirmedPercent != null && totalConfirmedPercent >= demand.getAllocationPercent()) {
            demand.setStatus(DemandStatus.ALLOCATED);
            demandRepository.save(demand);
        } else if (demand.getStatus() == DemandStatus.OPEN) {
            demand.setStatus(DemandStatus.MATCHED);
            demandRepository.save(demand);
        }

        return AllocationResponse.fromEntity(confirmed);
    }

    @Override
    public AllocationResponse cancelAllocation(Long allocationId, String reason) {
        ResourceAllocation allocation = allocationRepository.findByIdWithDetails(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceAllocation", "id", allocationId));

        if (allocation.getStatus() == AllocationStatus.CANCELLED) {
            return AllocationResponse.fromEntity(allocation);
        }

        allocation.setStatus(AllocationStatus.CANCELLED);
        allocation.setRejectionReason(reason != null ? reason : "Allocation cancelled");
        ResourceAllocation saved = allocationRepository.save(allocation);

        // Recheck demand status
        ProjectDemand demand = allocation.getDemand();
        if (demand != null && demand.getStatus() != DemandStatus.CANCELLED) {
            Integer totalConfirmed = allocationRepository.sumConfirmedAllocationsForDemand(demand.getId());
            if (totalConfirmed == null || totalConfirmed == 0) {
                demand.setStatus(DemandStatus.OPEN);
                demandRepository.save(demand);
            } else if (totalConfirmed < demand.getAllocationPercent()) {
                demand.setStatus(DemandStatus.MATCHED);
                demandRepository.save(demand);
            }
        }

        return AllocationResponse.fromEntity(saved);
    }

    @Override
    public AllocationResponse completeAllocation(Long allocationId) {
        ResourceAllocation allocation = allocationRepository.findByIdWithDetails(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceAllocation", "id", allocationId));

        if (allocation.getStatus() == AllocationStatus.COMPLETED) {
            return AllocationResponse.fromEntity(allocation);
        }

        if (allocation.getStatus() != AllocationStatus.CONFIRMED) {
            throw new BusinessRuleValidationException(
                    "Only CONFIRMED allocations can be marked as COMPLETED. Current status: " + allocation.getStatus()
            );
        }

        allocation.setStatus(AllocationStatus.COMPLETED);
        ResourceAllocation saved = allocationRepository.save(allocation);

        // Project allocation history is fully preserved.
        return AllocationResponse.fromEntity(saved);
    }

    @Override
    public AllocationResponse rejectAllocation(Long allocationId, RejectAllocationRequest request) {
        ResourceAllocation allocation = allocationRepository.findByIdWithDetails(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceAllocation", "id", allocationId));

        if (allocation.getStatus() == AllocationStatus.CONFIRMED) {
            throw new BusinessRuleValidationException("Cannot reject an already confirmed allocation. Please cancel it instead.");
        }

        allocation.setStatus(AllocationStatus.REJECTED);
        allocation.setRejectionReason(request.getReason());
        if (request.getRejectedBy() != null) {
            allocation.setConfirmedBy(request.getRejectedBy());
        }

        ResourceAllocation saved = allocationRepository.save(allocation);
        return AllocationResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationResponse getAllocationById(Long allocationId) {
        ResourceAllocation allocation = allocationRepository.findByIdWithDetails(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceAllocation", "id", allocationId));
        return AllocationResponse.fromEntity(allocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsForEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
        return allocationRepository.findByEmployeeIdOrderByStartDateAsc(employeeId).stream()
                .map(AllocationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsForDemand(Long demandId) {
        if (!demandRepository.existsById(demandId)) {
            throw new ResourceNotFoundException("ProjectDemand", "id", demandId);
        }
        return allocationRepository.findByDemandIdOrderByStartDateAsc(demandId).stream()
                .map(AllocationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllAllocations(AllocationStatus status) {
        List<ResourceAllocation> allocations = (status != null)
                ? allocationRepository.findByStatusWithDetails(status)
                : allocationRepository.findAllWithDetails();
        return allocations.stream()
                .map(AllocationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
