package com.talentmarketplace.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.ProjectDemand;

public class DemandResponse {

    private Long id;
    private String demandCode;
    private String projectName;
    private String role;
    private String department;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer allocationPercent;
    private DemandPriority priority;
    private DemandStatus status;
    private Double requiredExperienceYears;
    private List<DemandSkillResponse> requiredSkills = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DemandResponse() {
    }

    public DemandResponse(Long id, String demandCode, String projectName, String role,
                          String department, String location, LocalDate startDate,
                          LocalDate endDate, Integer allocationPercent, DemandPriority priority,
                          DemandStatus status, Double requiredExperienceYears,
                          List<DemandSkillResponse> requiredSkills, LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.demandCode = demandCode;
        this.projectName = projectName;
        this.role = role;
        this.department = department;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.allocationPercent = allocationPercent;
        this.priority = priority;
        this.status = status;
        this.requiredExperienceYears = requiredExperienceYears;
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DemandResponse fromEntity(ProjectDemand demand) {
        if (demand == null) {
            return null;
        }

        List<DemandSkillResponse> skillResponses = new ArrayList<>();
        if (demand.getRequiredSkills() != null) {
            skillResponses = demand.getRequiredSkills().stream()
                    .map(DemandSkillResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return new DemandResponse(
                demand.getId(),
                demand.getDemandCode(),
                demand.getProjectName(),
                demand.getRole(),
                demand.getDepartment(),
                demand.getLocation(),
                demand.getStartDate(),
                demand.getEndDate(),
                demand.getAllocationPercent(),
                demand.getPriority(),
                demand.getStatus(),
                demand.getRequiredExperienceYears(),
                skillResponses,
                demand.getCreatedAt(),
                demand.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<DemandSkillResponse> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<DemandSkillResponse> requiredSkills) {
        this.requiredSkills = requiredSkills;
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
