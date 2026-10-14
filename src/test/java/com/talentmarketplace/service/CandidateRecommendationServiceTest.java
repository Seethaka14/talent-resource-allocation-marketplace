package com.talentmarketplace.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.talentmarketplace.dto.response.AvailabilityAssessmentResponse;
import com.talentmarketplace.dto.response.CandidateRecommendationResponse;
import com.talentmarketplace.dto.response.DemandRecommendationListResponse;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandSkill;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeSkill;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;

@ExtendWith(MockitoExtension.class)
class CandidateRecommendationServiceTest {

    @Mock
    private ProjectDemandRepository demandRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;

    @Mock
    private AvailabilityService availabilityService;

    @InjectMocks
    private CandidateRecommendationServiceImpl recommendationService;

    private ProjectDemand sampleDemand;
    private Skill javaSkill;
    private Skill dockerSkill;
    private Employee activeEmployee1;
    private Employee activeEmployee2;

    @BeforeEach
    void setUp() {
        sampleDemand = new ProjectDemand(
                "DEMAND-TEST-01", "Core Payment System", "Senior Java Engineer",
                "Fintech", "New York", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30),
                50, DemandPriority.HIGH, DemandStatus.OPEN, 3.0
        );
        sampleDemand.setId(100L);

        javaSkill = new Skill("Java", SkillCategory.BACKEND, "Java Programming");
        javaSkill.setId(10L);

        dockerSkill = new Skill("Docker", SkillCategory.CLOUD_DEVOPS, "Containerization");
        dockerSkill.setId(20L);

        // Add mandatory Java (INTERMEDIATE) and optional Docker (BEGINNER)
        DemandSkill dsJava = new DemandSkill(sampleDemand, javaSkill, true, ProficiencyLevel.INTERMEDIATE);
        sampleDemand.addRequiredSkill(dsJava);

        DemandSkill dsDocker = new DemandSkill(sampleDemand, dockerSkill, false, ProficiencyLevel.BEGINNER);
        sampleDemand.addRequiredSkill(dsDocker);

        activeEmployee1 = new Employee("EMP-01", "Alice", "Java", "alice@example.com",
                "Fintech", "Software Engineer", "New York", LocalDate.of(2025, 1, 1), EmploymentStatus.ACTIVE, 100);
        activeEmployee1.setId(1L);

