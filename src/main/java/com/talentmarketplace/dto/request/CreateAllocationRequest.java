package com.talentmarketplace.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateAllocationRequest {

    @NotNull(message = "Demand ID is required")
    private Long demandId;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Allocation percent is required")
    @Min(value = 1, message = "Allocation percent must be at least 1")
    @Max(value = 100, message = "Allocation percent cannot exceed 100")
    private Integer allocationPercent;

    @Size(max = 100, message = "Allocated by cannot exceed 100 characters")
    private String allocatedBy;

    private boolean autoConfirm = false;

    public CreateAllocationRequest() {
    }

    public CreateAllocationRequest(Long demandId, Long employeeId, LocalDate startDate,
                                   LocalDate endDate, Integer allocationPercent, String allocatedBy) {
        this.demandId = demandId;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.allocationPercent = allocationPercent;
        this.allocatedBy = allocatedBy;
        this.autoConfirm = false;
    }

    public CreateAllocationRequest(Long demandId, Long employeeId, LocalDate startDate,
                                   LocalDate endDate, Integer allocationPercent, String allocatedBy,
                                   boolean autoConfirm) {
        this.demandId = demandId;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.allocationPercent = allocationPercent;
        this.allocatedBy = allocatedBy;
        this.autoConfirm = autoConfirm;
    }

    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
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

    public Integer getAllocationPercent() {
        return allocationPercent;
    }

    public void setAllocationPercent(Integer allocationPercent) {
        this.allocationPercent = allocationPercent;
    }

    public String getAllocatedBy() {
        return allocatedBy;
    }

    public void setAllocatedBy(String allocatedBy) {
        this.allocatedBy = allocatedBy;
    }

    public boolean isAutoConfirm() {
        return autoConfirm;
    }

    public void setAutoConfirm(boolean autoConfirm) {
        this.autoConfirm = autoConfirm;
    }
}
