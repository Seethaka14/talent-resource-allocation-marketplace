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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.talentmarketplace.dto.request.CreateUnavailabilityRequest;
import com.talentmarketplace.dto.request.UpdateUnavailabilityRequest;
import com.talentmarketplace.dto.response.ApiResponse;
import com.talentmarketplace.dto.response.UnavailabilityResponse;
import com.talentmarketplace.service.EmployeeUnavailabilityService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees/{employeeId}/unavailability")
public class EmployeeUnavailabilityController {

    private final EmployeeUnavailabilityService unavailabilityService;

    public EmployeeUnavailabilityController(EmployeeUnavailabilityService unavailabilityService) {
        this.unavailabilityService = unavailabilityService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UnavailabilityResponse>> createUnavailability(
            @PathVariable Long employeeId,
            @Valid @RequestBody CreateUnavailabilityRequest request) {
        UnavailabilityResponse response = unavailabilityService.createUnavailability(employeeId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.ok("Unavailability recorded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UnavailabilityResponse>>> getEmployeeUnavailabilities(
            @PathVariable Long employeeId) {
        List<UnavailabilityResponse> responses = unavailabilityService.getEmployeeUnavailabilities(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UnavailabilityResponse>> getUnavailabilityById(
            @PathVariable Long employeeId,
            @PathVariable Long id) {
        UnavailabilityResponse response = unavailabilityService.getUnavailabilityById(employeeId, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UnavailabilityResponse>> updateUnavailability(
            @PathVariable Long employeeId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUnavailabilityRequest request) {
        UnavailabilityResponse response = unavailabilityService.updateUnavailability(employeeId, id, request);
        return ResponseEntity.ok(ApiResponse.ok("Unavailability updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnavailability(
            @PathVariable Long employeeId,
            @PathVariable Long id) {
        unavailabilityService.deleteUnavailability(employeeId, id);
        return ResponseEntity.noContent().build();
    }
}
