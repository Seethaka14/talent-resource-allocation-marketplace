package com.talentmarketplace.dto.response.analytics;

import java.util.ArrayList;
import java.util.List;

import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.SkillCategory;

public class ResourcesBySkillReportResponse {

    private long totalActiveResources;
    private long totalDistinctSkills;
    private List<SkillProficiencyResourceSummary> items = new ArrayList<>();

    public ResourcesBySkillReportResponse() {
    }

    public ResourcesBySkillReportResponse(long totalActiveResources, long totalDistinctSkills,
                                          List<SkillProficiencyResourceSummary> items) {
        this.totalActiveResources = totalActiveResources;
        this.totalDistinctSkills = totalDistinctSkills;
        this.items = items != null ? items : new ArrayList<>();
    }

    public long getTotalActiveResources() {
        return totalActiveResources;
    }

    public void setTotalActiveResources(long totalActiveResources) {
        this.totalActiveResources = totalActiveResources;
    }

    public long getTotalDistinctSkills() {
        return totalDistinctSkills;
    }

    public void setTotalDistinctSkills(long totalDistinctSkills) {
        this.totalDistinctSkills = totalDistinctSkills;
    }

    public List<SkillProficiencyResourceSummary> getItems() {
        return items;
    }

    public void setItems(List<SkillProficiencyResourceSummary> items) {
        this.items = items;
    }

    public static class SkillProficiencyResourceSummary {
        private Long skillId;
        private String skillName;
        private SkillCategory category;
        private ProficiencyLevel proficiencyLevel;
        private long resourceCount;
        private double avgYearsExperience;

        public SkillProficiencyResourceSummary() {
        }

        public SkillProficiencyResourceSummary(Long skillId, String skillName, SkillCategory category,
                                               ProficiencyLevel proficiencyLevel, long resourceCount,
                                               double avgYearsExperience) {
            this.skillId = skillId;
            this.skillName = skillName;
            this.category = category;
            this.proficiencyLevel = proficiencyLevel;
            this.resourceCount = resourceCount;
            this.avgYearsExperience = avgYearsExperience;
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

        public ProficiencyLevel getProficiencyLevel() {
            return proficiencyLevel;
        }

        public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) {
            this.proficiencyLevel = proficiencyLevel;
        }

        public long getResourceCount() {
            return resourceCount;
        }

        public void setResourceCount(long resourceCount) {
            this.resourceCount = resourceCount;
        }

        public double getAvgYearsExperience() {
            return avgYearsExperience;
        }

        public void setAvgYearsExperience(double avgYearsExperience) {
            this.avgYearsExperience = avgYearsExperience;
        }
    }
}
