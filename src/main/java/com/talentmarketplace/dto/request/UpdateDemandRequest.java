package com.talentmarketplace.dto.request;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateDemandRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Project name cannot exceed 100 characters")
    private String projectName;

    @NotBlank(message = "Role is required")
    @Size(max = 50, message = "Role cannot exceed 50 characters")
    private String role;

    @Size(max = 50, message = "Department cannot exceed 50 characters")
    private String department;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Allocation percent is required")
    @Min(value = 1, message = "Allocation percent must be at least 1")
    @Max(value = 100, message = "Allocation percent cannot exceed 100")
    private Integer allocationPercent;

    @NotNull(message = "Priority is required")
    private DemandPriority priority;

    @NotNull(message = "Status is required")
    private DemandStatus status;

    @DecimalMin(value = "0.0", message = "Required experience cannot be negative")
    private Double requiredExperienceYears = 0.0;

    private List<@Valid DemandSkillRequest> requiredSkills = new ArrayList<>();

    public UpdateDemandRequest() {
    }

    public UpdateDemandRequest(String projectName, String role, String department, String location,
                               LocalDate startDate, LocalDate endDate, Integer allocationPercent,
                               DemandPriority priority, DemandStatus status,
                               Double requiredExperienceYears, List<DemandSkillRequest> requiredSkills) {
        this.projectName = projectName;
        this.role = role;
        this.department = department;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.allocationPercent = allocationPercent;
        this.priority = priority;
        this.status = status;
        this.requiredExperienceYears = requiredExperienceYears != null ? requiredExperienceYears : 0.0;
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
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

    public DemandPriority getPriority() {
        return priority;
    }

    public void setPriority(DemandPriority priority) {
        this.priority = priority;
    }

    public DemandStatus getStatus() {
        return status;
    }

    public void setStatus(DemandStatus status) {
        this.status = status;
    }

    public Double getRequiredExperienceYears() {
        return requiredExperienceYears;
    }

    public void setRequiredExperienceYears(Double requiredExperienceYears) {
        this.requiredExperienceYears = requiredExperienceYears;
    }

    public List<DemandSkillRequest> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<DemandSkillRequest> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }
}
