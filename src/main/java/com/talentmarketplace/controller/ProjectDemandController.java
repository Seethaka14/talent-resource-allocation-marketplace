package com.talentmarketplace.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.talentmarketplace.dto.request.CreateDemandRequest;
import com.talentmarketplace.dto.request.DemandSkillRequest;
import com.talentmarketplace.dto.request.UpdateDemandRequest;
import com.talentmarketplace.dto.response.ApiResponse;
import com.talentmarketplace.dto.response.DemandResponse;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.service.ProjectDemandService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/demands")
public class ProjectDemandController {

    private final ProjectDemandService demandService;

    public ProjectDemandController(ProjectDemandService demandService) {
        this.demandService = demandService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DemandResponse>> createDemand(
            @Valid @RequestBody CreateDemandRequest request) {
        DemandResponse response = demandService.createDemand(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.ok("Project demand created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DemandResponse>>> searchDemands(
            @RequestParam(required = false) DemandStatus status,
            @RequestParam(required = false) DemandPriority priority,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String search) {
        List<DemandResponse> responses = demandService.searchDemands(
                status, priority, role, location, fromDate, toDate, search
        );
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DemandResponse>> getDemandById(@PathVariable Long id) {
        DemandResponse response = demandService.getDemandById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/code/{demandCode}")
    public ResponseEntity<ApiResponse<DemandResponse>> getDemandByCode(@PathVariable String demandCode) {
        DemandResponse response = demandService.getDemandByCode(demandCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DemandResponse>> updateDemand(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDemandRequest request) {
        DemandResponse response = demandService.updateDemand(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Project demand updated successfully", response));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<DemandResponse>> cancelDemand(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = (body != null && body.containsKey("reason")) ? body.get("reason") : "Demand cancelled";
        DemandResponse response = demandService.cancelDemand(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Project demand cancelled successfully", response));
    }

    @PostMapping("/{id}/skills")
    public ResponseEntity<ApiResponse<DemandResponse>> addRequiredSkill(
            @PathVariable Long id,
            @Valid @RequestBody DemandSkillRequest request) {
        DemandResponse response = demandService.addRequiredSkill(id, request);
        return ResponseEntity.created(URI.create("/api/demands/" + id))
                .body(ApiResponse.ok("Required skill added to demand", response));
    }

    @DeleteMapping("/{id}/skills/{skillId}")
    public ResponseEntity<Void> removeRequiredSkill(
            @PathVariable Long id,
            @PathVariable Long skillId) {
        demandService.removeRequiredSkill(id, skillId);
        return ResponseEntity.noContent().build();
    }
}
