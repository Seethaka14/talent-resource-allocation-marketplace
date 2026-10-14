package com.talentmarketplace.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "employee_skills",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_skills_emp_skill",
                        columnNames = {"employee_id", "skill_id"}
                )
        }
)
public class EmployeeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Employee is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull(message = "Skill is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @NotNull(message = "Proficiency level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level", nullable = false, length = 20)
    private ProficiencyLevel proficiencyLevel;

    @DecimalMin(value = "0.0", message = "Years of experience cannot be negative")
    @Column(name = "years_of_experience")
    private Double yearsOfExperience = 0.0;

    @Column(name = "last_validated_at")
    private LocalDate lastValidatedAt;

    @Size(max = 100, message = "Validated by cannot exceed 100 characters")
    @Column(name = "validated_by", length = 100)
    private String validatedBy;

    @Size(max = 1000, message = "Evidence notes cannot exceed 1000 characters")
    @Column(name = "evidence_notes", length = 1000)
    private String evidenceNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.yearsOfExperience == null) {
            this.yearsOfExperience = 0.0;
        }
        if (this.lastValidatedAt == null) {
            this.lastValidatedAt = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.yearsOfExperience == null) {
            this.yearsOfExperience = 0.0;
        }
    }

    public EmployeeSkill() {
    }

    public EmployeeSkill(Employee employee, Skill skill, ProficiencyLevel proficiencyLevel,
                         Double yearsOfExperience) {
        this(employee, skill, proficiencyLevel, yearsOfExperience, LocalDate.now(), null, null);
    }

    public EmployeeSkill(Employee employee, Skill skill, ProficiencyLevel proficiencyLevel,
                         Double yearsOfExperience, LocalDate lastValidatedAt,
                         String validatedBy, String evidenceNotes) {
        this.employee = employee;
        this.skill = skill;
        this.proficiencyLevel = proficiencyLevel;
        this.yearsOfExperience = yearsOfExperience != null ? yearsOfExperience : 0.0;
        this.lastValidatedAt = lastValidatedAt != null ? lastValidatedAt : LocalDate.now();
        this.validatedBy = validatedBy;
        this.evidenceNotes = evidenceNotes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public ProficiencyLevel getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public Double getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Double yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public LocalDate getLastValidatedAt() {
        return lastValidatedAt;
    }

    public void setLastValidatedAt(LocalDate lastValidatedAt) {
        this.lastValidatedAt = lastValidatedAt;
    }

    public String getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(String validatedBy) {
        this.validatedBy = validatedBy;
    }

    public String getEvidenceNotes() {
        return evidenceNotes;
    }

    public void setEvidenceNotes(String evidenceNotes) {
        this.evidenceNotes = evidenceNotes;
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
        return "EmployeeSkill{" +
                "id=" + id +
                ", employeeId=" + (employee != null ? employee.getId() : null) +
                ", skillId=" + (skill != null ? skill.getId() : null) +
                ", proficiencyLevel=" + proficiencyLevel +
                ", yearsOfExperience=" + yearsOfExperience +
                ", lastValidatedAt=" + lastValidatedAt +
                '}';
    }
}
