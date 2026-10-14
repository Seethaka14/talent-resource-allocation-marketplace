package com.talentmarketplace.dto.response.analytics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityDateRangeReportResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private int totalEmployeesEvaluated;
    private int fullyAvailableCount;
    private int partiallyAvailableCount;
    private int unavailableCount;
    private List<EmployeeAvailabilitySummary> employees = new ArrayList<>();

    public AvailabilityDateRangeReportResponse() {
    }

    public AvailabilityDateRangeReportResponse(LocalDate startDate, LocalDate endDate,
                                               int totalEmployeesEvaluated, int fullyAvailableCount,
                                               int partiallyAvailableCount, int unavailableCount,
                                               List<EmployeeAvailabilitySummary> employees) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalEmployeesEvaluated = totalEmployeesEvaluated;
        this.fullyAvailableCount = fullyAvailableCount;
        this.partiallyAvailableCount = partiallyAvailableCount;
        this.unavailableCount = unavailableCount;
        this.employees = employees != null ? employees : new ArrayList<>();
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

    public int getTotalEmployeesEvaluated() {
        return totalEmployeesEvaluated;
    }

    public void setTotalEmployeesEvaluated(int totalEmployeesEvaluated) {
        this.totalEmployeesEvaluated = totalEmployeesEvaluated;
    }

    public int getFullyAvailableCount() {
        return fullyAvailableCount;
    }

    public void setFullyAvailableCount(int fullyAvailableCount) {
        this.fullyAvailableCount = fullyAvailableCount;
    }

    public int getPartiallyAvailableCount() {
        return partiallyAvailableCount;
    }

    public void setPartiallyAvailableCount(int partiallyAvailableCount) {
        this.partiallyAvailableCount = partiallyAvailableCount;
    }

    public int getUnavailableCount() {
        return unavailableCount;
    }

    public void setUnavailableCount(int unavailableCount) {
        this.unavailableCount = unavailableCount;
    }

    public List<EmployeeAvailabilitySummary> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeAvailabilitySummary> employees) {
        this.employees = employees;
    }

    public static class EmployeeAvailabilitySummary {
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private String department;
        private String location;
        private int maxCapacityPercent;
        private int peakAllocatedPercent;
        private int remainingCapacityPercent;
        private boolean fullyAvailable;
        private boolean hasOverlappingUnavailability;
        private int overlappingAllocationsCount;
        private String statusReason;

        public EmployeeAvailabilitySummary() {
        }

        public EmployeeAvailabilitySummary(Long employeeId, String employeeCode, String employeeName,
                                           String department, String location, int maxCapacityPercent,
                                           int peakAllocatedPercent, int remainingCapacityPercent,
                                           boolean fullyAvailable, boolean hasOverlappingUnavailability,
                                           int overlappingAllocationsCount, String statusReason) {
            this.employeeId = employeeId;
            this.employeeCode = employeeCode;
            this.employeeName = employeeName;
            this.department = department;
            this.location = location;
            this.maxCapacityPercent = maxCapacityPercent;
            this.peakAllocatedPercent = peakAllocatedPercent;
            this.remainingCapacityPercent = remainingCapacityPercent;
            this.fullyAvailable = fullyAvailable;
            this.hasOverlappingUnavailability = hasOverlappingUnavailability;
            this.overlappingAllocationsCount = overlappingAllocationsCount;
            this.statusReason = statusReason;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }

        public void setEmployeeCode(String employeeCode) {
            this.employeeCode = employeeCode;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public int getMaxCapacityPercent() {
            return maxCapacityPercent;
        }

        public void setMaxCapacityPercent(int maxCapacityPercent) {
            this.maxCapacityPercent = maxCapacityPercent;
        }

        public int getPeakAllocatedPercent() {
            return peakAllocatedPercent;
        }

        public void setPeakAllocatedPercent(int peakAllocatedPercent) {
            this.peakAllocatedPercent = peakAllocatedPercent;
        }

        public int getRemainingCapacityPercent() {
            return remainingCapacityPercent;
        }

        public void setRemainingCapacityPercent(int remainingCapacityPercent) {
            this.remainingCapacityPercent = remainingCapacityPercent;
        }

        public boolean isFullyAvailable() {
            return fullyAvailable;
        }

        public void setFullyAvailable(boolean fullyAvailable) {
            this.fullyAvailable = fullyAvailable;
        }

        public boolean isHasOverlappingUnavailability() {
            return hasOverlappingUnavailability;
        }

        public void setHasOverlappingUnavailability(boolean hasOverlappingUnavailability) {
            this.hasOverlappingUnavailability = hasOverlappingUnavailability;
        }

        public int getOverlappingAllocationsCount() {
            return overlappingAllocationsCount;
        }

        public void setOverlappingAllocationsCount(int overlappingAllocationsCount) {
            this.overlappingAllocationsCount = overlappingAllocationsCount;
        }

        public String getStatusReason() {
            return statusReason;
        }

        public void setStatusReason(String statusReason) {
            this.statusReason = statusReason;
        }
    }
}
