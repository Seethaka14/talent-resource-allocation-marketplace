package com.talentmarketplace.dto.response;

import java.time.LocalDate;

import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.SkillCategory;

public class SkillMatchDetail {

    private Long skillId;
    private String skillName;
    private SkillCategory category;
    private boolean mandatory;
    private ProficiencyLevel requiredProficiency;
    private int requiredProficiencyWeight;
    private ProficiencyLevel candidateProficiency;
    private int candidateProficiencyWeight;
    private boolean matched;
    private boolean meetsProficiencyRequirement;
    private Double yearsOfExperience;
    private LocalDate lastValidatedAt;
    private String validatedBy;
    private String evidenceNotes;

    public SkillMatchDetail() {
    }

    public SkillMatchDetail(Long skillId, String skillName, SkillCategory category, boolean mandatory,
                            ProficiencyLevel requiredProficiency, ProficiencyLevel candidateProficiency,
                            boolean matched, boolean meetsProficiencyRequirement,
                            Double yearsOfExperience, LocalDate lastValidatedAt,
                            String validatedBy, String evidenceNotes) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.mandatory = mandatory;
        this.requiredProficiency = requiredProficiency;
        this.requiredProficiencyWeight = requiredProficiency != null ? requiredProficiency.getLevelWeight() : 0;
        this.candidateProficiency = candidateProficiency;
        this.candidateProficiencyWeight = candidateProficiency != null ? candidateProficiency.getLevelWeight() : 0;
        this.matched = matched;
        this.meetsProficiencyRequirement = meetsProficiencyRequirement;
        this.yearsOfExperience = yearsOfExperience;
        this.lastValidatedAt = lastValidatedAt;
        this.validatedBy = validatedBy;
        this.evidenceNotes = evidenceNotes;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public void setCategory(SkillCategory category) {
        this.category = category;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public ProficiencyLevel getRequiredProficiency() {
        return requiredProficiency;
    }

    public void setRequiredProficiency(ProficiencyLevel requiredProficiency) {
        this.requiredProficiency = requiredProficiency;
        this.requiredProficiencyWeight = requiredProficiency != null ? requiredProficiency.getLevelWeight() : 0;
    }

    public int getRequiredProficiencyWeight() {
        return requiredProficiencyWeight;
    }

    public void setRequiredProficiencyWeight(int requiredProficiencyWeight) {
        this.requiredProficiencyWeight = requiredProficiencyWeight;
    }

    public ProficiencyLevel getCandidateProficiency() {
        return candidateProficiency;
    }

    public void setCandidateProficiency(ProficiencyLevel candidateProficiency) {
        this.candidateProficiency = candidateProficiency;
        this.candidateProficiencyWeight = candidateProficiency != null ? candidateProficiency.getLevelWeight() : 0;
    }

    public int getCandidateProficiencyWeight() {
        return candidateProficiencyWeight;
    }

    public void setCandidateProficiencyWeight(int candidateProficiencyWeight) {
        this.candidateProficiencyWeight = candidateProficiencyWeight;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public boolean isMeetsProficiencyRequirement() {
        return meetsProficiencyRequirement;
    }

    public void setMeetsProficiencyRequirement(boolean meetsProficiencyRequirement) {
        this.meetsProficiencyRequirement = meetsProficiencyRequirement;
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
