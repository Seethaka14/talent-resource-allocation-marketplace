package com.talentmarketplace.dto.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.talentmarketplace.entity.EmploymentStatus;

public class AvailabilityAssessmentResponse {

    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private EmploymentStatus employmentStatus;
    private Integer maxCapacityPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer requestedAllocationPercent;
    private boolean available;
    private Integer remainingCapacityPercent;
    private List<UnavailabilityResponse> overlappingUnavailabilities = new ArrayList<>();
    private String reason;

    public AvailabilityAssessmentResponse() {
    }

    public AvailabilityAssessmentResponse(Long employeeId, String employeeCode, String employeeName,
                                          EmploymentStatus employmentStatus, Integer maxCapacityPercent,
                                          LocalDate startDate, LocalDate endDate,
                                          Integer requestedAllocationPercent, boolean available,
                                          Integer remainingCapacityPercent,
                                          List<UnavailabilityResponse> overlappingUnavailabilities,
                                          String reason) {
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.employmentStatus = employmentStatus;
        this.maxCapacityPercent = maxCapacityPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.requestedAllocationPercent = requestedAllocationPercent;
        this.available = available;
        this.remainingCapacityPercent = remainingCapacityPercent;
        this.overlappingUnavailabilities = overlappingUnavailabilities != null ? overlappingUnavailabilities : new ArrayList<>();
        this.reason = reason;
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

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public Integer getMaxCapacityPercent() {
        return maxCapacityPercent;
    }

    public void setMaxCapacityPercent(Integer maxCapacityPercent) {
        this.maxCapacityPercent = maxCapacityPercent;
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

    public Integer getRequestedAllocationPercent() {
        return requestedAllocationPercent;
    }

    public void setRequestedAllocationPercent(Integer requestedAllocationPercent) {
        this.requestedAllocationPercent = requestedAllocationPercent;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Integer getRemainingCapacityPercent() {
        return remainingCapacityPercent;
    }

    public void setRemainingCapacityPercent(Integer remainingCapacityPercent) {
        this.remainingCapacityPercent = remainingCapacityPercent;
    }

    public List<UnavailabilityResponse> getOverlappingUnavailabilities() {
        return overlappingUnavailabilities;
    }

    public void setOverlappingUnavailabilities(List<UnavailabilityResponse> overlappingUnavailabilities) {
        this.overlappingUnavailabilities = overlappingUnavailabilities;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
