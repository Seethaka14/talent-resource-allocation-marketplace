package com.talentmarketplace.dto.response.analytics;

import java.util.ArrayList;
import java.util.List;

import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.SkillCategory;

public class SkillGapsReportResponse {

    private int totalCategories;
    private int categoriesWithGapsCount;
    private int totalUnmetSkillDemandSlots;
    private List<CategorySkillGapSummary> categoryGaps = new ArrayList<>();

    public SkillGapsReportResponse() {
    }

    public SkillGapsReportResponse(int totalCategories, int categoriesWithGapsCount,
                                  int totalUnmetSkillDemandSlots,
                                  List<CategorySkillGapSummary> categoryGaps) {
        this.totalCategories = totalCategories;
        this.categoriesWithGapsCount = categoriesWithGapsCount;
        this.totalUnmetSkillDemandSlots = totalUnmetSkillDemandSlots;
        this.categoryGaps = categoryGaps != null ? categoryGaps : new ArrayList<>();
    }

    public int getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(int totalCategories) {
        this.totalCategories = totalCategories;
    }

    public int getCategoriesWithGapsCount() {
        return categoriesWithGapsCount;
    }

    public void setCategoriesWithGapsCount(int categoriesWithGapsCount) {
        this.categoriesWithGapsCount = categoriesWithGapsCount;
    }

    public int getTotalUnmetSkillDemandSlots() {
        return totalUnmetSkillDemandSlots;
    }

    public void setTotalUnmetSkillDemandSlots(int totalUnmetSkillDemandSlots) {
        this.totalUnmetSkillDemandSlots = totalUnmetSkillDemandSlots;
    }

    public List<CategorySkillGapSummary> getCategoryGaps() {
        return categoryGaps;
    }

    public void setCategoryGaps(List<CategorySkillGapSummary> categoryGaps) {
        this.categoryGaps = categoryGaps;
    }

    public static class CategorySkillGapSummary {
        private SkillCategory category;
        private int distinctDemandsCount;
        private int totalDemandSlots;
        private int qualifiedSupplyCount;
        private int netDeficit;
        private List<SkillGapDetail> skillGaps = new ArrayList<>();

        public CategorySkillGapSummary() {
        }

        public CategorySkillGapSummary(SkillCategory category, int distinctDemandsCount,
                                      int totalDemandSlots, int qualifiedSupplyCount,
                                      int netDeficit, List<SkillGapDetail> skillGaps) {
            this.category = category;
            this.distinctDemandsCount = distinctDemandsCount;
            this.totalDemandSlots = totalDemandSlots;
            this.qualifiedSupplyCount = qualifiedSupplyCount;
            this.netDeficit = netDeficit;
            this.skillGaps = skillGaps != null ? skillGaps : new ArrayList<>();
        }

        public SkillCategory getCategory() {
            return category;
        }

        public void setCategory(SkillCategory category) {
            this.category = category;
        }

        public int getDistinctDemandsCount() {
            return distinctDemandsCount;
        }

        public void setDistinctDemandsCount(int distinctDemandsCount) {
            this.distinctDemandsCount = distinctDemandsCount;
        }

        public int getTotalDemandSlots() {
            return totalDemandSlots;
        }

        public void setTotalDemandSlots(int totalDemandSlots) {
            this.totalDemandSlots = totalDemandSlots;
        }

        public int getQualifiedSupplyCount() {
            return qualifiedSupplyCount;
        }

        public void setQualifiedSupplyCount(int qualifiedSupplyCount) {
            this.qualifiedSupplyCount = qualifiedSupplyCount;
        }

        public int getNetDeficit() {
            return netDeficit;
        }

        public void setNetDeficit(int netDeficit) {
            this.netDeficit = netDeficit;
        }

        public List<SkillGapDetail> getSkillGaps() {
            return skillGaps;
        }

        public void setSkillGaps(List<SkillGapDetail> skillGaps) {
            this.skillGaps = skillGaps;
        }
    }

    public static class SkillGapDetail {
        private Long skillId;
        private String skillName;
        private ProficiencyLevel minimumProficiency;
        private int demandCount;
        private int mandatoryDemandCount;
        private int optionalDemandCount;
        private int qualifiedResourceCount;
        private int deficit;

        public SkillGapDetail() {
        }

        public SkillGapDetail(Long skillId, String skillName, ProficiencyLevel minimumProficiency,
                              int demandCount, int mandatoryDemandCount, int optionalDemandCount,
                              int qualifiedResourceCount, int deficit) {
            this.skillId = skillId;
            this.skillName = skillName;
            this.minimumProficiency = minimumProficiency;
            this.demandCount = demandCount;
            this.mandatoryDemandCount = mandatoryDemandCount;
            this.optionalDemandCount = optionalDemandCount;
            this.qualifiedResourceCount = qualifiedResourceCount;
            this.deficit = deficit;
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

        public ProficiencyLevel getMinimumProficiency() {
            return minimumProficiency;
        }

        public void setMinimumProficiency(ProficiencyLevel minimumProficiency) {
            this.minimumProficiency = minimumProficiency;
        }

        public int getDemandCount() {
            return demandCount;
        }

        public void setDemandCount(int demandCount) {
            this.demandCount = demandCount;
        }

        public int getMandatoryDemandCount() {
            return mandatoryDemandCount;
        }

        public void setMandatoryDemandCount(int mandatoryDemandCount) {
            this.mandatoryDemandCount = mandatoryDemandCount;
        }

        public int getOptionalDemandCount() {
            return optionalDemandCount;
        }

        public void setOptionalDemandCount(int optionalDemandCount) {
            this.optionalDemandCount = optionalDemandCount;
        }

        public int getQualifiedResourceCount() {
            return qualifiedResourceCount;
        }

        public void setQualifiedResourceCount(int qualifiedResourceCount) {
            this.qualifiedResourceCount = qualifiedResourceCount;
        }

        public int getDeficit() {
            return deficit;
        }

        public void setDeficit(int deficit) {
            this.deficit = deficit;
        }
    }
}
