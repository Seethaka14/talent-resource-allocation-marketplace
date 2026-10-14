package com.talentmarketplace.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.talentmarketplace.dto.request.CreateUnavailabilityRequest;
import com.talentmarketplace.dto.request.UpdateUnavailabilityRequest;
import com.talentmarketplace.dto.response.UnavailabilityResponse;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeUnavailability;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.UnavailabilityType;
import com.talentmarketplace.exception.BusinessRuleValidationException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeUnavailabilityServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeUnavailabilityRepository unavailabilityRepository;

    @InjectMocks
    private EmployeeUnavailabilityServiceImpl unavailabilityService;

    private Employee employee;
    private EmployeeUnavailability sampleUnavailability;

    @BeforeEach
    void setUp() {
        employee = new Employee(
                "EMP-001", "Alice", "Smith", "alice@example.com",
                "Engineering", "Senior Dev", "Bangalore",
                LocalDate.of(2023, 1, 1), EmploymentStatus.ACTIVE
        );
        employee.setId(1L);

        sampleUnavailability = new EmployeeUnavailability(
                employee,
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 10),
                UnavailabilityType.ANNUAL_LEAVE,
                "Family vacation"
        );
        sampleUnavailability.setId(10L);
    }

    @Test
    @DisplayName("createUnavailability: creates record when valid and non-overlapping")
    void createUnavailability_Success() {
        CreateUnavailabilityRequest request = new CreateUnavailabilityRequest(
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 10),
                UnavailabilityType.ANNUAL_LEAVE,
                "Family vacation"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(
                1L, request.getStartDate(), request.getEndDate()
        )).thenReturn(List.of());
        when(unavailabilityRepository.save(any(EmployeeUnavailability.class))).thenReturn(sampleUnavailability);

        UnavailabilityResponse response = unavailabilityService.createUnavailability(1L, request);

        assertNotNull(response);
        assertEquals(UnavailabilityType.ANNUAL_LEAVE, response.getType());
        assertEquals("EMP-001", response.getEmployeeCode());
        verify(unavailabilityRepository).save(any(EmployeeUnavailability.class));
    }

    @Test
    @DisplayName("createUnavailability: throws ResourceNotFoundException when employee does not exist")
    void createUnavailability_EmployeeNotFound() {
        CreateUnavailabilityRequest request = new CreateUnavailabilityRequest(
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 10),
                UnavailabilityType.ANNUAL_LEAVE,
                "Vacation"
        );

        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                unavailabilityService.createUnavailability(999L, request)
        );
        verify(unavailabilityRepository, never()).save(any(EmployeeUnavailability.class));
    }

    @Test
    @DisplayName("createUnavailability: throws BusinessRuleValidationException when start date is after end date")
    void createUnavailability_InvalidDates() {
        CreateUnavailabilityRequest request = new CreateUnavailabilityRequest(
                LocalDate.of(2024, 6, 15),
                LocalDate.of(2024, 6, 10), // End before start
                UnavailabilityType.SICK_LEAVE,
                "Medical"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(BusinessRuleValidationException.class, () ->
                unavailabilityService.createUnavailability(1L, request)
        );
        verify(unavailabilityRepository, never()).save(any(EmployeeUnavailability.class));
    }

    @Test
    @DisplayName("createUnavailability: throws BusinessRuleValidationException when overlapping period exists")
    void createUnavailability_OverlappingRejected() {
        CreateUnavailabilityRequest request = new CreateUnavailabilityRequest(
                LocalDate.of(2024, 6, 5),
                LocalDate.of(2024, 6, 15),
                UnavailabilityType.ANNUAL_LEAVE,
                "Overlap attempt"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(unavailabilityRepository.findOverlappingUnavailabilities(
                1L, request.getStartDate(), request.getEndDate()
        )).thenReturn(List.of(sampleUnavailability));

        assertThrows(BusinessRuleValidationException.class, () ->
                unavailabilityService.createUnavailability(1L, request)
        );
        verify(unavailabilityRepository, never()).save(any(EmployeeUnavailability.class));
    }

    @Test
    @DisplayName("getEmployeeUnavailabilities: returns list of unavailabilities")
    void getEmployeeUnavailabilities_Success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(unavailabilityRepository.findByEmployeeIdOrderByStartDateAsc(1L)).thenReturn(List.of(sampleUnavailability));

        List<UnavailabilityResponse> responses = unavailabilityService.getEmployeeUnavailabilities(1L);

        assertEquals(1, responses.size());
        assertEquals(LocalDate.of(2024, 6, 1), responses.get(0).getStartDate());
    }

    @Test
    @DisplayName("updateUnavailability: updates successfully when no conflict")
    void updateUnavailability_Success() {
        UpdateUnavailabilityRequest request = new UpdateUnavailabilityRequest(
                LocalDate.of(2024, 7, 1),
                LocalDate.of(2024, 7, 5),
                UnavailabilityType.TRAINING,
                "Cloud Certification Course"
        );

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(unavailabilityRepository.findById(10L)).thenReturn(Optional.of(sampleUnavailability));
        when(unavailabilityRepository.findOverlappingUnavailabilitiesExcludingId(
                1L, 10L, request.getStartDate(), request.getEndDate()
        )).thenReturn(List.of());
        when(unavailabilityRepository.save(any(EmployeeUnavailability.class))).thenReturn(sampleUnavailability);

        UnavailabilityResponse response = unavailabilityService.updateUnavailability(1L, 10L, request);

        assertNotNull(response);
        verify(unavailabilityRepository).save(sampleUnavailability);
    }

    @Test
    @DisplayName("deleteUnavailability: deletes existing record")
    void deleteUnavailability_Success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(unavailabilityRepository.findById(10L)).thenReturn(Optional.of(sampleUnavailability));

        unavailabilityService.deleteUnavailability(1L, 10L);

        verify(unavailabilityRepository).delete(sampleUnavailability);
    }
}
