package com.talentmarketplace.dto.request;

import java.time.LocalDate;

import com.talentmarketplace.entity.UnavailabilityType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateUnavailabilityRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Unavailability type is required")
    private UnavailabilityType type;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    public CreateUnavailabilityRequest() {
    }

    public CreateUnavailabilityRequest(LocalDate startDate, LocalDate endDate,
                                       UnavailabilityType type, String reason) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.reason = reason;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public UnavailabilityType getType() {
        return type;
    }

    public void setType(UnavailabilityType type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
