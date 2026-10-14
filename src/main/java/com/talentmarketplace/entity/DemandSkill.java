package com.talentmarketplace.entity;

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
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "demand_skills",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_demand_skills_demand_skill",
                        columnNames = {"demand_id", "skill_id"}
                )
        }
)
public class DemandSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Demand is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demand_id", nullable = false)
    private ProjectDemand demand;

    @NotNull(message = "Skill is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private boolean mandatory = true;

    @NotNull(message = "Minimum proficiency level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "minimum_proficiency", nullable = false, length = 20)
    private ProficiencyLevel minimumProficiency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public DemandSkill() {
    }

    public DemandSkill(ProjectDemand demand, Skill skill, boolean mandatory, ProficiencyLevel minimumProficiency) {
        this.demand = demand;
        this.skill = skill;
        this.mandatory = mandatory;
        this.minimumProficiency = minimumProficiency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProjectDemand getDemand() {
        return demand;
    }

    public void setDemand(ProjectDemand demand) {
        this.demand = demand;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public ProficiencyLevel getMinimumProficiency() {
        return minimumProficiency;
    }

    public void setMinimumProficiency(ProficiencyLevel minimumProficiency) {
        this.minimumProficiency = minimumProficiency;
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
        return "DemandSkill{" +
                "id=" + id +
                ", demandId=" + (demand != null ? demand.getId() : null) +
                ", skillId=" + (skill != null ? skill.getId() : null) +
                ", mandatory=" + mandatory +
                ", minimumProficiency=" + minimumProficiency +
                '}';
    }
}
