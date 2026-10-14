package com.talentmarketplace.dto.request;

import java.time.LocalDate;

import com.talentmarketplace.entity.ProficiencyLevel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateEmployeeSkillRequest {

    @NotNull(message = "Proficiency level is required (BEGINNER, INTERMEDIATE, ADVANCED)")
    private ProficiencyLevel proficiencyLevel;

    @DecimalMin(value = "0.0", message = "Years of experience cannot be negative")
    private Double yearsOfExperience = 0.0;

    private LocalDate lastValidatedAt;

    @Size(max = 100, message = "Validated by cannot exceed 100 characters")
    private String validatedBy;

    @Size(max = 1000, message = "Evidence notes cannot exceed 1000 characters")
    private String evidenceNotes;

    public UpdateEmployeeSkillRequest() {
    }

    public UpdateEmployeeSkillRequest(ProficiencyLevel proficiencyLevel, Double yearsOfExperience,
                                      LocalDate lastValidatedAt, String validatedBy, String evidenceNotes) {
        this.proficiencyLevel = proficiencyLevel;
        this.yearsOfExperience = yearsOfExperience != null ? yearsOfExperience : 0.0;
        this.lastValidatedAt = lastValidatedAt;
        this.validatedBy = validatedBy;
        this.evidenceNotes = evidenceNotes;
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
}
