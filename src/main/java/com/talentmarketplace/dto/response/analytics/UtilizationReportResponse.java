package com.talentmarketplace.dto.response.analytics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UtilizationReportResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private int windowDays;
    private int totalEmployees;
    private double averagePeakUtilizationPercent;
    private double averageTimeWeightedUtilizationPercent;
    private List<DepartmentUtilizationSummary> departmentSummaries = new ArrayList<>();
    private List<EmployeeUtilizationSummary> employeeDetails = new ArrayList<>();

    public UtilizationReportResponse() {
    }

    public UtilizationReportResponse(LocalDate startDate, LocalDate endDate, int windowDays,
                                     int totalEmployees, double averagePeakUtilizationPercent,
                                     double averageTimeWeightedUtilizationPercent,
                                     List<DepartmentUtilizationSummary> departmentSummaries,
                                     List<EmployeeUtilizationSummary> employeeDetails) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.windowDays = windowDays;
        this.totalEmployees = totalEmployees;
        this.averagePeakUtilizationPercent = averagePeakUtilizationPercent;
        this.averageTimeWeightedUtilizationPercent = averageTimeWeightedUtilizationPercent;
        this.departmentSummaries = departmentSummaries != null ? departmentSummaries : new ArrayList<>();
        this.employeeDetails = employeeDetails != null ? employeeDetails : new ArrayList<>();
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

    public int getWindowDays() {
        return windowDays;
    }

    public void setWindowDays(int windowDays) {
        this.windowDays = windowDays;
    }

    public int getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(int totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public double getAveragePeakUtilizationPercent() {
        return averagePeakUtilizationPercent;
    }

    public void setAveragePeakUtilizationPercent(double averagePeakUtilizationPercent) {
        this.averagePeakUtilizationPercent = averagePeakUtilizationPercent;
    }

    public double getAverageTimeWeightedUtilizationPercent() {
        return averageTimeWeightedUtilizationPercent;
    }

    public void setAverageTimeWeightedUtilizationPercent(double averageTimeWeightedUtilizationPercent) {
        this.averageTimeWeightedUtilizationPercent = averageTimeWeightedUtilizationPercent;
    }

    public List<DepartmentUtilizationSummary> getDepartmentSummaries() {
        return departmentSummaries;
    }

    public void setDepartmentSummaries(List<DepartmentUtilizationSummary> departmentSummaries) {
        this.departmentSummaries = departmentSummaries;
    }

    public List<EmployeeUtilizationSummary> getEmployeeDetails() {
        return employeeDetails;
    }

    public void setEmployeeDetails(List<EmployeeUtilizationSummary> employeeDetails) {
        this.employeeDetails = employeeDetails;
    }

    public static class DepartmentUtilizationSummary {
        private String department;
        private int employeeCount;
        private double averagePeakUtilizationPercent;
        private double averageTimeWeightedUtilizationPercent;

        public DepartmentUtilizationSummary() {
        }

        public DepartmentUtilizationSummary(String department, int employeeCount,
                                            double averagePeakUtilizationPercent,
                                            double averageTimeWeightedUtilizationPercent) {
            this.department = department;
            this.employeeCount = employeeCount;
            this.averagePeakUtilizationPercent = averagePeakUtilizationPercent;
            this.averageTimeWeightedUtilizationPercent = averageTimeWeightedUtilizationPercent;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public int getEmployeeCount() {
            return employeeCount;
        }

        public void setEmployeeCount(int employeeCount) {
            this.employeeCount = employeeCount;
        }

        public double getAveragePeakUtilizationPercent() {
            return averagePeakUtilizationPercent;
        }

        public void setAveragePeakUtilizationPercent(double averagePeakUtilizationPercent) {
            this.averagePeakUtilizationPercent = averagePeakUtilizationPercent;
        }

        public double getAverageTimeWeightedUtilizationPercent() {
            return averageTimeWeightedUtilizationPercent;
        }

        public void setAverageTimeWeightedUtilizationPercent(double averageTimeWeightedUtilizationPercent) {
            this.averageTimeWeightedUtilizationPercent = averageTimeWeightedUtilizationPercent;
        }
    }

    public static class EmployeeUtilizationSummary {
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private String department;
        private int maxCapacityPercent;
        private int peakSimultaneousAllocationPercent;
        private double peakUtilizationPercent;
        private double timeWeightedAverageUtilizationPercent;
        private int aggregateConfirmedAllocationsCount;
        private boolean hasConfirmedAllocations;

        public EmployeeUtilizationSummary() {
        }

        public EmployeeUtilizationSummary(Long employeeId, String employeeCode, String employeeName,
                                          String department, int maxCapacityPercent,
                                          int peakSimultaneousAllocationPercent,
                                          double peakUtilizationPercent,
                                          double timeWeightedAverageUtilizationPercent,
                                          int aggregateConfirmedAllocationsCount,
                                          boolean hasConfirmedAllocations) {
            this.employeeId = employeeId;
            this.employeeCode = employeeCode;
            this.employeeName = employeeName;
            this.department = department;
            this.maxCapacityPercent = maxCapacityPercent;
            this.peakSimultaneousAllocationPercent = peakSimultaneousAllocationPercent;
            this.peakUtilizationPercent = peakUtilizationPercent;
            this.timeWeightedAverageUtilizationPercent = timeWeightedAverageUtilizationPercent;
            this.aggregateConfirmedAllocationsCount = aggregateConfirmedAllocationsCount;
            this.hasConfirmedAllocations = hasConfirmedAllocations;
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

        public int getMaxCapacityPercent() {
            return maxCapacityPercent;
        }

        public void setMaxCapacityPercent(int maxCapacityPercent) {
            this.maxCapacityPercent = maxCapacityPercent;
        }

        public int getPeakSimultaneousAllocationPercent() {
            return peakSimultaneousAllocationPercent;
        }

        public void setPeakSimultaneousAllocationPercent(int peakSimultaneousAllocationPercent) {
            this.peakSimultaneousAllocationPercent = peakSimultaneousAllocationPercent;
        }

        public double getPeakUtilizationPercent() {
            return peakUtilizationPercent;
        }

        public void setPeakUtilizationPercent(double peakUtilizationPercent) {
            this.peakUtilizationPercent = peakUtilizationPercent;
        }

        public double getTimeWeightedAverageUtilizationPercent() {
            return timeWeightedAverageUtilizationPercent;
        }

        public void setTimeWeightedAverageUtilizationPercent(double timeWeightedAverageUtilizationPercent) {
            this.timeWeightedAverageUtilizationPercent = timeWeightedAverageUtilizationPercent;
        }

        public int getAggregateConfirmedAllocationsCount() {
            return aggregateConfirmedAllocationsCount;
        }

        public void setAggregateConfirmedAllocationsCount(int aggregateConfirmedAllocationsCount) {
            this.aggregateConfirmedAllocationsCount = aggregateConfirmedAllocationsCount;
        }

        public boolean isHasConfirmedAllocations() {
            return hasConfirmedAllocations;
        }

        public void setHasConfirmedAllocations(boolean hasConfirmedAllocations) {
            this.hasConfirmedAllocations = hasConfirmedAllocations;
        }
    }
}
