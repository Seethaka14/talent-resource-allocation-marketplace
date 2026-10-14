package com.talentmarketplace.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentmarketplace.dto.response.AvailabilityAssessmentResponse;
import com.talentmarketplace.dto.response.UnavailabilityResponse;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeUnavailability;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ResourceAllocation;
import com.talentmarketplace.exception.BusinessRuleValidationException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;

@Service
@Transactional(readOnly = true)
public class AvailabilityServiceImpl implements AvailabilityService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeUnavailabilityRepository unavailabilityRepository;
    private final ResourceAllocationRepository allocationRepository;

    public AvailabilityServiceImpl(EmployeeRepository employeeRepository,
                                   EmployeeUnavailabilityRepository unavailabilityRepository,
                                   ResourceAllocationRepository allocationRepository) {
        this.employeeRepository = employeeRepository;
        this.unavailabilityRepository = unavailabilityRepository;
        this.allocationRepository = allocationRepository;
    }

    @Override
    public AvailabilityAssessmentResponse assessEmployeeAvailability(Long employeeId, LocalDate startDate,
                                                                     LocalDate endDate, Integer requestedAllocationPercent) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        return assessEmployeeAvailability(employee, startDate, endDate, requestedAllocationPercent);
    }

    @Override
    public AvailabilityAssessmentResponse assessEmployeeAvailability(Employee employee, LocalDate startDate,
                                                                     LocalDate endDate, Integer requestedAllocationPercent) {
        return assessEmployeeAvailabilityExcludingAllocation(employee, startDate, endDate, requestedAllocationPercent, null);
    }

    @Override
    public AvailabilityAssessmentResponse assessEmployeeAvailabilityExcludingAllocation(
            Employee employee, LocalDate startDate, LocalDate endDate,
            Integer requestedAllocationPercent, Long excludeAllocationId) {
        if (startDate == null || endDate == null) {
            throw new BusinessRuleValidationException("Start date and end date are required for availability assessment");
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessRuleValidationException("Start date cannot be after end date");
        }

        int requestedPercent = (requestedAllocationPercent != null) ? requestedAllocationPercent : 100;
        if (requestedPercent < 1 || requestedPercent > 100) {
            throw new BusinessRuleValidationException("Requested allocation percent must be between 1 and 100");
        }

        int maxCapacity = (employee.getMaxCapacityPercent() != null) ? employee.getMaxCapacityPercent() : 100;

        // 1. Employment Status Check (Hard gate: must be ACTIVE)
        if (employee.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            return new AvailabilityAssessmentResponse(
                    employee.getId(),
                    employee.getEmployeeCode(),
                    employee.getName(),
                    employee.getEmploymentStatus(),
                    maxCapacity,
                    startDate,
                    endDate,
                    requestedPercent,
                    false,
                    0,
                    List.of(),
                    String.format("Employee is not available (employment status is %s)", employee.getEmploymentStatus())
            );
        }

        // 2. Maximum Configured Capacity Check
        if (requestedPercent > maxCapacity) {
            return new AvailabilityAssessmentResponse(
                    employee.getId(),
                    employee.getEmployeeCode(),
                    employee.getName(),
                    employee.getEmploymentStatus(),
                    maxCapacity,
                    startDate,
                    endDate,
                    requestedPercent,
                    false,
                    maxCapacity,
                    List.of(),
                    String.format("Requested allocation (%d%%) exceeds employee configured maximum capacity limit (%d%%)",
                            requestedPercent, maxCapacity)
            );
        }

        // 3. Unavailability Check (Leaves, Absences, Holidays intersecting [startDate, endDate])
        List<EmployeeUnavailability> overlapping = unavailabilityRepository.findOverlappingUnavailabilities(
                employee.getId(), startDate, endDate
        );

        if (!overlapping.isEmpty()) {
            List<UnavailabilityResponse> unavailResponses = overlapping.stream()
                    .map(UnavailabilityResponse::fromEntity)
                    .collect(Collectors.toList());

            EmployeeUnavailability primaryConflict = overlapping.get(0);
            return new AvailabilityAssessmentResponse(
                    employee.getId(),
                    employee.getEmployeeCode(),
                    employee.getName(),
                    employee.getEmploymentStatus(),
                    maxCapacity,
                    startDate,
                    endDate,
                    requestedPercent,
                    false,
                    0,
                    unavailResponses,
                    String.format("Employee has scheduled unavailability (%s) from %s to %s",
                            primaryConflict.getType(), primaryConflict.getStartDate(), primaryConflict.getEndDate())
            );
        }

        // 4. Remaining Capacity Check with Real Active Confirmed Allocation Records
        int peakAllocated = calculatePeakAllocation(employee.getId(), startDate, endDate, excludeAllocationId);
        int remainingCapacity = Math.max(0, maxCapacity - peakAllocated);

        if (requestedPercent > remainingCapacity) {
            return new AvailabilityAssessmentResponse(
                    employee.getId(),
                    employee.getEmployeeCode(),
                    employee.getName(),
                    employee.getEmploymentStatus(),
                    maxCapacity,
                    startDate,
                    endDate,
                    requestedPercent,
                    false,
                    remainingCapacity,
                    List.of(),
                    String.format("Requested allocation (%d%%) exceeds remaining available capacity (%d%%, currently allocated: %d%%)",
                            requestedPercent, remainingCapacity, peakAllocated)
            );
        }

        // 5. Fully Available
        return new AvailabilityAssessmentResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getName(),
                employee.getEmploymentStatus(),
                maxCapacity,
                startDate,
                endDate,
                requestedPercent,
                true,
                remainingCapacity,
                List.of(),
                String.format("Employee is fully available with %d%% remaining capacity", remainingCapacity)
        );
    }

    private int calculatePeakAllocation(Long employeeId, LocalDate startDate, LocalDate endDate, Long excludeAllocationId) {
        if (allocationRepository == null) {
            return 0;
        }

        List<ResourceAllocation> allocations;
        if (excludeAllocationId != null) {
            allocations = allocationRepository.findOverlappingConfirmedAllocationsExcludingId(
                    employeeId, excludeAllocationId, startDate, endDate
            );
        } else {
            allocations = allocationRepository.findOverlappingConfirmedAllocations(
                    employeeId, startDate, endDate
            );
        }

        if (allocations == null || allocations.isEmpty()) {
            return 0;
        }

        Set<LocalDate> criticalDates = new TreeSet<>();
        criticalDates.add(startDate);
        for (ResourceAllocation a : allocations) {
            if (!a.getStartDate().isBefore(startDate) && !a.getStartDate().isAfter(endDate)) {
                criticalDates.add(a.getStartDate());
            }
            LocalDate dayAfterEnd = a.getEndDate().plusDays(1);
            if (!dayAfterEnd.isBefore(startDate) && !dayAfterEnd.isAfter(endDate)) {
                criticalDates.add(dayAfterEnd);
            }
        }

        int peak = 0;
        for (LocalDate date : criticalDates) {
            int currentOnDate = 0;
            for (ResourceAllocation a : allocations) {
                if (!date.isBefore(a.getStartDate()) && !date.isAfter(a.getEndDate())) {
                    currentOnDate += a.getAllocationPercent();
                }
            }
            if (currentOnDate > peak) {
                peak = currentOnDate;
            }
        }
        return peak;
    }
}
