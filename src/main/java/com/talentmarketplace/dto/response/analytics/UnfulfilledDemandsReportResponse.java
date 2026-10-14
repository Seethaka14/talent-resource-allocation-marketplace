package com.talentmarketplace.dto.response.analytics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.talentmarketplace.entity.DemandPriority;

public class UnfulfilledDemandsReportResponse {

    private int totalOpenDemandsEvaluated;
    private int unfulfillableDemandsCount;
    private List<UnfulfilledDemandSummary> unfulfillableDemands = new ArrayList<>();

    public UnfulfilledDemandsReportResponse() {
    }

    public UnfulfilledDemandsReportResponse(int totalOpenDemandsEvaluated, int unfulfillableDemandsCount,
                                            List<UnfulfilledDemandSummary> unfulfillableDemands) {
        this.totalOpenDemandsEvaluated = totalOpenDemandsEvaluated;
        this.unfulfillableDemandsCount = unfulfillableDemandsCount;
        this.unfulfillableDemands = unfulfillableDemands != null ? unfulfillableDemands : new ArrayList<>();
    }

    public int getTotalOpenDemandsEvaluated() {
        return totalOpenDemandsEvaluated;
    }

    public void setTotalOpenDemandsEvaluated(int totalOpenDemandsEvaluated) {
        this.totalOpenDemandsEvaluated = totalOpenDemandsEvaluated;
    }

    public int getUnfulfillableDemandsCount() {
        return unfulfillableDemandsCount;
    }

    public void setUnfulfillableDemandsCount(int unfulfillableDemandsCount) {
        this.unfulfillableDemandsCount = unfulfillableDemandsCount;
    }

    public List<UnfulfilledDemandSummary> getUnfulfillableDemands() {
        return unfulfillableDemands;
    }

    public void setUnfulfillableDemands(List<UnfulfilledDemandSummary> unfulfillableDemands) {
        this.unfulfillableDemands = unfulfillableDemands;
    }

    public static class UnfulfilledDemandSummary {
        private Long demandId;
        private String demandCode;
        private String projectName;
        private String role;
        private String department;
        private DemandPriority priority;
        private LocalDate startDate;
        private LocalDate endDate;
        private int allocationPercent;
        private int totalCandidatesEvaluated;
        private int eligibleCandidatesCount;
        private List<String> requiredSkills = new ArrayList<>();
        private String primaryReason;

        public UnfulfilledDemandSummary() {
        }

        public UnfulfilledDemandSummary(Long demandId, String demandCode, String projectName,
                                        String role, String department, DemandPriority priority,
                                        LocalDate startDate, LocalDate endDate, int allocationPercent,
                                        int totalCandidatesEvaluated, int eligibleCandidatesCount,
                                        List<String> requiredSkills, String primaryReason) {
            this.demandId = demandId;
            this.demandCode = demandCode;
            this.projectName = projectName;
            this.role = role;
            this.department = department;
            this.priority = priority;
            this.startDate = startDate;
            this.endDate = endDate;
            this.allocationPercent = allocationPercent;
            this.totalCandidatesEvaluated = totalCandidatesEvaluated;
            this.eligibleCandidatesCount = eligibleCandidatesCount;
            this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
            this.primaryReason = primaryReason;
        }

        public Long getDemandId() {
            return demandId;
        }

        public void setDemandId(Long demandId) {
            this.demandId = demandId;
        }

        public String getDemandCode() {
            return demandCode;
        }

        public void setDemandCode(String demandCode) {
            this.demandCode = demandCode;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public DemandPriority getPriority() {
            return priority;
        }

        public void setPriority(DemandPriority priority) {
            this.priority = priority;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public int getAllocationPercent() {
            return allocationPercent;
        }

        public void setAllocationPercent(int allocationPercent) {
            this.allocationPercent = allocationPercent;
        }

        public int getTotalCandidatesEvaluated() {
            return totalCandidatesEvaluated;
        }

        public void setTotalCandidatesEvaluated(int totalCandidatesEvaluated) {
            this.totalCandidatesEvaluated = totalCandidatesEvaluated;
        }

        public int getEligibleCandidatesCount() {
            return eligibleCandidatesCount;
        }

        public void setEligibleCandidatesCount(int eligibleCandidatesCount) {
            this.eligibleCandidatesCount = eligibleCandidatesCount;
        }

        public List<String> getRequiredSkills() {
            return requiredSkills;
        }

        public void setRequiredSkills(List<String> requiredSkills) {
            this.requiredSkills = requiredSkills;
        }

        public String getPrimaryReason() {
            return primaryReason;
        }

        public void setPrimaryReason(String primaryReason) {
            this.primaryReason = primaryReason;
        }
    }
}
