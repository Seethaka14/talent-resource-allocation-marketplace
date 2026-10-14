package com.talentmarketplace.dto.request;

import com.talentmarketplace.entity.SkillCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateSkillRequest {

    @NotBlank(message = "Skill name is required")
    @Size(max = 100, message = "Skill name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Skill category is required")
    private SkillCategory category;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    public UpdateSkillRequest() {
    }

    public UpdateSkillRequest(String name, SkillCategory category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public void setCategory(SkillCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
