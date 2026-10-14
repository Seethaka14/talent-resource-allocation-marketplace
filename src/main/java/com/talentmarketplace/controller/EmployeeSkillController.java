package com.talentmarketplace.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.talentmarketplace.dto.request.AssignSkillRequest;
import com.talentmarketplace.dto.request.UpdateEmployeeSkillRequest;
import com.talentmarketplace.dto.response.ApiResponse;
import com.talentmarketplace.dto.response.EmployeeSkillResponse;
import com.talentmarketplace.service.EmployeeSkillService;

import jakarta.validation.Valid;

@RestController
public class EmployeeSkillController {

    private final EmployeeSkillService employeeSkillService;

    public EmployeeSkillController(EmployeeSkillService employeeSkillService) {
        this.employeeSkillService = employeeSkillService;
    }

    @PostMapping("/api/employees/{employeeId}/skills")
    public ResponseEntity<ApiResponse<EmployeeSkillResponse>> assignSkill(
            @PathVariable Long employeeId,
            @Valid @RequestBody AssignSkillRequest request) {
        EmployeeSkillResponse response = employeeSkillService.assignSkillToEmployee(employeeId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{skillId}")
                .buildAndExpand(response.getSkillId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.ok("Skill assigned to employee successfully", response));
    }

    @GetMapping("/api/employees/{employeeId}/skills")
    public ResponseEntity<ApiResponse<List<EmployeeSkillResponse>>> getEmployeeSkills(
            @PathVariable Long employeeId) {
        List<EmployeeSkillResponse> responses = employeeSkillService.getEmployeeSkills(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/api/employees/{employeeId}/skills/{skillId}")
    public ResponseEntity<ApiResponse<EmployeeSkillResponse>> getEmployeeSkill(
            @PathVariable Long employeeId,
            @PathVariable Long skillId) {
        EmployeeSkillResponse response = employeeSkillService.getEmployeeSkill(employeeId, skillId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/api/employees/{employeeId}/skills/{skillId}")
    public ResponseEntity<ApiResponse<EmployeeSkillResponse>> updateEmployeeSkill(
            @PathVariable Long employeeId,
            @PathVariable Long skillId,
            @Valid @RequestBody UpdateEmployeeSkillRequest request) {
        EmployeeSkillResponse response = employeeSkillService.updateEmployeeSkill(employeeId, skillId, request);
        return ResponseEntity.ok(ApiResponse.ok("Employee skill updated successfully", response));
    }

    @DeleteMapping("/api/employees/{employeeId}/skills/{skillId}")
    public ResponseEntity<Void> removeEmployeeSkill(
            @PathVariable Long employeeId,
            @PathVariable Long skillId) {
        employeeSkillService.removeSkillFromEmployee(employeeId, skillId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/skills/{skillId}/employees")
    public ResponseEntity<ApiResponse<List<EmployeeSkillResponse>>> getEmployeesBySkill(
            @PathVariable Long skillId) {
        List<EmployeeSkillResponse> responses = employeeSkillService.getEmployeesBySkill(skillId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
