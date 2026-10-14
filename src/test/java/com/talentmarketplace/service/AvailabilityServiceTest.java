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
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeUnavailabilityRepository unavailabilityRepository;

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
}
