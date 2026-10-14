package com.talentmarketplace.ai.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.talentmarketplace.ai.ollama.OllamaTool;
import com.talentmarketplace.dto.response.CandidateRecommendationResponse;
import com.talentmarketplace.dto.response.DemandRecommendationListResponse;
import com.talentmarketplace.service.CandidateRecommendationService;

@Component
public class GetDemandCandidatesTool implements AiTool {

    private final CandidateRecommendationService candidateRecommendationService;

    public GetDemandCandidatesTool(CandidateRecommendationService candidateRecommendationService) {
        this.candidateRecommendationService = candidateRecommendationService;
    }

    @Override
    public String getName() {
        return "get_demand_candidates";
    }

    @Override
    public String getDescription() {
        return "Retrieve deterministic candidate recommendations and rankings for a project demand ID";
    }

    @Override
    public OllamaTool getOllamaTool() {
        Map<String, Object> props = new HashMap<>();

        Map<String, Object> idProp = new HashMap<>();
        idProp.put("type", "integer");
        idProp.put("description", "The numeric project demand ID (e.g. 50)");
        props.put("demandId", idProp);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", props);
        parameters.put("required", Collections.singletonList("demandId"));

        return OllamaTool.function(getName(), getDescription(), parameters);
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        if (arguments == null || !arguments.containsKey("demandId") || arguments.get("demandId") == null) {
            throw new IllegalArgumentException("Argument 'demandId' is required for tool " + getName());
        }

        Long demandId;
        try {
            demandId = Long.valueOf(arguments.get("demandId").toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid demandId format: " + arguments.get("demandId"));
        }

        DemandRecommendationListResponse response = candidateRecommendationService.recommendCandidatesForDemand(demandId);

        Map<String, Object> result = new HashMap<>();
        result.put("demandId", response.getDemandId());
        result.put("demandCode", response.getDemandCode());
        result.put("role", response.getRole());
        result.put("totalCandidatesEvaluated", response.getTotalCandidatesEvaluated());
        result.put("eligibleCandidatesCount", response.getEligibleCandidatesCount());

        List<Map<String, Object>> candidateList = new ArrayList<>();
        if (response.getCandidates() != null) {
            List<CandidateRecommendationResponse> topCandidates = response.getCandidates().stream()
                    .limit(5)
                    .collect(Collectors.toList());
            for (CandidateRecommendationResponse c : topCandidates) {
                Map<String, Object> cMap = new HashMap<>();
                cMap.put("employeeId", c.getEmployeeId());
                cMap.put("employeeName", c.getEmployeeName());
                cMap.put("employeeCode", c.getEmployeeCode());
                cMap.put("eligible", c.isEligible());
                cMap.put("totalScore", c.getTotalScore());
                cMap.put("matchExplanation", c.getMatchExplanation());
                candidateList.add(cMap);
            }
        }
        result.put("topCandidates", candidateList);

        return result;
    }
}
