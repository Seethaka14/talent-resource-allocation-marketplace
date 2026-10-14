package com.talentmarketplace.service;

import java.time.LocalDate;

import com.talentmarketplace.dto.response.analytics.AvailabilityDateRangeReportResponse;
import com.talentmarketplace.dto.response.analytics.OpenDemandsReportResponse;
import com.talentmarketplace.dto.response.analytics.ResourcesBySkillReportResponse;
import com.talentmarketplace.dto.response.analytics.SkillGapsReportResponse;
import com.talentmarketplace.dto.response.analytics.UnfulfilledDemandsReportResponse;
import com.talentmarketplace.dto.response.analytics.UtilizationReportResponse;

public interface AnalyticsService {

    /**
     * Report 1: Aggregates open demands grouped by role and priority.
     */
    OpenDemandsReportResponse getOpenDemandsByRoleAndPriority();

    /**
     * Report 2: Aggregates active resources possessing skills at each proficiency level.
     */
    ResourcesBySkillReportResponse getResourcesBySkillAndProficiency(Long skillId, String category, String proficiencyLevel);

    /**
     * Report 3: Assesses resource availability across an inclusive date range [startDate, endDate],
     * respecting maximum capacity, confirmed overlapping allocations, and employee leaves.
     */
    AvailabilityDateRangeReportResponse getAvailabilityForDateRange(LocalDate startDate, LocalDate endDate,
                                                                   String department, Integer minAvailablePercent);

    /**
     * Report 4: Measures employee and departmental utilization across an evaluation window [startDate, endDate],
     * distinguishing peak simultaneous allocation from time-weighted average utilization and including zero-allocation employees.
     */
    UtilizationReportResponse getUtilizationByEmployeeAndDepartment(LocalDate startDate, LocalDate endDate,
                                                                    String department);

    /**
     * Report 5: Identifies open project demands for which no active candidate meets all deterministic eligibility gates.
     */
    UnfulfilledDemandsReportResponse getDemandsWithNoEligibleCandidate();

    /**
     * Report 6: Aggregates skill requirements by category and compares against active qualified employee supply.
     */
    SkillGapsReportResponse getSkillGapsByCategory(String category);
}
