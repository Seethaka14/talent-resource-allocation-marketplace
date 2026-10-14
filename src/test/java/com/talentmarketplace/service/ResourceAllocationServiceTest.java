package com.talentmarketplace.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.talentmarketplace.dto.request.CreateAllocationRequest;
import com.talentmarketplace.dto.request.RejectAllocationRequest;
import com.talentmarketplace.dto.response.AllocationResponse;
import com.talentmarketplace.dto.response.AvailabilityAssessmentResponse;
import com.talentmarketplace.entity.AllocationStatus;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.ResourceAllocation;
import com.talentmarketplace.exception.BusinessRuleValidationException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;

@ExtendWith(MockitoExtension.class)
class ResourceAllocationServiceTest {

    @Mock
    private ResourceAllocationRepository allocationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProjectDemandRepository demandRepository;

    @Mock
    private AvailabilityService availabilityService;

    @InjectMocks
    private ResourceAllocationServiceImpl allocationService;

    private Employee employee;
    private ProjectDemand demand;

    @BeforeEach
    void setUp() {
        employee = new Employee("EMP-TEST-01", "Alice", "Smith", "alice@example.com",
                "Engineering", "Senior Engineer", "New York", LocalDate.of(2025, 1, 1),
                EmploymentStatus.ACTIVE, 100);
        employee.setId(10L);

        demand = new ProjectDemand("DEM-TEST-01", "Cloud Portal", "Cloud Engineer", "Engineering",
                "New York", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                50, DemandPriority.HIGH, DemandStatus.OPEN, 3.0);
        demand.setId(20L);
    }

    private AvailabilityAssessmentResponse createAvailableResponse(int remainingCapacity) {
        return new AvailabilityAssessmentResponse(
                employee.getId(), employee.getEmployeeCode(), employee.getName(), employee.getEmploymentStatus(),
                employee.getMaxCapacityPercent(), demand.getStartDate(), demand.getEndDate(),
                50, true, remainingCapacity, List.of(), "Fully available"
        );
    }

    private AvailabilityAssessmentResponse createUnavailableResponse(String reason) {
        return new AvailabilityAssessmentResponse(
                employee.getId(), employee.getEmployeeCode(), employee.getName(), employee.getEmploymentStatus(),
                employee.getMaxCapacityPercent(), demand.getStartDate(), demand.getEndDate(),
                50, false, 0, List.of(), reason
        );
    }

