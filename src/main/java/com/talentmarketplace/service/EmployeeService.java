package com.talentmarketplace.service;

import java.util.List;

import com.talentmarketplace.dto.request.CreateEmployeeRequest;
import com.talentmarketplace.dto.request.UpdateEmployeeRequest;
import com.talentmarketplace.dto.response.EmployeeResponse;
import com.talentmarketplace.entity.EmploymentStatus;

public interface EmployeeService {

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse getEmployeeByCode(String employeeCode);

    List<EmployeeResponse> searchEmployees(String department, String location, EmploymentStatus status, String search);

    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    void deleteEmployee(Long id);
}
