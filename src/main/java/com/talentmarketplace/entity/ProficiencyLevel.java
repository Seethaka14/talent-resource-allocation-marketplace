package com.talentmarketplace.entity;

public enum ProficiencyLevel {
    BEGINNER(1),
    INTERMEDIATE(2),
    ADVANCED(3);

    private final int levelWeight;

    ProficiencyLevel(int levelWeight) {
        this.levelWeight = levelWeight;
    }

    public int getLevelWeight() {
        return levelWeight;
    }

    public boolean meetsMinimumRequirement(ProficiencyLevel required) {
        if (required == null) {
            return true;
        }
        return this.levelWeight >= required.levelWeight;
    }
}
