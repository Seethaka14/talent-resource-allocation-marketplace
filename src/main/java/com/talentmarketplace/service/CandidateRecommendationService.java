package com.talentmarketplace.service;

import com.talentmarketplace.dto.response.CandidateRecommendationResponse;
import com.talentmarketplace.dto.response.DemandRecommendationListResponse;

public interface CandidateRecommendationService {

    DemandRecommendationListResponse recommendCandidatesForDemand(Long demandId, Boolean eligibleOnly, Double minScore, Integer limit);

    default DemandRecommendationListResponse recommendCandidatesForDemand(Long demandId) {
        return recommendCandidatesForDemand(demandId, false, null, null);
    }

    CandidateRecommendationResponse evaluateCandidateForDemand(Long demandId, Long employeeId);
}
