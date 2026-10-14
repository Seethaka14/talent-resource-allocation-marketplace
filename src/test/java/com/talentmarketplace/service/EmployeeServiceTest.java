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

import com.talentmarketplace.dto.request.CreateEmployeeRequest;
import com.talentmarketplace.dto.request.UpdateEmployeeRequest;
import com.talentmarketplace.dto.response.EmployeeResponse;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.exception.DuplicateResourceException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private CreateEmployeeRequest sampleCreateRequest;
    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleCreateRequest = new CreateEmployeeRequest(
                "EMP-001",
                "Alice",
                "Smith",
                "alice.smith@example.com",
                "Engineering",
                "Senior Java Engineer",
                "Bangalore",
                LocalDate.of(2023, 1, 15),
                EmploymentStatus.ACTIVE,
                100
        );

        sampleEmployee = new Employee(
                "EMP-001",
                "Alice",
                "Smith",
                "alice.smith@example.com",
                "Engineering",
                "Senior Java Engineer",
                "Bangalore",
                LocalDate.of(2023, 1, 15),
                EmploymentStatus.ACTIVE,
                100
        );
    }

    @Test
    @DisplayName("createEmployee: successfully creates employee when inputs are valid")
    void createEmployee_Success() {
        when(employeeRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
        when(employeeRepository.existsByEmail("alice.smith@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

        EmployeeResponse response = employeeService.createEmployee(sampleCreateRequest);

        assertNotNull(response);
        assertEquals("EMP-001", response.getEmployeeCode());
        assertEquals("Alice Smith", response.getName());
        assertEquals("alice.smith@example.com", response.getEmail());
        assertEquals(100, response.getMaxCapacityPercent());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee: throws DuplicateResourceException when employeeCode exists")
    void createEmployee_DuplicateEmployeeCode() {
        when(employeeRepository.existsByEmployeeCode("EMP-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                employeeService.createEmployee(sampleCreateRequest)
        );
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee: throws DuplicateResourceException when email exists")
    void createEmployee_DuplicateEmail() {
        when(employeeRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
        when(employeeRepository.existsByEmail("alice.smith@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                employeeService.createEmployee(sampleCreateRequest)
        );
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("getEmployeeById: returns employee when found")
    void getEmployeeById_Found() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        EmployeeResponse response = employeeService.getEmployeeById(1L);

        assertNotNull(response);
        assertEquals("EMP-001", response.getEmployeeCode());
    }

    @Test
    @DisplayName("getEmployeeById: throws ResourceNotFoundException when not found")
    void getEmployeeById_NotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                employeeService.getEmployeeById(999L)
        );
    }

    @Test
    @DisplayName("getEmployeeByCode: returns employee when found")
    void getEmployeeByCode_Found() {
        when(employeeRepository.findByEmployeeCode("EMP-001")).thenReturn(Optional.of(sampleEmployee));

        EmployeeResponse response = employeeService.getEmployeeByCode("EMP-001");

        assertNotNull(response);
        assertEquals("EMP-001", response.getEmployeeCode());
    }

    @Test
    @DisplayName("updateEmployee: updates fields successfully")
    void updateEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(employeeRepository.existsByEmailAndIdNot("alice.updated@example.com", 1L)).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

        UpdateEmployeeRequest updateReq = new UpdateEmployeeRequest(
                "Alice",
                "Johnson",
                "alice.updated@example.com",
                "Engineering",
                "Lead Engineer",
                "Hyderabad",
                LocalDate.of(2023, 1, 15),
                EmploymentStatus.ACTIVE,
                80
        );

        EmployeeResponse response = employeeService.updateEmployee(1L, updateReq);

        assertNotNull(response);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("deleteEmployee: deletes existing employee")
    void deleteEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).delete(sampleEmployee);
    }

    @Test
    @DisplayName("searchEmployees: calls search query with normalized filters")
    void searchEmployees_Success() {
        when(employeeRepository.searchEmployees("Engineering", "Bangalore", EmploymentStatus.ACTIVE, "Alice"))
                .thenReturn(List.of(sampleEmployee));

        List<EmployeeResponse> results = employeeService.searchEmployees(" Engineering ", " Bangalore ", EmploymentStatus.ACTIVE, " Alice ");

        assertEquals(1, results.size());
        assertEquals("EMP-001", results.get(0).getEmployeeCode());
    }
}
