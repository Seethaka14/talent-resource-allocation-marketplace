package com.talentmarketplace.dto.response;

import java.time.LocalDateTime;

import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;

public class SkillResponse {

    private Long id;
    private String name;
    private SkillCategory category;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SkillResponse() {
    }

    public SkillResponse(Long id, String name, SkillCategory category, String description,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SkillResponse fromEntity(Skill skill) {
        if (skill == null) {
            return null;
        }
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCategory(),
                skill.getDescription(),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
