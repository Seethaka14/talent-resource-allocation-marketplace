package com.talentmarketplace.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentmarketplace.dto.response.ApiResponse;
import com.talentmarketplace.dto.response.analytics.AvailabilityDateRangeReportResponse;
import com.talentmarketplace.dto.response.analytics.OpenDemandsReportResponse;
import com.talentmarketplace.dto.response.analytics.ResourcesBySkillReportResponse;
import com.talentmarketplace.dto.response.analytics.SkillGapsReportResponse;
import com.talentmarketplace.dto.response.analytics.UnfulfilledDemandsReportResponse;
import com.talentmarketplace.dto.response.analytics.UtilizationReportResponse;
import com.talentmarketplace.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Report 1: Open Demands by Role and Priority
     */
    @GetMapping({"/reports/open-demands-by-role-priority", "/open-demands"})
    public ResponseEntity<ApiResponse<OpenDemandsReportResponse>> getOpenDemandsByRoleAndPriority() {
        OpenDemandsReportResponse report = analyticsService.getOpenDemandsByRoleAndPriority();
        return ResponseEntity.ok(ApiResponse.ok("Open demands report retrieved successfully", report));
    }

    /**
     * Report 2: Resources by Skill and Proficiency
     */
    @GetMapping({"/reports/resources-by-skill-proficiency", "/resources-by-skill"})
    public ResponseEntity<ApiResponse<ResourcesBySkillReportResponse>> getResourcesBySkillAndProficiency(
            @RequestParam(required = false) Long skillId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String proficiencyLevel) {
        ResourcesBySkillReportResponse report = analyticsService.getResourcesBySkillAndProficiency(skillId, category, proficiencyLevel);
        return ResponseEntity.ok(ApiResponse.ok("Resources by skill and proficiency report retrieved successfully", report));
    }

    /**
     * Report 3: Availability for a Date Range
     */
    @GetMapping({"/reports/availability-date-range", "/availability"})
    public ResponseEntity<ApiResponse<AvailabilityDateRangeReportResponse>> getAvailabilityForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer minAvailablePercent) {
        AvailabilityDateRangeReportResponse report = analyticsService.getAvailabilityForDateRange(startDate, endDate, department, minAvailablePercent);
        return ResponseEntity.ok(ApiResponse.ok("Availability report retrieved successfully", report));
    }

    /**
     * Report 4: Utilization by Employee and Department
     */
    @GetMapping({"/reports/utilization-by-employee-department", "/utilization"})
    public ResponseEntity<ApiResponse<UtilizationReportResponse>> getUtilizationByEmployeeAndDepartment(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String department) {
        UtilizationReportResponse report = analyticsService.getUtilizationByEmployeeAndDepartment(startDate, endDate, department);
        return ResponseEntity.ok(ApiResponse.ok("Utilization report retrieved successfully", report));
    }

    /**
     * Report 5: Demands with No Eligible Candidate
     */
    @GetMapping({"/reports/demands-with-no-eligible-candidate", "/unfulfilled-demands"})
    public ResponseEntity<ApiResponse<UnfulfilledDemandsReportResponse>> getDemandsWithNoEligibleCandidate() {
        UnfulfilledDemandsReportResponse report = analyticsService.getDemandsWithNoEligibleCandidate();
        return ResponseEntity.ok(ApiResponse.ok("Unfulfilled demands report retrieved successfully", report));
    }

    /**
     * Report 6: Skill Gaps by Category
     */
    @GetMapping({"/reports/skill-gaps-by-category", "/skill-gaps"})
    public ResponseEntity<ApiResponse<SkillGapsReportResponse>> getSkillGapsByCategory(
            @RequestParam(required = false) String category) {
        SkillGapsReportResponse report = analyticsService.getSkillGapsByCategory(category);
        return ResponseEntity.ok(ApiResponse.ok("Skill gaps report retrieved successfully", report));
    }
}
