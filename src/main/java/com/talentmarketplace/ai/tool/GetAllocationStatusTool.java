package com.talentmarketplace.ai.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.talentmarketplace.ai.ollama.OllamaTool;
import com.talentmarketplace.dto.response.AllocationResponse;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.service.ResourceAllocationService;

@Component
public class GetAllocationStatusTool implements AiTool {

    private final ResourceAllocationService resourceAllocationService;
    private final EmployeeRepository employeeRepository;

    public GetAllocationStatusTool(ResourceAllocationService resourceAllocationService, EmployeeRepository employeeRepository) {
        this.resourceAllocationService = resourceAllocationService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public String getName() {
        return "get_allocation_status";
    }

    @Override
    public String getDescription() {
        return "Retrieve current resource allocation status and history for a demand ID or employee ID";
    }

    @Override
    public OllamaTool getOllamaTool() {
        Map<String, Object> props = new HashMap<>();

        Map<String, Object> demandProp = new HashMap<>();
        demandProp.put("type", "integer");
        demandProp.put("description", "Optional numeric project demand ID");
        props.put("demandId", demandProp);

        Map<String, Object> empProp = new HashMap<>();
        empProp.put("type", "string");
        empProp.put("description", "Optional employee ID, employee code, or employee name");
        props.put("employeeId", empProp);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", props);

        return OllamaTool.function(getName(), getDescription(), parameters);
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        Long demandId = null;
        Long employeeId = null;

        if (arguments != null) {
            if (arguments.containsKey("demandId") && arguments.get("demandId") != null) {
                try {
                    demandId = Long.valueOf(arguments.get("demandId").toString());
                } catch (NumberFormatException ignored) {
                }
            }
            if (arguments.containsKey("employeeId") && arguments.get("employeeId") != null) {
                String empArg = arguments.get("employeeId").toString().trim();
                try {
                    employeeId = Long.valueOf(empArg);
                } catch (NumberFormatException e) {
                    List<Employee> found = employeeRepository.searchEmployees(null, null, null, empArg);
                    if (!found.isEmpty()) {
                        employeeId = found.get(0).getId();
                    }
                }
            }
        }

        if (demandId == null && employeeId == null) {
            throw new IllegalArgumentException("Either 'demandId' or 'employeeId' must be provided to query allocations.");
        }

        List<AllocationResponse> allocations = new ArrayList<>();
        if (demandId != null) {
            allocations = resourceAllocationService.getAllocationsForDemand(demandId);
        } else {
            allocations = resourceAllocationService.getAllocationsForEmployee(employeeId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("queryDemandId", demandId);
        result.put("queryEmployeeId", employeeId);
        result.put("allocationCount", allocations.size());

        List<Map<String, Object>> list = allocations.stream().map(a -> {
            Map<String, Object> aMap = new HashMap<>();
            aMap.put("allocationId", a.getId());
            aMap.put("employeeId", a.getEmployeeId());
            aMap.put("employeeName", a.getEmployeeName());
            aMap.put("employeeCode", a.getEmployeeCode());
            aMap.put("demandId", a.getDemandId());
            aMap.put("demandCode", a.getDemandCode());
            aMap.put("role", a.getRole());
            aMap.put("startDate", a.getStartDate());
            aMap.put("endDate", a.getEndDate());
            aMap.put("allocationPercent", a.getAllocationPercent());
            aMap.put("status", a.getStatus());
            aMap.put("confirmedBy", a.getConfirmedBy());
            return aMap;
        }).collect(Collectors.toList());

        result.put("allocations", list);
        return result;
    }
}
