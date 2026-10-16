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

import com.talentmarketplace.dto.response.AvailabilityAssessmentResponse;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeUnavailability;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.UnavailabilityType;
import com.talentmarketplace.exception.BusinessRuleValidationException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.entity.AllocationStatus;
import com.talentmarketplace.entity.ResourceAllocation;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeUnavailabilityRepository unavailabilityRepository;

    @Mock
    private ResourceAllocationRepository allocationRepository;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    private Employee activeEmployee;

    @BeforeEach
    void setUp() {
        activeEmployee = new Employee("EMP-AVAIL-01", "Alice", "Available", "alice.avail@example.com",
                "Engineering", "Software Engineer", "New York", LocalDate.of(2025, 1, 1), EmploymentStatus.ACTIVE, 100);
        activeEmployee.setId(1L);
    }

    @Test
    @DisplayName("Should evaluate employee as fully available when active, within capacity, and no unavailabilities")
    void testAssessAvailability_fullyAvailable() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate))
                .thenReturn(List.of());

        AvailabilityAssessmentResponse response = availabilityService.assessEmployeeAvailability(
                1L, startDate, endDate, 50
        );

        assertThat(response).isNotNull();
        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getEmployeeId()).isEqualTo(1L);
        assertThat(response.getRemainingCapacityPercent()).isEqualTo(100);
        assertThat(response.getRequestedAllocationPercent()).isEqualTo(50);
        assertThat(response.getOverlappingUnavailabilities()).isEmpty();
        assertThat(response.getReason()).contains("fully available");
    }

    @Test
    @DisplayName("Should exclude inactive employee and mark availability false with zero capacity")
    void testAssessAvailability_inactiveEmployeeExclusion() {
        activeEmployee.setEmploymentStatus(EmploymentStatus.INACTIVE);
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));

        AvailabilityAssessmentResponse response = availabilityService.assessEmployeeAvailability(
                1L, startDate, endDate, 50
        );

        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getRemainingCapacityPercent()).isEqualTo(0);
        assertThat(response.getReason()).contains("INACTIVE");
    }

    @Test
    @DisplayName("Should detect unavailability overlapping demand period and mark available false")
    void testAssessAvailability_unavailabilityOverlappingDemandPeriod() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        EmployeeUnavailability unavail = new EmployeeUnavailability(
                activeEmployee, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15),
                UnavailabilityType.ANNUAL_LEAVE, "Annual Leave"
        );
        unavail.setId(10L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate))
                .thenReturn(List.of(unavail));

        AvailabilityAssessmentResponse response = availabilityService.assessEmployeeAvailability(
                1L, startDate, endDate, 50
        );

        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getRemainingCapacityPercent()).isEqualTo(0);
        assertThat(response.getOverlappingUnavailabilities()).hasSize(1);
        assertThat(response.getOverlappingUnavailabilities().get(0).getType()).isEqualTo(UnavailabilityType.ANNUAL_LEAVE);
        assertThat(response.getReason()).contains("ANNUAL_LEAVE");
    }

    @Test
    @DisplayName("Should consider employee available when unavailability is outside demand period")
    void testAssessAvailability_unavailabilityOutsideDemandPeriod() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate))
                .thenReturn(List.of());

        AvailabilityAssessmentResponse response = availabilityService.assessEmployeeAvailability(
                1L, startDate, endDate, 100
        );

        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getRemainingCapacityPercent()).isEqualTo(100);
        assertThat(response.getOverlappingUnavailabilities()).isEmpty();
    }

    @Test
    @DisplayName("Should reject allocation when requested percent exceeds employee max capacity")
    void testAssessAvailability_allocationExceedsMaxCapacity() {
        activeEmployee.setMaxCapacityPercent(50);
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));

        AvailabilityAssessmentResponse response = availabilityService.assessEmployeeAvailability(
                1L, startDate, endDate, 80
        );

        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getRemainingCapacityPercent()).isEqualTo(50);
        assertThat(response.getReason()).contains("exceeds employee configured maximum capacity limit");
    }

    @Test
    @DisplayName("Should throw BusinessRuleValidationException when start date is after end date")
    void testAssessAvailability_invalidDates_startDateAfterEndDate() {
        LocalDate startDate = LocalDate.of(2026, 11, 30);
        LocalDate endDate = LocalDate.of(2026, 11, 1);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));

        assertThatThrownBy(() -> availabilityService.assessEmployeeAvailability(1L, startDate, endDate, 50))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("Start date cannot be after end date");
    }

    @Test
    @DisplayName("Should throw BusinessRuleValidationException when dates are null")
    void testAssessAvailability_nullDates() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));

        assertThatThrownBy(() -> availabilityService.assessEmployeeAvailability(1L, null, LocalDate.now(), 50))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("Start date and end date are required");
    }

    @Test
    @DisplayName("Should throw BusinessRuleValidationException when allocation percent is invalid")
    void testAssessAvailability_invalidAllocationPercent() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));

        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        assertThatThrownBy(() -> availabilityService.assessEmployeeAvailability(1L, startDate, endDate, 150))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("Requested allocation percent must be between 1 and 100");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employee does not exist")
    void testAssessAvailability_employeeNotFound() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.assessEmployeeAvailability(999L, startDate, endDate, 50))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    @DisplayName("Should reduce remaining capacity when confirmed allocation overlaps demand period")
    void testAssessAvailability_confirmedAllocationConsumesCapacity() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        ResourceAllocation confirmedAlloc = new ResourceAllocation(
                activeEmployee, null, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 25),
                60, AllocationStatus.CONFIRMED, "PM"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate))
                .thenReturn(List.of());
        when(allocationRepository.findOverlappingConfirmedAllocations(1L, startDate, endDate))
                .thenReturn(List.of(confirmedAlloc));

        // Employee max is 100%, 60% is confirmed -> remaining is 40%.
        // Requesting 50% should fail.
        AvailabilityAssessmentResponse response = availabilityService.assessEmployeeAvailability(
                1L, startDate, endDate, 50
        );

        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getRemainingCapacityPercent()).isEqualTo(40);
        assertThat(response.getReason()).contains("currently allocated: 60%");
    }

    @Test
    @DisplayName("Should consider employee available when confirmed allocation leaves sufficient capacity")
    void testAssessAvailability_confirmedAllocationLeavesSufficientCapacity() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        ResourceAllocation confirmedAlloc = new ResourceAllocation(
                activeEmployee, null, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 25),
                40, AllocationStatus.CONFIRMED, "PM"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate))
                .thenReturn(List.of());
        when(allocationRepository.findOverlappingConfirmedAllocations(1L, startDate, endDate))
                .thenReturn(List.of(confirmedAlloc));

        // Employee max is 100%, 40% is confirmed -> remaining is 60%.
        // Requesting 50% should succeed.
        AvailabilityAssessmentResponse response = availabilityService.assessEmployeeAvailability(
                1L, startDate, endDate, 50
        );

        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getRemainingCapacityPercent()).isEqualTo(60);
    }

    @Test
    @DisplayName("Should evaluate peak allocation across daily overlapping intervals rather than single aggregate average")
    void testAssessAvailability_multipleOverlappingAllocations_calculatesDailyPeakRemainingCapacity() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        // Alloc 1: Nov 1 - Nov 15 @ 40%
        ResourceAllocation alloc1 = new ResourceAllocation(
                activeEmployee, null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 15),
                40, AllocationStatus.CONFIRMED, "PM1"
        );
        // Alloc 2: Nov 10 - Nov 25 @ 40%
        ResourceAllocation alloc2 = new ResourceAllocation(
                activeEmployee, null, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 25),
                40, AllocationStatus.CONFIRMED, "PM2"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate))
                .thenReturn(List.of());
        when(allocationRepository.findOverlappingConfirmedAllocations(1L, startDate, endDate))
                .thenReturn(List.of(alloc1, alloc2));

        // On Nov 10-15, peak simultaneous allocation is 40% + 40% = 80%.
        // Remaining capacity is 100 - 80 = 20%.
        // A requested allocation of 30% must fail because at peak it would demand 110%.
        AvailabilityAssessmentResponse response = availabilityService.assessEmployeeAvailability(
                1L, startDate, endDate, 30
        );

        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getPeakAllocatedPercent()).isEqualTo(80);
        assertThat(response.getRemainingCapacityPercent()).isEqualTo(20);
        assertThat(response.getReason()).contains("currently allocated: 80%");
    }

    @Test
    @DisplayName("Should enforce inclusive date range boundary: allocation on exact boundary dates")
    void testAssessAvailability_boundaryOverlap_inclusiveDateRange() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        // Allocation starting on exactly the end date Nov 30
        ResourceAllocation boundaryAlloc = new ResourceAllocation(
                activeEmployee, null, LocalDate.of(2026, 11, 30), LocalDate.of(2026, 12, 10),
                70, AllocationStatus.CONFIRMED, "PM"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate))
                .thenReturn(List.of());
        when(allocationRepository.findOverlappingConfirmedAllocations(1L, startDate, endDate))
                .thenReturn(List.of(boundaryAlloc));

        // Peak is 70% on Nov 30; remaining capacity is 30%.
        // Requesting 40% should fail.
        AvailabilityAssessmentResponse response = availabilityService.assessEmployeeAvailability(
                1L, startDate, endDate, 40
        );

        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getPeakAllocatedPercent()).isEqualTo(70);
        assertThat(response.getRemainingCapacityPercent()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should track PENDING allocations without depleting capacity across other demands")
    void testAssessAvailability_pendingAllocationsDoNotDepleteCapacity_butAreTracked() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        ResourceAllocation pendingAlloc = new ResourceAllocation(
                activeEmployee, null, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 20),
                50, AllocationStatus.PENDING, "PM"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate))
                .thenReturn(List.of());
        when(allocationRepository.findOverlappingConfirmedAllocations(1L, startDate, endDate))
                .thenReturn(List.of());
        when(allocationRepository.findOverlappingPendingAllocations(1L, startDate, endDate))
                .thenReturn(List.of(pendingAlloc));

        AvailabilityAssessmentResponse response = availabilityService.assessEmployeeAvailability(
                1L, startDate, endDate, 100
        );

        // Confirmed capacity is 100% available because PENDING proposals do not lock out other demands
        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getPeakAllocatedPercent()).isEqualTo(0);
        assertThat(response.getRemainingCapacityPercent()).isEqualTo(100);
        assertThat(response.getPendingAllocationsCount()).isEqualTo(1);
        assertThat(response.getPendingAllocationPercent()).isEqualTo(50);
        assertThat(response.getReason()).contains("pending proposal(s) awaiting review");
    }

    @Test
    @DisplayName("100% configured capacity with no confirmed allocations: shows 100% configured, 0% committed, 100% remaining")
    void testAssessAvailability_100PercentConfigured_NoAllocations() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate)).thenReturn(List.of());
        when(allocationRepository.findOverlappingConfirmedAllocations(1L, startDate, endDate)).thenReturn(List.of());
        when(allocationRepository.findOverlappingPendingAllocations(1L, startDate, endDate)).thenReturn(List.of());

        AvailabilityAssessmentResponse response = availabilityService.assessEmployeeAvailability(1L, startDate, endDate, 100);

        assertThat(response.getMaxCapacityPercent()).isEqualTo(100);
        assertThat(response.getPeakAllocatedPercent()).isEqualTo(0);
        assertThat(response.getRemainingCapacityPercent()).isEqualTo(100);
        assertThat(response.isAvailable()).isTrue();
        assertThat(activeEmployee.getMaxCapacityPercent()).isEqualTo(100); // Configured capacity is immutable
    }

    @Test
    @DisplayName("100% configured capacity with 70% confirmed allocation: shows 100% configured, 70% committed, 30% remaining")
    void testAssessAvailability_100PercentConfigured_With70PercentConfirmed() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        ResourceAllocation alloc70 = new ResourceAllocation(
                activeEmployee, null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30),
                70, AllocationStatus.CONFIRMED, "PM"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate)).thenReturn(List.of());
        when(allocationRepository.findOverlappingConfirmedAllocations(1L, startDate, endDate)).thenReturn(List.of(alloc70));
        when(allocationRepository.findOverlappingPendingAllocations(1L, startDate, endDate)).thenReturn(List.of());

        // Assessment for 30% requested allocation should succeed
        AvailabilityAssessmentResponse response30 = availabilityService.assessEmployeeAvailability(1L, startDate, endDate, 30);
        assertThat(response30.getMaxCapacityPercent()).isEqualTo(100);
        assertThat(response30.getPeakAllocatedPercent()).isEqualTo(70);
        assertThat(response30.getRemainingCapacityPercent()).isEqualTo(30);
        assertThat(response30.isAvailable()).isTrue();

        // Assessment for 40% requested allocation should fail
        AvailabilityAssessmentResponse response40 = availabilityService.assessEmployeeAvailability(1L, startDate, endDate, 40);
        assertThat(response40.getMaxCapacityPercent()).isEqualTo(100);
        assertThat(response40.getPeakAllocatedPercent()).isEqualTo(70);
        assertThat(response40.getRemainingCapacityPercent()).isEqualTo(30);
        assertThat(response40.isAvailable()).isFalse();

        // Crucial invariant: Allocations do NOT mutate configured capacity
        assertThat(activeEmployee.getMaxCapacityPercent()).isEqualTo(100);
    }

    @Test
    @DisplayName("30% configured capacity (Kavita Sharma model): shows 30% configured, 0% committed, 30% remaining")
    void testAssessAvailability_30PercentConfigured_KavitaSharmaModel() {
        activeEmployee.setMaxCapacityPercent(30);
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate)).thenReturn(List.of());
        when(allocationRepository.findOverlappingConfirmedAllocations(1L, startDate, endDate)).thenReturn(List.of());
        when(allocationRepository.findOverlappingPendingAllocations(1L, startDate, endDate)).thenReturn(List.of());

        // 25% allocation request within 30% capacity succeeds
        AvailabilityAssessmentResponse res25 = availabilityService.assessEmployeeAvailability(1L, startDate, endDate, 25);
        assertThat(res25.getMaxCapacityPercent()).isEqualTo(30);
        assertThat(res25.getPeakAllocatedPercent()).isEqualTo(0);
        assertThat(res25.getRemainingCapacityPercent()).isEqualTo(30);
        assertThat(res25.isAvailable()).isTrue();

        // 50% allocation request exceeds 30% configured capacity limit and fails
        AvailabilityAssessmentResponse res50 = availabilityService.assessEmployeeAvailability(1L, startDate, endDate, 50);
        assertThat(res50.getMaxCapacityPercent()).isEqualTo(30);
        assertThat(res50.getPeakAllocatedPercent()).isEqualTo(0);
        assertThat(res50.getRemainingCapacityPercent()).isEqualTo(30);
        assertThat(res50.isAvailable()).isFalse();
        assertThat(res50.getReason()).contains("exceeds employee configured maximum capacity limit (30%)");
    }

    @Test
    @DisplayName("30% configured capacity with 20% confirmed: shows 30% configured, 20% committed, 10% remaining")
    void testAssessAvailability_30PercentConfigured_WithConfirmedAllocation() {
        activeEmployee.setMaxCapacityPercent(30);
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        ResourceAllocation alloc20 = new ResourceAllocation(
                activeEmployee, null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30),
                20, AllocationStatus.CONFIRMED, "PM"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(1L, startDate, endDate)).thenReturn(List.of());
        when(allocationRepository.findOverlappingConfirmedAllocations(1L, startDate, endDate)).thenReturn(List.of(alloc20));
        when(allocationRepository.findOverlappingPendingAllocations(1L, startDate, endDate)).thenReturn(List.of());

        AvailabilityAssessmentResponse res10 = availabilityService.assessEmployeeAvailability(1L, startDate, endDate, 10);
        assertThat(res10.getMaxCapacityPercent()).isEqualTo(30);
        assertThat(res10.getPeakAllocatedPercent()).isEqualTo(20);
        assertThat(res10.getRemainingCapacityPercent()).isEqualTo(10);
        assertThat(res10.isAvailable()).isTrue();

        AvailabilityAssessmentResponse res15 = availabilityService.assessEmployeeAvailability(1L, startDate, endDate, 15);
        assertThat(res15.getMaxCapacityPercent()).isEqualTo(30);
        assertThat(res15.getPeakAllocatedPercent()).isEqualTo(20);
        assertThat(res15.getRemainingCapacityPercent()).isEqualTo(10);
        assertThat(res15.isAvailable()).isFalse();
    }
}
