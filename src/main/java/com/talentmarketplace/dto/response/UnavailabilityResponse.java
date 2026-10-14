package com.talentmarketplace.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.talentmarketplace.entity.EmployeeUnavailability;
import com.talentmarketplace.entity.UnavailabilityType;

public class UnavailabilityResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private UnavailabilityType type;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UnavailabilityResponse() {
    }

    public UnavailabilityResponse(Long id, Long employeeId, String employeeCode, String employeeName,
                                  LocalDate startDate, LocalDate endDate, UnavailabilityType type,
                                  String reason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UnavailabilityResponse fromEntity(EmployeeUnavailability u) {
        if (u == null) {
            return null;
        }

        Long empId = u.getEmployee() != null ? u.getEmployee().getId() : null;
        String empCode = u.getEmployee() != null ? u.getEmployee().getEmployeeCode() : null;
        String empName = u.getEmployee() != null ? u.getEmployee().getName() : null;

        return new UnavailabilityResponse(
                u.getId(),
                empId,
                empCode,
                empName,
                u.getStartDate(),
                u.getEndDate(),
                u.getType(),
                u.getReason(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public UnavailabilityType getType() {
        return type;
    }

    public void setType(UnavailabilityType type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