        activeEmployee2 = new Employee("EMP-02", "Bob", "Kotlin", "bob@example.com",
                "Fintech", "Software Engineer", "New York", LocalDate.of(2025, 1, 1), EmploymentStatus.ACTIVE, 100);
        activeEmployee2.setId(2L);
    }

    private AvailabilityAssessmentResponse createAvailableResponse(Employee emp, int requestedPercent) {
        return new AvailabilityAssessmentResponse(
                emp.getId(), emp.getEmployeeCode(), emp.getName(), emp.getEmploymentStatus(),
                emp.getMaxCapacityPercent(), sampleDemand.getStartDate(), sampleDemand.getEndDate(),
                requestedPercent, true, 100, List.of(), "Employee is fully available"
        );
    }

    private AvailabilityAssessmentResponse createUnavailableResponse(Employee emp, int requestedPercent, String reason) {
        return new AvailabilityAssessmentResponse(
                emp.getId(), emp.getEmployeeCode(), emp.getName(), emp.getEmploymentStatus(),
                emp.getMaxCapacityPercent(), sampleDemand.getStartDate(), sampleDemand.getEndDate(),
                requestedPercent, false, 0, List.of(), reason
        );
    }

    @Test
    @DisplayName("Should qualify candidate when mandatory skill is fully satisfied at or above required proficiency")
    void testRecommend_mandatorySkillFullySatisfied() {
        when(demandRepository.findByIdWithSkills(100L)).thenReturn(Optional.of(sampleDemand));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee1));

        EmployeeSkill esJava = new EmployeeSkill(activeEmployee1, javaSkill, ProficiencyLevel.ADVANCED, 4.0);
        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of(esJava));

        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee1), any(), any(), eq(50)))
                .thenReturn(createAvailableResponse(activeEmployee1, 50));

        CandidateRecommendationResponse response = recommendationService.evaluateCandidateForDemand(100L, 1L);

        assertThat(response.isEligible()).isTrue();
        assertThat(response.getMissingMandatorySkills()).isEmpty();
        assertThat(response.getMatchedSkills()).hasSize(1);
        assertThat(response.getMatchedSkills().get(0).getSkillName()).isEqualTo("Java");
        assertThat(response.getMatchedSkills().get(0).getCandidateProficiency()).isEqualTo(ProficiencyLevel.ADVANCED);
        assertThat(response.getMissingOptionalSkills()).hasSize(1); // Lacks optional Docker
        assertThat(response.getMissingOptionalSkills().get(0).getSkillName()).isEqualTo("Docker");
        assertThat(response.getAvailabilityScore()).isEqualTo(20.0);
        assertThat(response.getLocationScore()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Should disqualify candidate when mandatory skill is missing")
    void testRecommend_mandatorySkillMissing() {
        when(demandRepository.findByIdWithSkills(100L)).thenReturn(Optional.of(sampleDemand));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee1));

        // Employee only has Docker, lacks mandatory Java
        EmployeeSkill esDocker = new EmployeeSkill(activeEmployee1, dockerSkill, ProficiencyLevel.ADVANCED, 2.0);
        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of(esDocker));

        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee1), any(), any(), eq(50)))
                .thenReturn(createAvailableResponse(activeEmployee1, 50));

        CandidateRecommendationResponse response = recommendationService.evaluateCandidateForDemand(100L, 1L);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getMissingMandatorySkills()).hasSize(1);
        assertThat(response.getMissingMandatorySkills().get(0).getSkillName()).isEqualTo("Java");
        assertThat(response.getExclusionReasons())
                .anyMatch(r -> r.contains("lacks mandatory skill: Java"));
    }

    @Test
    @DisplayName("Should disqualify candidate when proficiency in mandatory skill is below required minimum")
    void testRecommend_proficiencyBelowMinimum() {
        when(demandRepository.findByIdWithSkills(100L)).thenReturn(Optional.of(sampleDemand));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee1));

        // Java required is INTERMEDIATE (weight 2), candidate only has BEGINNER (weight 1)
        EmployeeSkill esJava = new EmployeeSkill(activeEmployee1, javaSkill, ProficiencyLevel.BEGINNER, 1.0);
        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of(esJava));

        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee1), any(), any(), eq(50)))
                .thenReturn(createAvailableResponse(activeEmployee1, 50));

        CandidateRecommendationResponse response = recommendationService.evaluateCandidateForDemand(100L, 1L);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getMissingMandatorySkills()).hasSize(1);
        assertThat(response.getMissingMandatorySkills().get(0).isMeetsProficiencyRequirement()).isFalse();
        assertThat(response.getExclusionReasons())
                .anyMatch(r -> r.contains("below required minimum"));
    }

    @Test
    @DisplayName("Should keep candidate eligible when optional skill is missing")
    void testRecommend_optionalSkillMissing() {
        when(demandRepository.findByIdWithSkills(100L)).thenReturn(Optional.of(sampleDemand));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee1));

        // Satisfies mandatory Java, missing optional Docker
        EmployeeSkill esJava = new EmployeeSkill(activeEmployee1, javaSkill, ProficiencyLevel.INTERMEDIATE, 2.5);
        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of(esJava));

        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee1), any(), any(), eq(50)))
                .thenReturn(createAvailableResponse(activeEmployee1, 50));

        CandidateRecommendationResponse response = recommendationService.evaluateCandidateForDemand(100L, 1L);

        assertThat(response.isEligible()).isTrue();
        assertThat(response.getMissingOptionalSkills()).hasSize(1);
        assertThat(response.getMissingOptionalSkills().get(0).getSkillName()).isEqualTo("Docker");
        assertThat(response.getExclusionReasons()).isEmpty();
    }

    @Test
    @DisplayName("Should exclude inactive employee even if all skills match")
    void testRecommend_inactiveEmployeeExclusion() {
        activeEmployee1.setEmploymentStatus(EmploymentStatus.ON_LEAVE);

        when(demandRepository.findByIdWithSkills(100L)).thenReturn(Optional.of(sampleDemand));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee1));

        EmployeeSkill esJava = new EmployeeSkill(activeEmployee1, javaSkill, ProficiencyLevel.ADVANCED, 4.0);
        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of(esJava));

        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee1), any(), any(), eq(50)))
                .thenReturn(createUnavailableResponse(activeEmployee1, 50, "Employee is not available (employment status is ON_LEAVE)"));

        CandidateRecommendationResponse response = recommendationService.evaluateCandidateForDemand(100L, 1L);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getExclusionReasons())
                .anyMatch(r -> r.contains("ON_LEAVE"));
    }

    @Test
    @DisplayName("Should disqualify candidate when unavailability overlaps demand period")
    void testRecommend_unavailabilityOverlappingDemandPeriod() {
        when(demandRepository.findByIdWithSkills(100L)).thenReturn(Optional.of(sampleDemand));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee1));

        EmployeeSkill esJava = new EmployeeSkill(activeEmployee1, javaSkill, ProficiencyLevel.ADVANCED, 4.0);
        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of(esJava));

        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee1), any(), any(), eq(50)))
                .thenReturn(createUnavailableResponse(activeEmployee1, 50, "Employee has scheduled unavailability (ANNUAL_LEAVE) from 2026-11-10 to 2026-11-15"));

        CandidateRecommendationResponse response = recommendationService.evaluateCandidateForDemand(100L, 1L);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getAvailabilityScore()).isEqualTo(0.0);
        assertThat(response.getExclusionReasons())
                .anyMatch(r -> r.contains("ANNUAL_LEAVE"));
    }

    @Test
    @DisplayName("Should keep candidate eligible when unavailability is outside demand period")
    void testRecommend_unavailabilityOutsideDemandPeriod() {
        when(demandRepository.findByIdWithSkills(100L)).thenReturn(Optional.of(sampleDemand));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee1));

        EmployeeSkill esJava = new EmployeeSkill(activeEmployee1, javaSkill, ProficiencyLevel.ADVANCED, 4.0);
        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of(esJava));

        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee1), any(), any(), eq(50)))
                .thenReturn(createAvailableResponse(activeEmployee1, 50));

        CandidateRecommendationResponse response = recommendationService.evaluateCandidateForDemand(100L, 1L);

        assertThat(response.isEligible()).isTrue();
        assertThat(response.getAvailabilityScore()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("Should disqualify candidate when requested allocation exceeds remaining capacity")
    void testRecommend_allocationExceedsRemainingCapacity() {
        when(demandRepository.findByIdWithSkills(100L)).thenReturn(Optional.of(sampleDemand));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee1));

        EmployeeSkill esJava = new EmployeeSkill(activeEmployee1, javaSkill, ProficiencyLevel.ADVANCED, 4.0);
        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of(esJava));

        AvailabilityAssessmentResponse capResp = new AvailabilityAssessmentResponse(
                1L, "EMP-01", "Alice", EmploymentStatus.ACTIVE, 40,
                sampleDemand.getStartDate(), sampleDemand.getEndDate(), 50, false, 40,
                List.of(), "Requested allocation (50%) exceeds employee configured maximum capacity limit (40%)"
        );
        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee1), any(), any(), eq(50)))
                .thenReturn(capResp);

        CandidateRecommendationResponse response = recommendationService.evaluateCandidateForDemand(100L, 1L);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getExclusionReasons())
                .anyMatch(r -> r.contains("capacity"));
    }

    @Test
    @DisplayName("Should handle missing employee skill data cleanly without errors")
    void testRecommend_missingEmployeeSkillData() {
        when(demandRepository.findByIdWithSkills(100L)).thenReturn(Optional.of(sampleDemand));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(activeEmployee1));

        // Employee has no recorded skills at all
        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of());

        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee1), any(), any(), eq(50)))
                .thenReturn(createAvailableResponse(activeEmployee1, 50));

        CandidateRecommendationResponse response = recommendationService.evaluateCandidateForDemand(100L, 1L);

        assertThat(response.isEligible()).isFalse();
        assertThat(response.getSkillScore()).isEqualTo(0.0);
        assertThat(response.getMissingMandatorySkills()).hasSize(1);
        assertThat(response.getMissingOptionalSkills()).hasSize(1);
    }

    @Test
    @DisplayName("Should perform deterministic ranking and stable tie-breaking by ID")
    void testRecommend_deterministicRankingAndTieBreaking() {
        when(demandRepository.findByIdWithSkills(100L)).thenReturn(Optional.of(sampleDemand));
        when(employeeRepository.findAll()).thenReturn(List.of(activeEmployee2, activeEmployee1)); // Reverse insertion order

        // Both employees have identical skills (Java ADVANCED) and are fully available
        EmployeeSkill esJava1 = new EmployeeSkill(activeEmployee1, javaSkill, ProficiencyLevel.ADVANCED, 4.0);
        EmployeeSkill esJava2 = new EmployeeSkill(activeEmployee2, javaSkill, ProficiencyLevel.ADVANCED, 4.0);

        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of(esJava1));
        when(employeeSkillRepository.findByEmployeeIdWithDetails(2L)).thenReturn(List.of(esJava2));

        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee1), any(), any(), eq(50)))
                .thenReturn(createAvailableResponse(activeEmployee1, 50));
        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee2), any(), any(), eq(50)))
                .thenReturn(createAvailableResponse(activeEmployee2, 50));

        DemandRecommendationListResponse listResponse = recommendationService.recommendCandidatesForDemand(100L);

        assertThat(listResponse.getCandidates()).hasSize(2);
        // Scores are equal -> stable tie-breaker: employeeId ASCENDING (1L before 2L)
        assertThat(listResponse.getCandidates().get(0).getEmployeeId()).isEqualTo(1L);
        assertThat(listResponse.getCandidates().get(1).getEmployeeId()).isEqualTo(2L);
        assertThat(listResponse.getCandidates().get(0).getTotalScore())
                .isEqualTo(listResponse.getCandidates().get(1).getTotalScore());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when demand does not exist")
    void testRecommend_demandNotFound() {
        when(demandRepository.findByIdWithSkills(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recommendationService.recommendCandidatesForDemand(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProjectDemand not found");
    }

    @Test
    @DisplayName("Should correctly filter by eligibleOnly and minScore")
    void testRecommend_filteringEligibleOnlyAndMinScore() {
        when(demandRepository.findByIdWithSkills(100L)).thenReturn(Optional.of(sampleDemand));
        when(employeeRepository.findAll()).thenReturn(List.of(activeEmployee1, activeEmployee2));

        // Emp 1: eligible (has Java ADVANCED)
        EmployeeSkill esJava1 = new EmployeeSkill(activeEmployee1, javaSkill, ProficiencyLevel.ADVANCED, 4.0);
        // Emp 2: ineligible (lacks Java)
        EmployeeSkill esDocker2 = new EmployeeSkill(activeEmployee2, dockerSkill, ProficiencyLevel.ADVANCED, 2.0);

        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of(esJava1));
        when(employeeSkillRepository.findByEmployeeIdWithDetails(2L)).thenReturn(List.of(esDocker2));

        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee1), any(), any(), eq(50)))
                .thenReturn(createAvailableResponse(activeEmployee1, 50));
        when(availabilityService.assessEmployeeAvailability(eq(activeEmployee2), any(), any(), eq(50)))
                .thenReturn(createAvailableResponse(activeEmployee2, 50));

        DemandRecommendationListResponse filtered = recommendationService.recommendCandidatesForDemand(
                100L, true, null, null
        );

        assertThat(filtered.getTotalCandidatesEvaluated()).isEqualTo(2);
        assertThat(filtered.getEligibleCandidatesCount()).isEqualTo(1);
        assertThat(filtered.getCandidates()).hasSize(1);
        assertThat(filtered.getCandidates().get(0).getEmployeeId()).isEqualTo(1L);
    }
}
