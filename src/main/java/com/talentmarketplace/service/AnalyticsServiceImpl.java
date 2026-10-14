package com.talentmarketplace.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentmarketplace.dto.response.DemandRecommendationListResponse;
import com.talentmarketplace.dto.response.analytics.AvailabilityDateRangeReportResponse;
import com.talentmarketplace.dto.response.analytics.AvailabilityDateRangeReportResponse.EmployeeAvailabilitySummary;
import com.talentmarketplace.dto.response.analytics.OpenDemandsReportResponse;
import com.talentmarketplace.dto.response.analytics.OpenDemandsReportResponse.RolePriorityDemandSummary;
import com.talentmarketplace.dto.response.analytics.ResourcesBySkillReportResponse;
import com.talentmarketplace.dto.response.analytics.ResourcesBySkillReportResponse.SkillProficiencyResourceSummary;
import com.talentmarketplace.dto.response.analytics.SkillGapsReportResponse;
import com.talentmarketplace.dto.response.analytics.SkillGapsReportResponse.CategorySkillGapSummary;
import com.talentmarketplace.dto.response.analytics.SkillGapsReportResponse.SkillGapDetail;
import com.talentmarketplace.dto.response.analytics.UnfulfilledDemandsReportResponse;
import com.talentmarketplace.dto.response.analytics.UnfulfilledDemandsReportResponse.UnfulfilledDemandSummary;
import com.talentmarketplace.dto.response.analytics.UtilizationReportResponse;
import com.talentmarketplace.dto.response.analytics.UtilizationReportResponse.DepartmentUtilizationSummary;
import com.talentmarketplace.dto.response.analytics.UtilizationReportResponse.EmployeeUtilizationSummary;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandSkill;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeUnavailability;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.ResourceAllocation;
import com.talentmarketplace.entity.SkillCategory;
import com.talentmarketplace.exception.BusinessRuleValidationException;
import com.talentmarketplace.repository.AnalyticsRepository;
import com.talentmarketplace.repository.AnalyticsRepository.CandidateOpenDemandProjection;
import com.talentmarketplace.repository.AnalyticsRepository.DemandSkillRequirementProjection;
import com.talentmarketplace.repository.AnalyticsRepository.EmployeeSkillSupplyProjection;
import com.talentmarketplace.repository.AnalyticsRepository.RolePriorityDemandProjection;
import com.talentmarketplace.repository.AnalyticsRepository.SkillProficiencyResourceProjection;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final AnalyticsRepository analyticsRepository;
    private final EmployeeRepository employeeRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final EmployeeUnavailabilityRepository unavailabilityRepository;
    private final ProjectDemandRepository demandRepository;
    private final CandidateRecommendationService recommendationService;

    public AnalyticsServiceImpl(AnalyticsRepository analyticsRepository,
                                EmployeeRepository employeeRepository,
                                ResourceAllocationRepository allocationRepository,
                                EmployeeUnavailabilityRepository unavailabilityRepository,
                                ProjectDemandRepository demandRepository,
                                CandidateRecommendationService recommendationService) {
        this.analyticsRepository = analyticsRepository;
        this.employeeRepository = employeeRepository;
        this.allocationRepository = allocationRepository;
        this.unavailabilityRepository = unavailabilityRepository;
        this.demandRepository = demandRepository;
        this.recommendationService = recommendationService;
    }

    // =========================================================================
    // REPORT 1: Open Demands by Role and Priority
    // =========================================================================
    @Override
    public OpenDemandsReportResponse getOpenDemandsByRoleAndPriority() {
        log.info("Generating Report 1: Open Demands by Role and Priority");
        List<RolePriorityDemandProjection> projections = analyticsRepository.findOpenDemandsByRoleAndPriority();

        List<RolePriorityDemandSummary> groups = new ArrayList<>();
        long totalDemands = 0;
        long totalCapacity = 0;

        for (RolePriorityDemandProjection proj : projections) {
            DemandPriority priority = DemandPriority.valueOf(proj.getPriority().toUpperCase());
            long count = proj.getDemandCount() != null ? proj.getDemandCount() : 0L;
            long capacity = proj.getTotalAllocationPercent() != null ? proj.getTotalAllocationPercent() : 0L;

            groups.add(new RolePriorityDemandSummary(
                    proj.getRole(),
                    priority,
                    count,
                    capacity,
                    proj.getEarliestStartDate(),
                    proj.getLatestEndDate()
            ));

            totalDemands += count;
            totalCapacity += capacity;
        }

        return new OpenDemandsReportResponse(totalDemands, totalCapacity, groups);
    }

    // =========================================================================
    // REPORT 2: Resources by Skill and Proficiency
    // =========================================================================
    @Override
    public ResourcesBySkillReportResponse getResourcesBySkillAndProficiency(Long skillId, String category, String proficiencyLevel) {
        log.info("Generating Report 2: Resources by Skill and Proficiency (skillId={}, category={}, proficiencyLevel={})",
                skillId, category, proficiencyLevel);

        String normalizedCategory = null;
        if (category != null && !category.isBlank()) {
            try {
                normalizedCategory = SkillCategory.valueOf(category.trim().toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleValidationException("Invalid skill category: " + category);
            }
        }

        String normalizedProficiency = null;
        if (proficiencyLevel != null && !proficiencyLevel.isBlank()) {
            try {
                normalizedProficiency = ProficiencyLevel.valueOf(proficiencyLevel.trim().toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleValidationException("Invalid proficiency level: " + proficiencyLevel);
            }
        }

        List<SkillProficiencyResourceProjection> projections = analyticsRepository.findResourcesBySkillAndProficiency(
                skillId, normalizedCategory, normalizedProficiency
        );

        List<SkillProficiencyResourceSummary> items = new ArrayList<>();
        Set<Long> distinctSkillIds = new HashSet<>();
        long totalResourcesCount = 0;

        for (SkillProficiencyResourceProjection p : projections) {
            SkillCategory cat = SkillCategory.valueOf(p.getCategory());
            ProficiencyLevel prof = ProficiencyLevel.valueOf(p.getProficiencyLevel());
            long count = p.getResourceCount() != null ? p.getResourceCount() : 0L;
            double avgExp = p.getAvgYearsExperience() != null ? p.getAvgYearsExperience() : 0.0;

            items.add(new SkillProficiencyResourceSummary(
                    p.getSkillId(),
                    p.getSkillName(),
                    cat,
                    prof,
                    count,
                    avgExp
            ));

            distinctSkillIds.add(p.getSkillId());
            totalResourcesCount += count;
        }

        return new ResourcesBySkillReportResponse(totalResourcesCount, distinctSkillIds.size(), items);
    }

    // =========================================================================
    // REPORT 3: Availability for a Date Range
    // =========================================================================
    @Override
    public AvailabilityDateRangeReportResponse getAvailabilityForDateRange(LocalDate startDate, LocalDate endDate,
                                                                          String department, Integer minAvailablePercent) {
        log.info("Generating Report 3: Availability for Date Range [{} to {}], department={}",
                startDate, endDate, department);

        validateDateRange(startDate, endDate);

        if (minAvailablePercent != null && (minAvailablePercent < 0 || minAvailablePercent > 100)) {
            throw new BusinessRuleValidationException("minAvailablePercent must be between 0 and 100");
        }

        String deptFilter = (department != null && !department.isBlank()) ? department.trim() : null;
        List<Employee> employees = employeeRepository.searchEmployees(deptFilter, null, EmploymentStatus.ACTIVE, null);

        List<ResourceAllocation> allocations = allocationRepository.findAllOverlappingConfirmedAllocations(startDate, endDate);
        Map<Long, List<ResourceAllocation>> allocMap = allocations.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        List<EmployeeUnavailability> unavailabilities = unavailabilityRepository.findAllOverlappingUnavailabilities(startDate, endDate);
        Map<Long, List<EmployeeUnavailability>> unavailMap = unavailabilities.stream()
                .collect(Collectors.groupingBy(u -> u.getEmployee().getId()));

        List<EmployeeAvailabilitySummary> summaries = new ArrayList<>();
        int fullyAvailableCount = 0;
        int partiallyAvailableCount = 0;
        int unavailableCount = 0;

        for (Employee emp : employees) {
            int maxCap = emp.getMaxCapacityPercent() != null ? emp.getMaxCapacityPercent() : 100;
            List<EmployeeUnavailability> empLeaves = unavailMap.getOrDefault(emp.getId(), Collections.emptyList());
            boolean hasLeave = !empLeaves.isEmpty();

            List<ResourceAllocation> empAllocs = allocMap.getOrDefault(emp.getId(), Collections.emptyList());
            int peakAlloc = calculatePeakSimultaneousAllocation(empAllocs, startDate, endDate);
            int remainingCap = hasLeave ? 0 : Math.max(0, maxCap - peakAlloc);

            boolean fullyAvailable = !hasLeave && (peakAlloc == 0) && (remainingCap == maxCap);
            boolean unavailable = hasLeave || (remainingCap == 0);
            boolean partiallyAvailable = !hasLeave && !fullyAvailable && (remainingCap > 0);

            if (fullyAvailable) fullyAvailableCount++;
            else if (partiallyAvailable) partiallyAvailableCount++;
            else unavailableCount++;

            String statusReason;
            if (hasLeave) {
                EmployeeUnavailability firstLeave = empLeaves.get(0);
                statusReason = String.format("Scheduled unavailability (%s) from %s to %s",
                        firstLeave.getType(), firstLeave.getStartDate(), firstLeave.getEndDate());
            } else if (remainingCap == 0) {
                statusReason = String.format("Fully allocated (%d%% committed across %d active allocation(s))",
                        peakAlloc, empAllocs.size());
            } else if (peakAlloc > 0) {
                statusReason = String.format("Partially available (%d%% remaining capacity, %d%% committed)",
                        remainingCap, peakAlloc);
            } else {
                statusReason = String.format("Fully available (%d%% remaining capacity)", remainingCap);
            }

            if (minAvailablePercent == null || remainingCap >= minAvailablePercent) {
                summaries.add(new EmployeeAvailabilitySummary(
                        emp.getId(),
                        emp.getEmployeeCode(),
                        emp.getName(),
                        emp.getDepartment(),
                        emp.getLocation(),
                        maxCap,
                        peakAlloc,
                        remainingCap,
                        fullyAvailable,
                        hasLeave,
                        empAllocs.size(),
                        statusReason
                ));
            }
        }

        return new AvailabilityDateRangeReportResponse(
                startDate,
                endDate,
                employees.size(),
                fullyAvailableCount,
                partiallyAvailableCount,
                unavailableCount,
                summaries
        );
    }

    // =========================================================================
    // REPORT 4: Utilization by Employee and Department
    // =========================================================================
    @Override
    public UtilizationReportResponse getUtilizationByEmployeeAndDepartment(LocalDate startDate, LocalDate endDate,
                                                                           String department) {
        log.info("Generating Report 4: Utilization by Employee and Department [{} to {}], department={}",
                startDate, endDate, department);

        validateDateRange(startDate, endDate);
        int windowDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        String deptFilter = (department != null && !department.isBlank()) ? department.trim() : null;
        List<Employee> employees = employeeRepository.searchEmployees(deptFilter, null, EmploymentStatus.ACTIVE, null);

        List<ResourceAllocation> allocations = allocationRepository.findAllOverlappingConfirmedAllocations(startDate, endDate);
        Map<Long, List<ResourceAllocation>> allocMap = allocations.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        List<EmployeeUtilizationSummary> employeeSummaries = new ArrayList<>();
        double totalPeakUtil = 0.0;
        double totalTimeWeightedUtil = 0.0;

        for (Employee emp : employees) {
            int maxCap = emp.getMaxCapacityPercent() != null ? emp.getMaxCapacityPercent() : 100;
            List<ResourceAllocation> empAllocs = allocMap.getOrDefault(emp.getId(), Collections.emptyList());

            int peakAlloc = calculatePeakSimultaneousAllocation(empAllocs, startDate, endDate);
            double peakUtilPercent = Math.min(100.0, Math.round(((double) peakAlloc / maxCap * 100.0) * 10.0) / 10.0);

            // Compute time-weighted average across window days
            long personPercentDays = 0;
            for (int d = 0; d < windowDays; d++) {
                LocalDate day = startDate.plusDays(d);
                int dayAlloc = 0;
                for (ResourceAllocation a : empAllocs) {
                    if (!day.isBefore(a.getStartDate()) && !day.isAfter(a.getEndDate())) {
                        dayAlloc += a.getAllocationPercent();
                    }
                }
                personPercentDays += dayAlloc;
            }

            double avgDailyAlloc = (double) personPercentDays / windowDays;
            double timeWeightedUtilPercent = Math.min(100.0, Math.round(((avgDailyAlloc / maxCap) * 100.0) * 10.0) / 10.0);

            totalPeakUtil += peakUtilPercent;
            totalTimeWeightedUtil += timeWeightedUtilPercent;

            employeeSummaries.add(new EmployeeUtilizationSummary(
                    emp.getId(),
                    emp.getEmployeeCode(),
                    emp.getName(),
                    emp.getDepartment(),
                    maxCap,
                    peakAlloc,
                    peakUtilPercent,
                    timeWeightedUtilPercent,
                    empAllocs.size(),
                    !empAllocs.isEmpty()
            ));
        }

        // Aggregate by department
        Map<String, List<EmployeeUtilizationSummary>> byDept = employeeSummaries.stream()
                .collect(Collectors.groupingBy(EmployeeUtilizationSummary::getDepartment));

        List<DepartmentUtilizationSummary> deptSummaries = new ArrayList<>();
        for (Map.Entry<String, List<EmployeeUtilizationSummary>> entry : byDept.entrySet()) {
            List<EmployeeUtilizationSummary> deptList = entry.getValue();
            double deptPeakAvg = deptList.stream().mapToDouble(EmployeeUtilizationSummary::getPeakUtilizationPercent).average().orElse(0.0);
            double deptTimeAvg = deptList.stream().mapToDouble(EmployeeUtilizationSummary::getTimeWeightedAverageUtilizationPercent).average().orElse(0.0);

            deptSummaries.add(new DepartmentUtilizationSummary(
                    entry.getKey(),
                    deptList.size(),
                    Math.round(deptPeakAvg * 10.0) / 10.0,
                    Math.round(deptTimeAvg * 10.0) / 10.0
            ));
        }

        double overallPeakAvg = employeeSummaries.isEmpty() ? 0.0 : Math.round((totalPeakUtil / employeeSummaries.size()) * 10.0) / 10.0;
        double overallTimeAvg = employeeSummaries.isEmpty() ? 0.0 : Math.round((totalTimeWeightedUtil / employeeSummaries.size()) * 10.0) / 10.0;

        return new UtilizationReportResponse(
                startDate,
                endDate,
                windowDays,
                employees.size(),
                overallPeakAvg,
                overallTimeAvg,
                deptSummaries,
                employeeSummaries
        );
    }

    // =========================================================================
    // REPORT 5: Demands with No Eligible Candidate
    // =========================================================================
    @Override
    public UnfulfilledDemandsReportResponse getDemandsWithNoEligibleCandidate() {
        log.info("Generating Report 5: Demands with No Eligible Candidate");
        List<CandidateOpenDemandProjection> openDemands = analyticsRepository.findOpenDemandsForEligibilityAssessment();

        List<UnfulfilledDemandSummary> unfulfillableDemands = new ArrayList<>();

        for (CandidateOpenDemandProjection proj : openDemands) {
            DemandRecommendationListResponse recResponse = recommendationService.recommendCandidatesForDemand(proj.getDemandId());

            if (recResponse.getEligibleCandidatesCount() == 0) {
                // Fetch full demand entity to get skill names accurately
                ProjectDemand demand = demandRepository.findByIdWithSkills(proj.getDemandId()).orElse(null);
                List<String> requiredSkillNames = demand != null ?
                        demand.getRequiredSkills().stream()
                                .map(ds -> ds.getSkill().getName() + " (" + ds.getMinimumProficiency() + (ds.isMandatory() ? ", Mandatory" : "") + ")")
                                .collect(Collectors.toList()) :
                        Collections.emptyList();

                String reason;
                if (recResponse.getTotalCandidatesEvaluated() == 0) {
                    reason = "No active employees exist in the organization.";
                } else if (recResponse.getCandidates().stream().allMatch(c -> c.getMissingMandatorySkills() != null && !c.getMissingMandatorySkills().isEmpty())) {
                    reason = "No active employees possess the required mandatory skill(s) at or above the minimum proficiency level.";
                } else {
                    reason = "Candidate(s) possess required skills, but fail availability or capacity gates (scheduled leave, overlapping allocations, or capacity limit).";
                }

                DemandPriority priority = DemandPriority.valueOf(proj.getPriority().toUpperCase());

                unfulfillableDemands.add(new UnfulfilledDemandSummary(
                        proj.getDemandId(),
                        proj.getDemandCode(),
                        proj.getProjectName(),
                        proj.getRole(),
                        proj.getDepartment(),
                        priority,
                        proj.getStartDate(),
                        proj.getEndDate(),
                        proj.getAllocationPercent() != null ? proj.getAllocationPercent() : 100,
                        recResponse.getTotalCandidatesEvaluated(),
                        recResponse.getEligibleCandidatesCount(),
                        requiredSkillNames,
                        reason
                ));
            }
        }

        return new UnfulfilledDemandsReportResponse(
                openDemands.size(),
                unfulfillableDemands.size(),
                unfulfillableDemands
        );
    }

    // =========================================================================
    // REPORT 6: Skill Gaps by Category
    // =========================================================================
    @Override
    public SkillGapsReportResponse getSkillGapsByCategory(String category) {
        log.info("Generating Report 6: Skill Gaps by Category (category={})", category);

        String normalizedCategory = null;
        if (category != null && !category.isBlank()) {
            try {
                normalizedCategory = SkillCategory.valueOf(category.trim().toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleValidationException("Invalid skill category: " + category);
            }
        }

        List<DemandSkillRequirementProjection> demandProjections = analyticsRepository.findOpenDemandSkillRequirements(normalizedCategory);
        List<EmployeeSkillSupplyProjection> supplyProjections = analyticsRepository.findActiveEmployeeSkillSupply(normalizedCategory);

        // Build qualified supply map: (skillId) -> Map<ProficiencyLevel, Long resourceCount>
        Map<Long, Map<ProficiencyLevel, Long>> supplyMap = new HashMap<>();
        for (EmployeeSkillSupplyProjection sp : supplyProjections) {
            ProficiencyLevel level = ProficiencyLevel.valueOf(sp.getProficiencyLevel());
            long count = sp.getResourceCount() != null ? sp.getResourceCount() : 0L;
            supplyMap.computeIfAbsent(sp.getSkillId(), k -> new HashMap<>()).put(level, count);
        }

        // Group demand requirements by category
        Map<SkillCategory, List<SkillGapDetail>> detailsByCategory = new HashMap<>();
        Map<SkillCategory, Set<Long>> distinctDemandsByCategory = new HashMap<>();

        // Also fetch distinct demands for each category
        for (DemandSkillRequirementProjection dp : demandProjections) {
            SkillCategory cat = SkillCategory.valueOf(dp.getCategory());
            ProficiencyLevel reqLevel = ProficiencyLevel.valueOf(dp.getMinimumProficiency());
            int demandCount = dp.getDemandCount() != null ? dp.getDemandCount().intValue() : 0;
            int mandatoryCount = dp.getMandatoryCount() != null ? dp.getMandatoryCount().intValue() : 0;
            int optionalCount = dp.getOptionalCount() != null ? dp.getOptionalCount().intValue() : 0;

            // Calculate qualified supply count: employee proficiency level >= reqLevel
            Map<ProficiencyLevel, Long> skillSupply = supplyMap.getOrDefault(dp.getSkillId(), Collections.emptyMap());
            long qualifiedSupply = 0;
            for (Map.Entry<ProficiencyLevel, Long> entry : skillSupply.entrySet()) {
                if (getProficiencyRank(entry.getKey()) >= getProficiencyRank(reqLevel)) {
                    qualifiedSupply += entry.getValue();
                }
            }

            int deficit = Math.max(0, demandCount - (int) qualifiedSupply);

            detailsByCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(new SkillGapDetail(
                    dp.getSkillId(),
                    dp.getSkillName(),
                    reqLevel,
                    demandCount,
                    mandatoryCount,
                    optionalCount,
                    (int) qualifiedSupply,
                    deficit
            ));
        }

        List<CategorySkillGapSummary> categorySummaries = new ArrayList<>();
        int totalUnmetSlots = 0;
        int categoriesWithGaps = 0;

        for (Map.Entry<SkillCategory, List<SkillGapDetail>> entry : detailsByCategory.entrySet()) {
            SkillCategory cat = entry.getKey();
            List<SkillGapDetail> details = entry.getValue();

            int catDemandSlots = details.stream().mapToInt(SkillGapDetail::getDemandCount).sum();
            int catQualifiedSupply = details.stream().mapToInt(SkillGapDetail::getQualifiedResourceCount).sum();
            int catNetDeficit = details.stream().mapToInt(SkillGapDetail::getDeficit).sum();

            if (catNetDeficit > 0) {
                categoriesWithGaps++;
            }
            totalUnmetSlots += catNetDeficit;

            categorySummaries.add(new CategorySkillGapSummary(
                    cat,
                    details.size(),
                    catDemandSlots,
                    catQualifiedSupply,
                    catNetDeficit,
                    details
            ));
        }

        // Sort by net deficit descending
        categorySummaries.sort((a, b) -> Integer.compare(b.getNetDeficit(), a.getNetDeficit()));

        return new SkillGapsReportResponse(
                categorySummaries.size(),
                categoriesWithGaps,
                totalUnmetSlots,
                categorySummaries
        );
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessRuleValidationException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessRuleValidationException("Start date cannot be after end date");
        }
    }

    private int calculatePeakSimultaneousAllocation(List<ResourceAllocation> allocations, LocalDate windowStart, LocalDate windowEnd) {
        if (allocations == null || allocations.isEmpty()) {
            return 0;
        }

        Set<LocalDate> criticalDates = new TreeSet<>();
        criticalDates.add(windowStart);

        for (ResourceAllocation a : allocations) {
            if (!a.getStartDate().isBefore(windowStart) && !a.getStartDate().isAfter(windowEnd)) {
                criticalDates.add(a.getStartDate());
            }
            LocalDate dayAfterEnd = a.getEndDate().plusDays(1);
            if (!dayAfterEnd.isBefore(windowStart) && !dayAfterEnd.isAfter(windowEnd)) {
                criticalDates.add(dayAfterEnd);
            }
        }

        int peak = 0;
        for (LocalDate date : criticalDates) {
            int currentOnDate = 0;
            for (ResourceAllocation a : allocations) {
                if (!date.isBefore(a.getStartDate()) && !date.isAfter(a.getEndDate())) {
                    currentOnDate += a.getAllocationPercent();
                }
            }
            if (currentOnDate > peak) {
                peak = currentOnDate;
            }
        }
        return peak;
    }

    private int getProficiencyRank(ProficiencyLevel level) {
        if (level == null) return 0;
        return switch (level) {
            case BEGINNER -> 1;
            case INTERMEDIATE -> 2;
            case ADVANCED -> 3;
        };
    }
}
