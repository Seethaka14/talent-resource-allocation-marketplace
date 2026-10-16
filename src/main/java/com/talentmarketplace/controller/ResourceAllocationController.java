package com.talentmarketplace.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.talentmarketplace.dto.request.CreateAllocationRequest;
import com.talentmarketplace.dto.request.RejectAllocationRequest;
import com.talentmarketplace.dto.response.AllocationResponse;
import com.talentmarketplace.dto.response.ApiResponse;
import com.talentmarketplace.service.ResourceAllocationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/allocations")
public class ResourceAllocationController {

    private final ResourceAllocationService allocationService;

    public ResourceAllocationController(ResourceAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AllocationResponse>> createAllocation(
            @Valid @RequestBody CreateAllocationRequest request) {
        AllocationResponse response = allocationService.createAllocation(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.ok("Resource allocation created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AllocationResponse>>> getAllAllocations(
            @org.springframework.web.bind.annotation.RequestParam(required = false) com.talentmarketplace.entity.AllocationStatus status) {
        List<AllocationResponse> responses = allocationService.getAllAllocations(status);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<AllocationResponse>> confirmAllocation(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String confirmedBy = (body != null && body.containsKey("confirmedBy")) ? body.get("confirmedBy") : "Manager";
        AllocationResponse response = allocationService.confirmAllocation(id, confirmedBy);
        return ResponseEntity.ok(ApiResponse.ok("Resource allocation confirmed successfully", response));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AllocationResponse>> cancelAllocation(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = (body != null && body.containsKey("reason")) ? body.get("reason") : "Allocation cancelled";
        AllocationResponse response = allocationService.cancelAllocation(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Resource allocation cancelled successfully", response));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<AllocationResponse>> completeAllocation(@PathVariable Long id) {
        AllocationResponse response = allocationService.completeAllocation(id);
        return ResponseEntity.ok(ApiResponse.ok("Resource allocation completed successfully", response));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<AllocationResponse>> rejectAllocation(
            @PathVariable Long id,
            @Valid @RequestBody RejectAllocationRequest request) {
        AllocationResponse response = allocationService.rejectAllocation(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Resource allocation rejected successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AllocationResponse>> getAllocationById(@PathVariable Long id) {
        AllocationResponse response = allocationService.getAllocationById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<AllocationResponse>>> getAllocationsForEmployee(
            @PathVariable Long employeeId) {
        List<AllocationResponse> responses = allocationService.getAllocationsForEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/demand/{demandId}")
    public ResponseEntity<ApiResponse<List<AllocationResponse>>> getAllocationsForDemand(
            @PathVariable Long demandId) {
        List<AllocationResponse> responses = allocationService.getAllocationsForDemand(demandId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
