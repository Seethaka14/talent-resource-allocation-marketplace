package com.talentmarketplace.dto.request;

import com.talentmarketplace.entity.ProficiencyLevel;

import jakarta.validation.constraints.NotNull;

public class DemandSkillRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    private boolean mandatory = true;

    @NotNull(message = "Minimum proficiency level is required (BEGINNER, INTERMEDIATE, ADVANCED)")
    private ProficiencyLevel minimumProficiency;

    public DemandSkillRequest() {
    }

    public DemandSkillRequest(Long skillId, boolean mandatory, ProficiencyLevel minimumProficiency) {
        this.skillId = skillId;
        this.mandatory = mandatory;
        this.minimumProficiency = minimumProficiency;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
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
}
