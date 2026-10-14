package com.talentmarketplace.dto.response.analytics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.talentmarketplace.entity.DemandPriority;

public class OpenDemandsReportResponse {

    private long totalOpenDemands;
    private long totalRequestedCapacityPercent;
    private List<RolePriorityDemandSummary> groups = new ArrayList<>();

    public OpenDemandsReportResponse() {
    }

    public OpenDemandsReportResponse(long totalOpenDemands, long totalRequestedCapacityPercent,
                                     List<RolePriorityDemandSummary> groups) {
        this.totalOpenDemands = totalOpenDemands;
        this.totalRequestedCapacityPercent = totalRequestedCapacityPercent;
        this.groups = groups != null ? groups : new ArrayList<>();
    }

    public long getTotalOpenDemands() {
        return totalOpenDemands;
    }

    public void setTotalOpenDemands(long totalOpenDemands) {
        this.totalOpenDemands = totalOpenDemands;
    }

    public long getTotalRequestedCapacityPercent() {
        return totalRequestedCapacityPercent;
    }

    public void setTotalRequestedCapacityPercent(long totalRequestedCapacityPercent) {
        this.totalRequestedCapacityPercent = totalRequestedCapacityPercent;
    }

    public List<RolePriorityDemandSummary> getGroups() {
        return groups;
    }

    public void setGroups(List<RolePriorityDemandSummary> groups) {
        this.groups = groups;
    }

    public static class RolePriorityDemandSummary {
        private String role;
        private DemandPriority priority;
        private long demandCount;
        private long totalAllocationPercent;
        private LocalDate earliestStartDate;
        private LocalDate latestEndDate;

        public RolePriorityDemandSummary() {
        }

        public RolePriorityDemandSummary(String role, DemandPriority priority, long demandCount,
                                         long totalAllocationPercent, LocalDate earliestStartDate,
                                         LocalDate latestEndDate) {
            this.role = role;
            this.priority = priority;
            this.demandCount = demandCount;
            this.totalAllocationPercent = totalAllocationPercent;
            this.earliestStartDate = earliestStartDate;
            this.latestEndDate = latestEndDate;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public DemandPriority getPriority() {
            return priority;
        }

        public void setPriority(DemandPriority priority) {
            this.priority = priority;
        }

        public long getDemandCount() {
            return demandCount;
        }

        public void setDemandCount(long demandCount) {
            this.demandCount = demandCount;
        }

        public long getTotalAllocationPercent() {
            return totalAllocationPercent;
        }

        public void setTotalAllocationPercent(long totalAllocationPercent) {
            this.totalAllocationPercent = totalAllocationPercent;
        }

        public LocalDate getEarliestStartDate() {
            return earliestStartDate;
        }

        public void setEarliestStartDate(LocalDate earliestStartDate) {
            this.earliestStartDate = earliestStartDate;
        }

        public LocalDate getLatestEndDate() {
            return latestEndDate;
        }

        public void setLatestEndDate(LocalDate latestEndDate) {
            this.latestEndDate = latestEndDate;
        }
    }
}
