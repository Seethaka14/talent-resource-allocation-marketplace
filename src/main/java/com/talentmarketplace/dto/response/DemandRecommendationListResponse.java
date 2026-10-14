package com.talentmarketplace.dto.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DemandRecommendationListResponse {

    private Long demandId;
    private String demandCode;
    private String projectName;
    private String role;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer allocationPercent;
    private int totalCandidatesEvaluated;
    private int eligibleCandidatesCount;
    private List<CandidateRecommendationResponse> candidates = new ArrayList<>();

    public DemandRecommendationListResponse() {
    }

    public DemandRecommendationListResponse(Long demandId, String demandCode, String projectName,
                                            String role, String location, LocalDate startDate,
                                            LocalDate endDate, Integer allocationPercent,
                                            int totalCandidatesEvaluated, int eligibleCandidatesCount,
                                            List<CandidateRecommendationResponse> candidates) {
        this.demandId = demandId;
        this.demandCode = demandCode;
        this.projectName = projectName;
        this.role = role;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.allocationPercent = allocationPercent;
        this.totalCandidatesEvaluated = totalCandidatesEvaluated;
        this.eligibleCandidatesCount = eligibleCandidatesCount;
        this.candidates = candidates != null ? candidates : new ArrayList<>();
    }

    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }

    public String getDemandCode() {
        return demandCode;
    }

    public void setDemandCode(String demandCode) {
        this.demandCode = demandCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public Integer getAllocationPercent() {
        return allocationPercent;
    }

    public void setAllocationPercent(Integer allocationPercent) {
        this.allocationPercent = allocationPercent;
    }

    public int getTotalCandidatesEvaluated() {
        return totalCandidatesEvaluated;
    }

    public void setTotalCandidatesEvaluated(int totalCandidatesEvaluated) {
        this.totalCandidatesEvaluated = totalCandidatesEvaluated;
    }

    public int getEligibleCandidatesCount() {
        return eligibleCandidatesCount;
    }

    public void setEligibleCandidatesCount(int eligibleCandidatesCount) {
        this.eligibleCandidatesCount = eligibleCandidatesCount;
    }

    public List<CandidateRecommendationResponse> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<CandidateRecommendationResponse> candidates) {
        this.candidates = candidates;
    }
}
