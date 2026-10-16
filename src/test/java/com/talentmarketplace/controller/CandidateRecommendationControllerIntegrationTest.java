package com.talentmarketplace.controller;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.talentmarketplace.entity.AllocationStatus;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandSkill;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeSkill;
import com.talentmarketplace.entity.EmployeeUnavailability;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.ResourceAllocation;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;
import com.talentmarketplace.entity.UnavailabilityType;
import com.talentmarketplace.repository.DemandSkillRepository;
import com.talentmarketplace.config.TestDatabaseSafetyGuard;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;
import com.talentmarketplace.repository.SkillRepository;

import javax.sql.DataSource;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CandidateRecommendationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceAllocationRepository allocationRepository;

    @Autowired
    private ProjectDemandRepository demandRepository;

    @Autowired
    private DemandSkillRepository demandSkillRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private EmployeeUnavailabilityRepository unavailabilityRepository;

    private ProjectDemand demand;
    private Skill javaSkill;
    private Skill springSkill;
    private Employee eligibleEmp;
    private Employee ineligibleEmp;

    @BeforeEach
    void cleanDatabase() {
        TestDatabaseSafetyGuard.assertSafeTestDatabase(dataSource);

        allocationRepository.deleteAll();
        demandSkillRepository.deleteAll();
        demandRepository.deleteAll();
        unavailabilityRepository.deleteAll();
        employeeSkillRepository.deleteAll();
        skillRepository.deleteAll();
        employeeRepository.deleteAll();

        // Seed Skills
        javaSkill = skillRepository.save(new Skill("Java", SkillCategory.BACKEND, "Java Backend"));
        springSkill = skillRepository.save(new Skill("Spring Boot", SkillCategory.BACKEND, "Spring Microservices"));

        // Seed ProjectDemand
        demand = new ProjectDemand(
                "DEMAND-IT-01", "Digital Wallet", "Backend Engineer", "Fintech", "New York",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                50, DemandPriority.HIGH, DemandStatus.OPEN, 3.0
        );
        demand = demandRepository.save(demand);

        // Add mandatory Java (INTERMEDIATE) & optional Spring Boot (BEGINNER)
        DemandSkill dsJava = new DemandSkill(demand, javaSkill, true, ProficiencyLevel.INTERMEDIATE);
        DemandSkill dsSpring = new DemandSkill(demand, springSkill, false, ProficiencyLevel.BEGINNER);
        demandSkillRepository.save(dsJava);
        demandSkillRepository.save(dsSpring);

        // Seed Eligible Employee (meets Java ADVANCED and Spring Boot INTERMEDIATE, active, 100% capacity)
        eligibleEmp = new Employee("EMP-ELIGIBLE-01", "Diana", "Prince", "diana@example.com",
                "Fintech", "Senior Developer", "New York", LocalDate.of(2025, 1, 1), EmploymentStatus.ACTIVE, 100);
        eligibleEmp = employeeRepository.save(eligibleEmp);

        employeeSkillRepository.save(new EmployeeSkill(eligibleEmp, javaSkill, ProficiencyLevel.ADVANCED, 5.0));
        employeeSkillRepository.save(new EmployeeSkill(eligibleEmp, springSkill, ProficiencyLevel.INTERMEDIATE, 3.0));

        // Seed Ineligible Employee (only BEGINNER in Java - below minimum, and has overlapping leave)
        ineligibleEmp = new Employee("EMP-INELIGIBLE-02", "Clark", "Kent", "clark@example.com",
                "News", "Reporter", "Metropolis", LocalDate.of(2025, 1, 1), EmploymentStatus.ACTIVE, 100);
        ineligibleEmp = employeeRepository.save(ineligibleEmp);

        employeeSkillRepository.save(new EmployeeSkill(ineligibleEmp, javaSkill, ProficiencyLevel.BEGINNER, 1.0));

        // Overlapping leave during demand
        unavailabilityRepository.save(new EmployeeUnavailability(
                ineligibleEmp, LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 20),
                UnavailabilityType.ANNUAL_LEAVE, "Personal Trip"
        ));
    }

    @Test
    @DisplayName("GET /api/demands/{id}/candidates should return ranked candidates with explainable scores")
    void testGetCandidatesForDemand() throws Exception {
        mockMvc.perform(get("/api/demands/{id}/candidates", demand.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.demandId").value(demand.getId()))
                .andExpect(jsonPath("$.data.demandCode").value("DEMAND-IT-01"))
                .andExpect(jsonPath("$.data.totalCandidatesEvaluated").value(2))
                .andExpect(jsonPath("$.data.eligibleCandidatesCount").value(1))
                // First candidate must be Diana (eligible, higher score)
                .andExpect(jsonPath("$.data.candidates[0].employeeId").value(eligibleEmp.getId()))
                .andExpect(jsonPath("$.data.candidates[0].eligible").value(true))
                .andExpect(jsonPath("$.data.candidates[0].matchedSkills").isArray())
                .andExpect(jsonPath("$.data.candidates[0].missingMandatorySkills").isEmpty())
                .andExpect(jsonPath("$.data.candidates[0].availabilityAssessment.available").value(true))
                .andExpect(jsonPath("$.data.candidates[0].totalScore").isNumber())
                // Second candidate must be Clark (ineligible)
                .andExpect(jsonPath("$.data.candidates[1].employeeId").value(ineligibleEmp.getId()))
                .andExpect(jsonPath("$.data.candidates[1].eligible").value(false))
                .andExpect(jsonPath("$.data.candidates[1].missingMandatorySkills").isNotEmpty())
                .andExpect(jsonPath("$.data.candidates[1].exclusionReasons").isArray());
    }

    @Test
    @DisplayName("GET /api/demands/{id}/candidates with ?eligibleOnly=true should only return eligible candidates")
    void testGetCandidates_eligibleOnlyFilter() throws Exception {
        mockMvc.perform(get("/api/demands/{id}/candidates", demand.getId())
                        .param("eligibleOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCandidatesEvaluated").value(2))
                .andExpect(jsonPath("$.data.eligibleCandidatesCount").value(1))
                .andExpect(jsonPath("$.data.candidates.length()").value(1))
                .andExpect(jsonPath("$.data.candidates[0].employeeId").value(eligibleEmp.getId()))
                .andExpect(jsonPath("$.data.candidates[0].eligible").value(true));
    }

    @Test
    @DisplayName("GET /api/demands/{id}/recommendations alias should function identically")
    void testGetRecommendationsAlias() throws Exception {
        mockMvc.perform(get("/api/demands/{id}/recommendations", demand.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidates.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/demands/{id}/candidates/{employeeId} should return single candidate evaluation")
    void testEvaluateSingleCandidateForDemand() throws Exception {
        mockMvc.perform(get("/api/demands/{id}/candidates/{employeeId}", demand.getId(), eligibleEmp.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeId").value(eligibleEmp.getId()))
                .andExpect(jsonPath("$.data.eligible").value(true))
                .andExpect(jsonPath("$.data.matchExplanation").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/demands/9999/candidates should return 404 when demand not found")
    void testGetCandidates_demandNotFound() throws Exception {
        mockMvc.perform(get("/api/demands/{id}/candidates", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("ProjectDemand not found with id: '9999'"));
    }

    @Test
    @DisplayName("GET /api/employees/{id}/availability should evaluate employee availability successfully")
    void testGetEmployeeAvailability_success() throws Exception {
        mockMvc.perform(get("/api/employees/{id}/availability", eligibleEmp.getId())
                        .param("startDate", "2026-12-01")
                        .param("endDate", "2026-12-31")
                        .param("allocationPercent", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeId").value(eligibleEmp.getId()))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.remainingCapacityPercent").value(100));
    }

    @Test
    @DisplayName("GET /api/employees/{id}/availability should report conflict when unavailability overlaps")
    void testGetEmployeeAvailability_conflict() throws Exception {
        mockMvc.perform(get("/api/employees/{id}/availability", ineligibleEmp.getId())
                        .param("startDate", "2026-12-01")
                        .param("endDate", "2026-12-31")
                        .param("allocationPercent", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeId").value(ineligibleEmp.getId()))
                .andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.data.overlappingUnavailabilities").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/employees/{id}/availability should return 400 when start date is after end date")
    void testGetEmployeeAvailability_invalidDates() throws Exception {
        mockMvc.perform(get("/api/employees/{id}/availability", eligibleEmp.getId())
                        .param("startDate", "2026-12-31")
                        .param("endDate", "2026-12-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Candidate with PENDING allocation on demand is excluded when eligibleOnly=true and displays 'Allocation Pending' when false")
    void testCandidateWithPendingAllocationOnDemand() throws Exception {
        // Create PENDING allocation for eligibleEmp on demand
        ResourceAllocation alloc = new ResourceAllocation(
                eligibleEmp, demand, demand.getStartDate(), demand.getEndDate(),
                demand.getAllocationPercent(), AllocationStatus.PENDING, "Lead PM"
        );
        allocationRepository.save(alloc);

        // With eligibleOnly=true: eligibleEmp must be excluded
        mockMvc.perform(get("/api/demands/{id}/candidates", demand.getId())
                        .param("eligibleOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidates.length()").value(0))
                .andExpect(jsonPath("$.data.eligibleCandidatesCount").value(0));

        // With eligibleOnly=false: eligibleEmp is returned with existingAllocationStatus='PENDING' and exclusion reason
        mockMvc.perform(get("/api/demands/{id}/candidates", demand.getId())
                        .param("eligibleOnly", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidates[?(@.employeeId == " + eligibleEmp.getId() + ")].eligible").value(false))
                .andExpect(jsonPath("$.data.candidates[?(@.employeeId == " + eligibleEmp.getId() + ")].existingAllocationStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.candidates[?(@.employeeId == " + eligibleEmp.getId() + ")].exclusionReasons[0]").value("An active allocation request already exists for this demand"));
    }

    @Test
    @DisplayName("Candidate with CONFIRMED allocation on demand is excluded from eligible and displays 'Already Allocated'")
    void testCandidateWithConfirmedAllocationOnDemand() throws Exception {
        // Create CONFIRMED allocation for eligibleEmp on demand
        ResourceAllocation alloc = new ResourceAllocation(
                eligibleEmp, demand, demand.getStartDate(), demand.getEndDate(),
                demand.getAllocationPercent(), AllocationStatus.CONFIRMED, "Lead PM"
        );
        allocationRepository.save(alloc);

        mockMvc.perform(get("/api/demands/{id}/candidates", demand.getId())
                        .param("eligibleOnly", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidates[?(@.employeeId == " + eligibleEmp.getId() + ")].eligible").value(false))
                .andExpect(jsonPath("$.data.candidates[?(@.employeeId == " + eligibleEmp.getId() + ")].existingAllocationStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.candidates[?(@.employeeId == " + eligibleEmp.getId() + ")].exclusionReasons[0]").value("Employee is already allocated to this demand"));
    }

    @Test
    @DisplayName("Candidate with prior CANCELLED or REJECTED allocation on demand remains eligible")
    void testCandidateWithCancelledOrRejectedAllocation_remainsEligible() throws Exception {
        // Create CANCELLED allocation for eligibleEmp on demand
        ResourceAllocation cancelledAlloc = new ResourceAllocation(
                eligibleEmp, demand, demand.getStartDate(), demand.getEndDate(),
                demand.getAllocationPercent(), AllocationStatus.CANCELLED, "Lead PM"
        );
        allocationRepository.save(cancelledAlloc);

        // Create REJECTED allocation for eligibleEmp on demand
        ResourceAllocation rejectedAlloc = new ResourceAllocation(
                eligibleEmp, demand, demand.getStartDate(), demand.getEndDate(),
                demand.getAllocationPercent(), AllocationStatus.REJECTED, "Lead PM"
        );
        allocationRepository.save(rejectedAlloc);

        // Neither CANCELLED nor REJECTED blocks eligibleEmp
        mockMvc.perform(get("/api/demands/{id}/candidates", demand.getId())
                        .param("eligibleOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidates[0].employeeId").value(eligibleEmp.getId()))
                .andExpect(jsonPath("$.data.candidates[0].eligible").value(true))
                .andExpect(jsonPath("$.data.candidates[0].existingAllocationStatus").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("Candidate allocated to a different demand is not globally hidden and remains eligible when partial capacity remains")
    void testCandidateAllocatedToDifferentDemand_eligibleWhenPartialCapacityRemains() throws Exception {
        // Create another demand
        ProjectDemand otherDemand = demandRepository.save(new ProjectDemand(
                "OTHER-DEM-01", "Core Gateway", "Java Dev", "Fintech", "New York",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                40, DemandPriority.MEDIUM, DemandStatus.OPEN, 2.0
        ));

        // Confirm eligibleEmp on otherDemand for 40%
        ResourceAllocation otherAlloc = new ResourceAllocation(
                eligibleEmp, otherDemand, otherDemand.getStartDate(), otherDemand.getEndDate(),
                40, AllocationStatus.CONFIRMED, "Manager"
        );
        allocationRepository.save(otherAlloc);

        // Target demand needs 50%. Total capacity is 100%. Remaining capacity is 60% >= 50% requested.
        // Employee must NOT be globally hidden and must be eligible!
        mockMvc.perform(get("/api/demands/{id}/candidates", demand.getId())
                        .param("eligibleOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidates.length()").value(1))
                .andExpect(jsonPath("$.data.candidates[0].employeeId").value(eligibleEmp.getId()))
                .andExpect(jsonPath("$.data.candidates[0].eligible").value(true))
                .andExpect(jsonPath("$.data.candidates[0].existingAllocationStatus").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data.candidates[0].availabilityAssessment.remainingCapacityPercent").value(60));
    }

    @Test
    @DisplayName("Candidate allocated to a different demand is marked ineligible when remaining capacity is insufficient")
    void testCandidateAllocatedToDifferentDemand_ineligibleWhenCapacityInsufficient() throws Exception {
        // Create another demand
        ProjectDemand otherDemand = demandRepository.save(new ProjectDemand(
                "OTHER-DEM-02", "Heavy Service", "Java Dev", "Fintech", "New York",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                70, DemandPriority.HIGH, DemandStatus.OPEN, 3.0
        ));

        // Confirm eligibleEmp on otherDemand for 70%
        ResourceAllocation otherAlloc = new ResourceAllocation(
                eligibleEmp, otherDemand, otherDemand.getStartDate(), otherDemand.getEndDate(),
                70, AllocationStatus.CONFIRMED, "Manager"
        );
        allocationRepository.save(otherAlloc);

        // Target demand needs 50%. Remaining capacity is 30% < 50% requested.
        mockMvc.perform(get("/api/demands/{id}/candidates", demand.getId())
                        .param("eligibleOnly", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidates[0].employeeId").value(eligibleEmp.getId()))
                .andExpect(jsonPath("$.data.candidates[0].eligible").value(false))
                .andExpect(jsonPath("$.data.candidates[0].existingAllocationStatus").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data.candidates[0].availabilityAssessment.remainingCapacityPercent").value(30));
    }

    @Test
    @DisplayName("No eligible candidates returns HTTP 200 with valid JSON and empty array")
    void testNoEligibleCandidates_returnsValidJsonWithEmptyList() throws Exception {
        ProjectDemand highDemand = demandRepository.save(new ProjectDemand(
                "HIGH-DEM-01", "Demanding Project", "Architect", "Fintech", "New York",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                100, DemandPriority.HIGH, DemandStatus.OPEN, 3.0
        ));
        Skill ruby = skillRepository.save(new Skill("Ruby", SkillCategory.BACKEND, "Ruby Programming"));
        demandSkillRepository.save(new DemandSkill(highDemand, ruby, true, ProficiencyLevel.ADVANCED));

        mockMvc.perform(get("/api/demands/{id}/candidates", highDemand.getId())
                        .param("eligibleOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.demandId").value(highDemand.getId()))
                .andExpect(jsonPath("$.data.totalCandidatesEvaluated").value(2))
                .andExpect(jsonPath("$.data.eligibleCandidatesCount").value(0))
                .andExpect(jsonPath("$.data.candidates").isArray())
                .andExpect(jsonPath("$.data.candidates").isEmpty());
    }

    @Test
    @DisplayName("Invalid non-existent demand ID returns HTTP 404 JSON ErrorResponse")
    void testInvalidDemandId_returns404JsonErrorResponse() throws Exception {
        mockMvc.perform(get("/api/demands/{id}/candidates", 999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ProjectDemand not found")));
    }

    @Test
    @DisplayName("Negative minScore parameter returns HTTP 400 JSON ErrorResponse")
    void testNegativeMinScore_returns400JsonErrorResponse() throws Exception {
        mockMvc.perform(get("/api/demands/{id}/candidates", demand.getId())
                        .param("minScore", "-5.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Minimum match score must be between 0 and 100"));
    }

    @Test
    @DisplayName("Invalid limit parameter <= 0 returns HTTP 400 JSON ErrorResponse")
    void testInvalidLimit_returns400JsonErrorResponse() throws Exception {
        mockMvc.perform(get("/api/demands/{id}/candidates", demand.getId())
                        .param("limit", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Limit must be greater than 0"));
    }

    @Test
    @DisplayName("Non-numeric demand ID returns HTTP 400 JSON ErrorResponse")
    void testNonNumericDemandId_returns400JsonErrorResponse() throws Exception {
        mockMvc.perform(get("/api/demands/not-a-number/candidates")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Parameter 'id' should be of type Long")));
    }
}
