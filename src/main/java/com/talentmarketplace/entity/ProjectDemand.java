package com.talentmarketplace.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "project_demands",
        indexes = {
                @Index(name = "idx_demands_code", columnList = "demand_code", unique = true),
                @Index(name = "idx_demands_status_priority", columnList = "status, priority"),
                @Index(name = "idx_demands_dates", columnList = "start_date, end_date")
        }
)
public class ProjectDemand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Demand code is required")
    @Size(max = 30, message = "Demand code cannot exceed 30 characters")
    @Column(name = "demand_code", nullable = false, unique = true, length = 30)
    private String demandCode;

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Project name cannot exceed 100 characters")
    @Column(name = "project_name", nullable = false, length = 100)
    private String projectName;

    @NotBlank(message = "Role is required")
    @Size(max = 50, message = "Role cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String role;

    @Size(max = 50, message = "Department cannot exceed 50 characters")
    @Column(length = 50)
    private String department;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String location;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Allocation percent is required")
    @Min(value = 1, message = "Allocation percent must be at least 1")
    @Max(value = 100, message = "Allocation percent cannot exceed 100")
    @Column(name = "allocation_percent", nullable = false)
    private Integer allocationPercent;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DemandPriority priority;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DemandStatus status = DemandStatus.OPEN;

    @DecimalMin(value = "0.0", message = "Required experience cannot be negative")
    @Column(name = "required_experience_years")
    private Double requiredExperienceYears = 0.0;

    @OneToMany(mappedBy = "demand", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DemandSkill> requiredSkills = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DemandStatus.OPEN;
        }
        if (this.requiredExperienceYears == null) {
            this.requiredExperienceYears = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public ProjectDemand() {
    }

    public ProjectDemand(String demandCode, String projectName, String role, String department,
                         String location, LocalDate startDate, LocalDate endDate,
                         Integer allocationPercent, DemandPriority priority, DemandStatus status,
                         Double requiredExperienceYears) {
        this.demandCode = demandCode;
        this.projectName = projectName;
        this.role = role;
        this.department = department;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.allocationPercent = allocationPercent;
        this.priority = priority;
        this.status = status != null ? status : DemandStatus.OPEN;
        this.requiredExperienceYears = requiredExperienceYears != null ? requiredExperienceYears : 0.0;
    }

    public void addRequiredSkill(DemandSkill skill) {
        requiredSkills.add(skill);
        skill.setDemand(this);
    }

    public void removeRequiredSkill(DemandSkill skill) {
        requiredSkills.remove(skill);
        skill.setDemand(null);
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

    public List<DemandSkill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<DemandSkill> requiredSkills) {
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

    @Override
    public String toString() {
        return "ProjectDemand{" +
                "id=" + id +
                ", demandCode='" + demandCode + '\'' +
                ", projectName='" + projectName + '\'' +
                ", role='" + role + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", allocationPercent=" + allocationPercent +
                ", priority=" + priority +
                ", status=" + status +
                '}';
    }
}
