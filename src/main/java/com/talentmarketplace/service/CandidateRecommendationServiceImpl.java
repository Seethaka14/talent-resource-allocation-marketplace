package com.talentmarketplace.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentmarketplace.dto.response.AvailabilityAssessmentResponse;
import com.talentmarketplace.dto.response.CandidateRecommendationResponse;
import com.talentmarketplace.dto.response.DemandRecommendationListResponse;
import com.talentmarketplace.dto.response.SkillMatchDetail;
import com.talentmarketplace.entity.DemandSkill;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeSkill;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;

@Service
@Transactional(readOnly = true)
public class CandidateRecommendationServiceImpl implements CandidateRecommendationService {

    private final ProjectDemandRepository demandRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final AvailabilityService availabilityService;

    public CandidateRecommendationServiceImpl(ProjectDemandRepository demandRepository,
                                             EmployeeRepository employeeRepository,
                                             EmployeeSkillRepository employeeSkillRepository,
                                             AvailabilityService availabilityService) {
        this.demandRepository = demandRepository;
        this.employeeRepository = employeeRepository;
        this.employeeSkillRepository = employeeSkillRepository;
        this.availabilityService = availabilityService;
    }

    @Override
    public DemandRecommendationListResponse recommendCandidatesForDemand(
            Long demandId, Boolean eligibleOnly, Double minScore, Integer limit) {
        ProjectDemand demand = demandRepository.findByIdWithSkills(demandId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectDemand", "id", demandId));

        List<Employee> allEmployees = employeeRepository.findAll();
        List<CandidateRecommendationResponse> candidates = new ArrayList<>();

        for (Employee employee : allEmployees) {
            List<EmployeeSkill> employeeSkills = employeeSkillRepository.findByEmployeeIdWithDetails(employee.getId());
            CandidateRecommendationResponse candidate = evaluateCandidate(demand, employee, employeeSkills);
            candidates.add(candidate);
        }

        // Deterministic stable sorting:
        // 1. Eligible candidates first
        // 2. Higher totalScore first
        // 3. Stable tie-breaker: Employee ID ascending
        candidates.sort(
                Comparator.comparing(CandidateRecommendationResponse::isEligible).reversed()
                        .thenComparing(Comparator.comparingDouble(CandidateRecommendationResponse::getTotalScore).reversed())
                        .thenComparing(CandidateRecommendationResponse::getEmployeeId)
        );

        int totalEvaluated = candidates.size();
        int eligibleCount = (int) candidates.stream().filter(CandidateRecommendationResponse::isEligible).count();

        List<CandidateRecommendationResponse> filteredCandidates = candidates.stream()
                .filter(c -> eligibleOnly == null || !eligibleOnly || c.isEligible())
                .filter(c -> minScore == null || c.getTotalScore() >= minScore)
                .collect(Collectors.toList());

        if (limit != null && limit > 0 && limit < filteredCandidates.size()) {
            filteredCandidates = filteredCandidates.subList(0, limit);
        }

        return new DemandRecommendationListResponse(
                demand.getId(),
                demand.getDemandCode(),
                demand.getProjectName(),
                demand.getRole(),
                demand.getLocation(),
                demand.getStartDate(),
                demand.getEndDate(),
                demand.getAllocationPercent(),
                totalEvaluated,
                eligibleCount,
                filteredCandidates
        );
    }

    @Override
    public CandidateRecommendationResponse evaluateCandidateForDemand(Long demandId, Long employeeId) {
        ProjectDemand demand = demandRepository.findByIdWithSkills(demandId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectDemand", "id", demandId));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        List<EmployeeSkill> employeeSkills = employeeSkillRepository.findByEmployeeIdWithDetails(employee.getId());
        return evaluateCandidate(demand, employee, employeeSkills);
    }

    private CandidateRecommendationResponse evaluateCandidate(ProjectDemand demand, Employee employee,
                                                              List<EmployeeSkill> employeeSkills) {
        boolean eligible = true;
        List<String> exclusionReasons = new ArrayList<>();

        // 1. Availability and Capacity Evaluation
        AvailabilityAssessmentResponse availResp = availabilityService.assessEmployeeAvailability(
                employee, demand.getStartDate(), demand.getEndDate(), demand.getAllocationPercent()
        );

        if (employee.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            eligible = false;
            exclusionReasons.add(String.format("Employee is not ACTIVE (current status: %s)", employee.getEmploymentStatus()));
        }

        if (!availResp.isAvailable()) {
            eligible = false;
            String reason = availResp.getReason();
            if (reason != null && !exclusionReasons.contains(reason) && !reason.contains("employment status")) {
                exclusionReasons.add(reason);
            }
        }

        if (availResp.getRemainingCapacityPercent() < demand.getAllocationPercent()) {
            eligible = false;
            String capMsg = String.format(
                    "Remaining capacity (%d%%) is insufficient for requested allocation (%d%%)",
                    availResp.getRemainingCapacityPercent(), demand.getAllocationPercent()
            );
            if (!exclusionReasons.contains(capMsg) && (availResp.getReason() == null || !availResp.getReason().contains("capacity"))) {
                exclusionReasons.add(capMsg);
            }
        }

        // 2. Skill Evaluation & Score Calculation
        Map<Long, EmployeeSkill> empSkillMap = employeeSkills.stream()
                .filter(es -> es.getSkill() != null)
                .collect(Collectors.toMap(es -> es.getSkill().getId(), es -> es, (a, b) -> a));

        List<SkillMatchDetail> matchedSkills = new ArrayList<>();
        List<SkillMatchDetail> missingMandatorySkills = new ArrayList<>();
        List<SkillMatchDetail> missingOptionalSkills = new ArrayList<>();

        double earnedSkillPoints = 0.0;
        double maxSkillPoints = 0.0;

        List<DemandSkill> requiredSkills = demand.getRequiredSkills() != null ? demand.getRequiredSkills() : List.of();

        if (requiredSkills.isEmpty()) {
            earnedSkillPoints = 70.0;
            maxSkillPoints = 70.0;
        } else {
            for (DemandSkill ds : requiredSkills) {
                double weightMultiplier = ds.isMandatory() ? 2.0 : 1.0;
                maxSkillPoints += weightMultiplier * 3.0; // 3.0 is max weight for ADVANCED

                EmployeeSkill empSkill = empSkillMap.get(ds.getSkill().getId());

                if (empSkill == null) {
                    SkillMatchDetail detail = new SkillMatchDetail(
                            ds.getSkill().getId(),
                            ds.getSkill().getName(),
                            ds.getSkill().getCategory(),
                            ds.isMandatory(),
                            ds.getMinimumProficiency(),
                            null,
                            false,
                            false,
                            0.0,
                            null,
                            null,
                            null
                    );

                    if (ds.isMandatory()) {
                        eligible = false;
                        exclusionReasons.add(String.format("Candidate lacks mandatory skill: %s", ds.getSkill().getName()));
                        missingMandatorySkills.add(detail);
                    } else {
                        missingOptionalSkills.add(detail);
                    }
                } else {
                    ProficiencyLevel candLevel = empSkill.getProficiencyLevel();
                    boolean meetsMin = candLevel.meetsMinimumRequirement(ds.getMinimumProficiency());

                    SkillMatchDetail detail = new SkillMatchDetail(
                            ds.getSkill().getId(),
                            ds.getSkill().getName(),
                            ds.getSkill().getCategory(),
                            ds.isMandatory(),
                            ds.getMinimumProficiency(),
                            candLevel,
                            true,
                            meetsMin,
                            empSkill.getYearsOfExperience(),
                            empSkill.getLastValidatedAt(),
                            empSkill.getValidatedBy(),
                            empSkill.getEvidenceNotes()
                    );

                    if (ds.isMandatory()) {
                        if (!meetsMin) {
                            eligible = false;
                            exclusionReasons.add(String.format(
                                    "Proficiency in mandatory skill '%s' (%s) is below required minimum (%s)",
                                    ds.getSkill().getName(), candLevel, ds.getMinimumProficiency()
                            ));
                            missingMandatorySkills.add(detail);
                        } else {
                            matchedSkills.add(detail);
                        }
                    } else {
                        if (!meetsMin) {
                            missingOptionalSkills.add(detail);
                        } else {
                            matchedSkills.add(detail);
                        }
                    }

                    earnedSkillPoints += weightMultiplier * candLevel.getLevelWeight();
                }
            }
        }

        double skillRatio = (maxSkillPoints > 0) ? (earnedSkillPoints / maxSkillPoints) : 1.0;
        double skillScore = Math.round((skillRatio * 70.0) * 10.0) / 10.0;

        // 3. Availability Score (out of 20.0)
        double availabilityScore = availResp.isAvailable() ? 20.0 : 0.0;

        // 4. Location Score (out of 10.0)
        double locationScore = 8.0;
        if (demand.getLocation().equalsIgnoreCase("Remote") ||
                (employee.getLocation() != null && employee.getLocation().equalsIgnoreCase(demand.getLocation()))) {
            locationScore = 10.0;
        }

        double totalScore = Math.round((skillScore + availabilityScore + locationScore) * 10.0) / 10.0;

        // Explainable reasoning text
        String matchExplanation;
        if (eligible) {
            matchExplanation = String.format(
                    "Eligible candidate: matches %d of %d required skills with %d%% capacity and location fit (%s). Total score: %.1f/100.",
                    matchedSkills.size(), requiredSkills.size(), availResp.getRemainingCapacityPercent(),
                    employee.getLocation(), totalScore
            );
        } else {
            matchExplanation = String.format(
                    "Ineligible: %s. Total score: %.1f/100.",
                    String.join("; ", exclusionReasons), totalScore
            );
        }

        return new CandidateRecommendationResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getName(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getLocation(),
                employee.getEmploymentStatus(),
                eligible,
                totalScore,
                skillScore,
                availabilityScore,
                locationScore,
                matchedSkills,
                missingMandatorySkills,
                missingOptionalSkills,
                availResp,
                exclusionReasons,
                matchExplanation
        );
    }
}
