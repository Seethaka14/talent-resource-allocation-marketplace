package com.talentmarketplace.entity;

public enum DemandStatus {
    OPEN,
    MATCHED,
    ALLOCATED,
    CANCELLED;

    public boolean canTransitionTo(DemandStatus nextStatus) {
        if (this == nextStatus) {
            return true;
        }
        if (this == CANCELLED) {
            return false; // Terminal state
        }
        switch (this) {
            case OPEN:
                return nextStatus == MATCHED || nextStatus == ALLOCATED || nextStatus == CANCELLED;
            case MATCHED:
                return nextStatus == OPEN || nextStatus == ALLOCATED || nextStatus == CANCELLED;
            case ALLOCATED:
                return nextStatus == CANCELLED || nextStatus == OPEN;
            default:
                return false;
        }
    }
}
