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
public class CompareCandidatesTool implements AiTool {

    private final CandidateRecommendationService candidateRecommendationService;

    public CompareCandidatesTool(CandidateRecommendationService candidateRecommendationService) {
        this.candidateRecommendationService = candidateRecommendationService;
    }

    @Override
    public String getName() {
        return "compare_candidates";
    }

    @Override
    public String getDescription() {
        return "Compare top candidates for a project demand side-by-side using actual recommendation results";
    }

    @Override
    public OllamaTool getOllamaTool() {
        Map<String, Object> props = new HashMap<>();

        Map<String, Object> demandProp = new HashMap<>();
        demandProp.put("type", "integer");
        demandProp.put("description", "Numeric project demand ID");
        props.put("demandId", demandProp);

        Map<String, Object> limitProp = new HashMap<>();
        limitProp.put("type", "integer");
        limitProp.put("description", "Maximum number of top candidates to compare (default 3)");
        props.put("limit", limitProp);

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
            throw new IllegalArgumentException("Invalid demandId: " + arguments.get("demandId"));
        }

        int limit = 3;
        if (arguments.containsKey("limit") && arguments.get("limit") != null) {
            try {
                limit = Math.max(1, Math.min(10, Integer.parseInt(arguments.get("limit").toString())));
            } catch (NumberFormatException ignored) {
            }
        }

        DemandRecommendationListResponse response = candidateRecommendationService.recommendCandidatesForDemand(demandId, false, null, limit);

        Map<String, Object> result = new HashMap<>();
        result.put("demandId", response.getDemandId());
        result.put("role", response.getRole());
        result.put("comparedCount", response.getCandidates() != null ? response.getCandidates().size() : 0);

        List<Map<String, Object>> comparisonList = new ArrayList<>();
        if (response.getCandidates() != null) {
            int rank = 1;
            for (CandidateRecommendationResponse c : response.getCandidates()) {
                Map<String, Object> cMap = new HashMap<>();
                cMap.put("rank", rank++);
                cMap.put("employeeName", c.getEmployeeName());
                cMap.put("employeeCode", c.getEmployeeCode());
                cMap.put("eligible", c.isEligible());
                cMap.put("totalScore", c.getTotalScore());
                cMap.put("skillScore", c.getSkillScore());
                cMap.put("availabilityScore", c.getAvailabilityScore());
                cMap.put("locationScore", c.getLocationScore());
                cMap.put("matchedSkillsCount", c.getMatchedSkills() != null ? c.getMatchedSkills().size() : 0);
                cMap.put("missingMandatoryCount", c.getMissingMandatorySkills() != null ? c.getMissingMandatorySkills().size() : 0);
                cMap.put("exclusionReasons", c.getExclusionReasons());
                cMap.put("matchExplanation", c.getMatchExplanation());
                comparisonList.add(cMap);
            }
        }
        result.put("candidates", comparisonList);

        return result;
    }
}
