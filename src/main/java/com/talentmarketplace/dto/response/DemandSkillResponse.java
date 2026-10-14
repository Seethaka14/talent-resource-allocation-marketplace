package com.talentmarketplace.dto.response;

import com.talentmarketplace.entity.DemandSkill;
import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.SkillCategory;

public class DemandSkillResponse {

    private Long id;
    private Long skillId;
    private String skillName;
    private SkillCategory skillCategory;
    private boolean mandatory;
    private ProficiencyLevel minimumProficiency;
    private int minimumProficiencyWeight;

    public DemandSkillResponse() {
    }

    public DemandSkillResponse(Long id, Long skillId, String skillName, SkillCategory skillCategory,
                               boolean mandatory, ProficiencyLevel minimumProficiency,
                               int minimumProficiencyWeight) {
        this.id = id;
        this.skillId = skillId;
        this.skillName = skillName;
        this.skillCategory = skillCategory;
        this.mandatory = mandatory;
        this.minimumProficiency = minimumProficiency;
        this.minimumProficiencyWeight = minimumProficiencyWeight;
    }

    public static DemandSkillResponse fromEntity(DemandSkill ds) {
        if (ds == null) {
            return null;
        }

        Long sId = ds.getSkill() != null ? ds.getSkill().getId() : null;
        String sName = ds.getSkill() != null ? ds.getSkill().getName() : null;
        SkillCategory sCat = ds.getSkill() != null ? ds.getSkill().getCategory() : null;
        int weight = ds.getMinimumProficiency() != null ? ds.getMinimumProficiency().getLevelWeight() : 0;

        return new DemandSkillResponse(
                ds.getId(),
                sId,
                sName,
                sCat,
                ds.isMandatory(),
                ds.getMinimumProficiency(),
                weight
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public SkillCategory getSkillCategory() {
        return skillCategory;
    }

    public void setSkillCategory(SkillCategory skillCategory) {
        this.skillCategory = skillCategory;
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

    public int getMinimumProficiencyWeight() {
        return minimumProficiencyWeight;
    }

    public void setMinimumProficiencyWeight(int minimumProficiencyWeight) {
        this.minimumProficiencyWeight = minimumProficiencyWeight;
    }
}
