package com.talentmarketplace.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.talentmarketplace.entity.AllocationStatus;
import com.talentmarketplace.entity.ResourceAllocation;

public class AllocationResponse {

    private Long id;
    private Long demandId;
    private String demandCode;
    private String projectName;
    private String role;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String department;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer allocationPercent;
    private AllocationStatus status;
    private String rejectionReason;
    private String allocatedBy;
    private String confirmedBy;
    private LocalDateTime confirmedAt;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AllocationResponse() {
    }

    public static AllocationResponse fromEntity(ResourceAllocation a) {
        AllocationResponse res = new AllocationResponse();
        res.setId(a.getId());
        if (a.getDemand() != null) {
            res.setDemandId(a.getDemand().getId());
            res.setDemandCode(a.getDemand().getDemandCode());
            res.setProjectName(a.getDemand().getProjectName());
            res.setRole(a.getDemand().getRole());
        }
        if (a.getEmployee() != null) {
            res.setEmployeeId(a.getEmployee().getId());
            res.setEmployeeCode(a.getEmployee().getEmployeeCode());
            res.setEmployeeName(a.getEmployee().getName());
            res.setDepartment(a.getEmployee().getDepartment());
            res.setLocation(a.getEmployee().getLocation());
        }
        res.setStartDate(a.getStartDate());
        res.setEndDate(a.getEndDate());
        res.setAllocationPercent(a.getAllocationPercent());
        res.setStatus(a.getStatus());
        res.setRejectionReason(a.getRejectionReason());
        res.setAllocatedBy(a.getAllocatedBy());
        res.setConfirmedBy(a.getConfirmedBy());
        res.setConfirmedAt(a.getConfirmedAt());
        res.setVersion(a.getVersion());
        res.setCreatedAt(a.getCreatedAt());
        res.setUpdatedAt(a.getUpdatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public AllocationStatus getStatus() {
        return status;
    }

    public void setStatus(AllocationStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getAllocatedBy() {
        return allocatedBy;
    }

    public void setAllocatedBy(String allocatedBy) {
        this.allocatedBy = allocatedBy;
    }

    public String getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(String confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