    @Test
    @DisplayName("Should successfully create a pending resource allocation when employee is available")
    void testCreateAllocation_success() {
        CreateAllocationRequest request = new CreateAllocationRequest(
                20L, 10L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50, "Lead PM"
        );

        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(demandRepository.findById(20L)).thenReturn(Optional.of(demand));
        when(availabilityService.assessEmployeeAvailability(eq(employee), any(), any(), eq(50)))
                .thenReturn(createAvailableResponse(100));

        ResourceAllocation saved = new ResourceAllocation(
                employee, demand, request.getStartDate(), request.getEndDate(), 50,
                AllocationStatus.PENDING, "Lead PM"
        );
        saved.setId(100L);
        when(allocationRepository.save(any(ResourceAllocation.class))).thenReturn(saved);

        AllocationResponse response = allocationService.createAllocation(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(AllocationStatus.PENDING);
        assertThat(response.getAllocationPercent()).isEqualTo(50);
        assertThat(response.getEmployeeCode()).isEqualTo("EMP-TEST-01");
        assertThat(response.getDemandCode()).isEqualTo("DEM-TEST-01");
    }

    @Test
    @DisplayName("Should reject allocation creation when requested capacity exceeds available capacity")
    void testCreateAllocation_insufficientCapacity_fails() {
        CreateAllocationRequest request = new CreateAllocationRequest(
                20L, 10L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50, "Lead PM"
        );

        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(demandRepository.findById(20L)).thenReturn(Optional.of(demand));
        when(availabilityService.assessEmployeeAvailability(eq(employee), any(), any(), eq(50)))
                .thenReturn(createUnavailableResponse("Requested allocation exceeds remaining available capacity"));

        assertThatThrownBy(() -> allocationService.createAllocation(request))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("Employee cannot be allocated");
    }

    @Test
    @DisplayName("Should reject allocation creation for a cancelled project demand")
    void testCreateAllocation_cancelledDemand_fails() {
        demand.setStatus(DemandStatus.CANCELLED);
        CreateAllocationRequest request = new CreateAllocationRequest(
                20L, 10L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50, "Lead PM"
        );

        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(demandRepository.findById(20L)).thenReturn(Optional.of(demand));

        assertThatThrownBy(() -> allocationService.createAllocation(request))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("Cannot allocate resources to a cancelled demand");
    }

    @Test
    @DisplayName("Should confirm allocation, revalidate capacity, and update demand status to ALLOCATED")
    void testConfirmAllocation_success_updatesDemandStatus() {
        ResourceAllocation allocation = new ResourceAllocation(
                employee, demand, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50,
                AllocationStatus.PENDING, "Lead PM"
        );
        allocation.setId(100L);

        when(allocationRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(allocation));
        when(availabilityService.assessEmployeeAvailabilityExcludingAllocation(
                eq(employee), any(), any(), eq(50), eq(100L)))
                .thenReturn(createAvailableResponse(100));
        when(allocationRepository.save(any(ResourceAllocation.class))).thenAnswer(i -> i.getArgument(0));
        when(allocationRepository.sumConfirmedAllocationsForDemand(20L)).thenReturn(50);

        AllocationResponse response = allocationService.confirmAllocation(100L, "Staffing Manager");

        assertThat(response.getStatus()).isEqualTo(AllocationStatus.CONFIRMED);
        assertThat(response.getConfirmedBy()).isEqualTo("Staffing Manager");
        assertThat(response.getConfirmedAt()).isNotNull();
        assertThat(demand.getStatus()).isEqualTo(DemandStatus.ALLOCATED);
        verify(demandRepository).save(demand);
    }

    @Test
    @DisplayName("Should fail allocation confirmation if concurrency check detects capacity was exhausted")
    void testConfirmAllocation_capacityRevalidationFails() {
        ResourceAllocation allocation = new ResourceAllocation(
                employee, demand, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50,
                AllocationStatus.PENDING, "Lead PM"
        );
        allocation.setId(100L);

        when(allocationRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(allocation));
        when(availabilityService.assessEmployeeAvailabilityExcludingAllocation(
                eq(employee), any(), any(), eq(50), eq(100L)))
                .thenReturn(createUnavailableResponse("Capacity already taken by another concurrent allocation"));

        assertThatThrownBy(() -> allocationService.confirmAllocation(100L, "Staffing Manager"))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("Allocation confirmation failed");
    }

    @Test
    @DisplayName("Should cancel confirmed allocation and restore demand status to OPEN")
    void testCancelAllocation_success() {
        ResourceAllocation allocation = new ResourceAllocation(
                employee, demand, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50,
                AllocationStatus.CONFIRMED, "Lead PM"
        );
        allocation.setId(100L);
        demand.setStatus(DemandStatus.ALLOCATED);

        when(allocationRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(allocation));
        when(allocationRepository.save(any(ResourceAllocation.class))).thenAnswer(i -> i.getArgument(0));
        when(allocationRepository.sumConfirmedAllocationsForDemand(20L)).thenReturn(0);

        AllocationResponse response = allocationService.cancelAllocation(100L, "Project postponed");

        assertThat(response.getStatus()).isEqualTo(AllocationStatus.CANCELLED);
        assertThat(response.getRejectionReason()).isEqualTo("Project postponed");
        assertThat(demand.getStatus()).isEqualTo(DemandStatus.OPEN);
        verify(demandRepository).save(demand);
    }

    @Test
    @DisplayName("Should reject allocation and record rejection reason")
    void testRejectAllocation_success() {
        ResourceAllocation allocation = new ResourceAllocation(
                employee, demand, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50,
                AllocationStatus.PENDING, "Lead PM"
        );
        allocation.setId(100L);

        when(allocationRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(allocation));
        when(allocationRepository.save(any(ResourceAllocation.class))).thenAnswer(i -> i.getArgument(0));

        RejectAllocationRequest request = new RejectAllocationRequest("Resource needed on priority client", "Resource Director");
        AllocationResponse response = allocationService.rejectAllocation(100L, request);

        assertThat(response.getStatus()).isEqualTo(AllocationStatus.REJECTED);
        assertThat(response.getRejectionReason()).isEqualTo("Resource needed on priority client");
        assertThat(response.getConfirmedBy()).isEqualTo("Resource Director");
    }
}
