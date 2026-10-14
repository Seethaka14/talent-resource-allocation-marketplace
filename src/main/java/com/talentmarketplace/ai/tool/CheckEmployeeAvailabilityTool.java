package com.talentmarketplace.ai.tool;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.util.List;
import org.springframework.stereotype.Component;

import com.talentmarketplace.ai.ollama.OllamaTool;
import com.talentmarketplace.dto.response.AvailabilityAssessmentResponse;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.service.AvailabilityService;

@Component
public class CheckEmployeeAvailabilityTool implements AiTool {

    private final AvailabilityService availabilityService;
    private final EmployeeRepository employeeRepository;

    public CheckEmployeeAvailabilityTool(AvailabilityService availabilityService, EmployeeRepository employeeRepository) {
        this.availabilityService = availabilityService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public String getName() {
        return "check_employee_availability";
    }

    @Override
    public String getDescription() {
        return "Check whether an employee is available for a specified date range and requested allocation percentage";
    }

    @Override
    public OllamaTool getOllamaTool() {
        Map<String, Object> props = new HashMap<>();

        Map<String, Object> empProp = new HashMap<>();
        empProp.put("type", "string");
        empProp.put("description", "Employee ID (e.g. 201), employee code (e.g. DEMO-EMP-004), or employee full name (e.g. Devon Vance)");
        props.put("employeeId", empProp);

        Map<String, Object> startProp = new HashMap<>();
        startProp.put("type", "string");
        startProp.put("description", "Evaluation start date in YYYY-MM-DD format (e.g. 2026-11-01)");
        props.put("startDate", startProp);

        Map<String, Object> endProp = new HashMap<>();
        endProp.put("type", "string");
        endProp.put("description", "Evaluation end date in YYYY-MM-DD format (e.g. 2026-11-30)");
        props.put("endDate", endProp);

        Map<String, Object> allocProp = new HashMap<>();
        allocProp.put("type", "integer");
        allocProp.put("description", "Requested allocation percentage (1-100, default 100)");
        props.put("requestedAllocationPercent", allocProp);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", props);
        parameters.put("required", Arrays.asList("employeeId", "startDate", "endDate"));

        return OllamaTool.function(getName(), getDescription(), parameters);
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        if (arguments == null || !arguments.containsKey("employeeId") || arguments.get("employeeId") == null) {
            throw new IllegalArgumentException("Argument 'employeeId' is required for tool " + getName());
        }
        if (!arguments.containsKey("startDate") || arguments.get("startDate") == null) {
            throw new IllegalArgumentException("Argument 'startDate' is required for tool " + getName());
        }
        if (!arguments.containsKey("endDate") || arguments.get("endDate") == null) {
            throw new IllegalArgumentException("Argument 'endDate' is required for tool " + getName());
        }

        Long employeeId;
        String rawEmp = arguments.get("employeeId").toString().trim();
        try {
            employeeId = Long.valueOf(rawEmp);
        } catch (NumberFormatException e) {
            List<Employee> found = employeeRepository.searchEmployees(null, null, null, rawEmp);
            if (!found.isEmpty()) {
                employeeId = found.get(0).getId();
            } else {
                throw new IllegalArgumentException("Employee '" + rawEmp + "' was not found in the database.");
            }
        }

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(arguments.get("startDate").toString());
            endDate = LocalDate.parse(arguments.get("endDate").toString());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD: " + e.getMessage());
        }

        int requestedAllocation = 100;
        if (arguments.containsKey("requestedAllocationPercent") && arguments.get("requestedAllocationPercent") != null) {
            try {
                requestedAllocation = Integer.parseInt(arguments.get("requestedAllocationPercent").toString());
            } catch (NumberFormatException ignored) {
            }
        }

        AvailabilityAssessmentResponse assessment = availabilityService.assessEmployeeAvailability(
                employeeId, startDate, endDate, requestedAllocation
        );

        Map<String, Object> result = new HashMap<>();
        result.put("employeeId", assessment.getEmployeeId());
        result.put("employeeName", assessment.getEmployeeName());
        result.put("employeeCode", assessment.getEmployeeCode());
        result.put("employmentStatus", assessment.getEmploymentStatus());
        result.put("available", assessment.isAvailable());
        result.put("remainingCapacityPercent", assessment.getRemainingCapacityPercent());
        result.put("maxCapacityPercent", assessment.getMaxCapacityPercent());
        result.put("reason", assessment.getReason());
        result.put("overlappingUnavailabilities", assessment.getOverlappingUnavailabilities());

        return result;
    }
}
