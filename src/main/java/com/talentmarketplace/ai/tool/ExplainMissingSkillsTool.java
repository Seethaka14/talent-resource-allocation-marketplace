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
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.service.CandidateRecommendationService;

@Component
public class ExplainMissingSkillsTool implements AiTool {

    private final CandidateRecommendationService candidateRecommendationService;
    private final EmployeeRepository employeeRepository;

    public ExplainMissingSkillsTool(CandidateRecommendationService candidateRecommendationService,
                                     EmployeeRepository employeeRepository) {
        this.candidateRecommendationService = candidateRecommendationService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public String getName() {
        return "explain_missing_skills";
    }

    @Override
    public String getDescription() {
        return "Explain missing mandatory and optional skills, or proficiency gaps, for a project demand and optionally a specific candidate";
    }

    @Override
    public OllamaTool getOllamaTool() {
        Map<String, Object> props = new HashMap<>();

        Map<String, Object> demandProp = new HashMap<>();
        demandProp.put("type", "integer");
        demandProp.put("description", "Numeric project demand ID");
        props.put("demandId", demandProp);

        Map<String, Object> empProp = new HashMap<>();
        empProp.put("type", "string");
        empProp.put("description", "Optional employee ID, employee code, or employee full name (e.g. Marcus Brody)");
        props.put("employeeId", empProp);

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

        Long employeeId = null;
        if (arguments.containsKey("employeeId") && arguments.get("employeeId") != null) {
            String empArg = arguments.get("employeeId").toString().trim();
            try {
                employeeId = Long.valueOf(empArg);
            } catch (NumberFormatException e) {
                List<Employee> found = employeeRepository.searchEmployees(null, null, null, empArg);
                if (!found.isEmpty()) {
                    employeeId = found.get(0).getId();
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("demandId", demandId);

        if (employeeId != null) {
            CandidateRecommendationResponse evaluation = candidateRecommendationService.evaluateCandidateForDemand(demandId, employeeId);
            result.put("employeeId", evaluation.getEmployeeId());
            result.put("employeeName", evaluation.getEmployeeName());
            result.put("eligible", evaluation.isEligible());
            result.put("skillScore", evaluation.getSkillScore());
            result.put("matchExplanation", evaluation.getMatchExplanation());
            result.put("missingMandatorySkills", evaluation.getMissingMandatorySkills() != null
                    ? evaluation.getMissingMandatorySkills().stream()
                        .map(s -> Map.of("skillName", s.getSkillName(), "requiredProficiency", s.getRequiredProficiency()))
                        .collect(Collectors.toList())
                    : Collections.emptyList());
            result.put("missingOptionalSkills", evaluation.getMissingOptionalSkills() != null
                    ? evaluation.getMissingOptionalSkills().stream()
                        .map(s -> Map.of("skillName", s.getSkillName(), "requiredProficiency", s.getRequiredProficiency()))
                        .collect(Collectors.toList())
                    : Collections.emptyList());
            result.put("matchedSkills", evaluation.getMatchedSkills() != null
                    ? evaluation.getMatchedSkills().stream()
                        .map(s -> Map.of("skillName", s.getSkillName(), "candidateProficiency", s.getCandidateProficiency(), "requiredProficiency", s.getRequiredProficiency()))
                        .collect(Collectors.toList())
                    : Collections.emptyList());
            return result;
        }

        DemandRecommendationListResponse recs = candidateRecommendationService.recommendCandidatesForDemand(demandId);
        result.put("role", recs.getRole());
        List<Map<String, Object>> candidateGapSummaries = new ArrayList<>();
        if (recs.getCandidates() != null) {
            for (CandidateRecommendationResponse c : recs.getCandidates()) {
                Map<String, Object> cMap = new HashMap<>();
                cMap.put("employeeId", c.getEmployeeId());
                cMap.put("employeeName", c.getEmployeeName());
                cMap.put("eligible", c.isEligible());
                cMap.put("missingMandatory", c.getMissingMandatorySkills() != null
                        ? c.getMissingMandatorySkills().stream().map(s -> s.getSkillName()).collect(Collectors.toList())
                        : Collections.emptyList());
                cMap.put("missingOptional", c.getMissingOptionalSkills() != null
                        ? c.getMissingOptionalSkills().stream().map(s -> s.getSkillName()).collect(Collectors.toList())
                        : Collections.emptyList());
                candidateGapSummaries.add(cMap);
            }
        }
        result.put("candidateSkillGaps", candidateGapSummaries);
        return result;
    }
}
