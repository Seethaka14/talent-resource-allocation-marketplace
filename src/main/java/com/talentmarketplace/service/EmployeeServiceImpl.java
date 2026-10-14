package com.talentmarketplace.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentmarketplace.dto.request.CreateEmployeeRequest;
import com.talentmarketplace.dto.request.UpdateEmployeeRequest;
import com.talentmarketplace.dto.response.EmployeeResponse;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.exception.DuplicateResourceException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new DuplicateResourceException("Employee", "employeeCode", request.getEmployeeCode());
        }

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }

        Employee employee = new Employee(
                request.getEmployeeCode().trim(),
                request.getFirstName().trim(),
                request.getLastName().trim(),
                request.getEmail().trim().toLowerCase(),
                request.getDepartment().trim(),
                request.getDesignation().trim(),
                request.getLocation().trim(),
                request.getJoiningDate(),
                request.getEmploymentStatus(),
                request.getMaxCapacityPercent() != null ? request.getMaxCapacityPercent() : 100
        );

        Employee saved = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return EmployeeResponse.fromEntity(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByCode(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeCode", employeeCode));
        return EmployeeResponse.fromEntity(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> searchEmployees(String department, String location,
                                                 EmploymentStatus status, String search) {
        String trimmedDept = (department != null && !department.isBlank()) ? department.trim() : null;
        String trimmedLoc = (location != null && !location.isBlank()) ? location.trim() : null;
        String trimmedSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        return employeeRepository.searchEmployees(trimmedDept, trimmedLoc, status, trimmedSearch)
                .stream()
                .map(EmployeeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (employeeRepository.existsByEmailAndIdNot(request.getEmail().trim().toLowerCase(), id)) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }

        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setEmail(request.getEmail().trim().toLowerCase());
        employee.setDepartment(request.getDepartment().trim());
        employee.setDesignation(request.getDesignation().trim());
        employee.setLocation(request.getLocation().trim());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setEmploymentStatus(request.getEmploymentStatus());
        if (request.getMaxCapacityPercent() != null) {
            employee.setMaxCapacityPercent(request.getMaxCapacityPercent());
        }

        Employee updated = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employeeRepository.delete(employee);
    }
}
