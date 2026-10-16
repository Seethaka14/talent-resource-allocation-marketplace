package com.talentmarketplace.controller;

import java.time.LocalDate;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.talentmarketplace.config.TestDatabaseSafetyGuard;
import com.talentmarketplace.dto.request.CreateAllocationRequest;
import com.talentmarketplace.dto.request.RejectAllocationRequest;
import com.talentmarketplace.entity.AllocationStatus;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandSkill;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;
import com.talentmarketplace.repository.DemandSkillRepository;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;
import com.talentmarketplace.repository.SkillRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ResourceAllocationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private Employee employee;
    private ProjectDemand demand;

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

        employee = new Employee("EMP-ALLOC-01", "Grace", "Hopper", "grace.hopper@example.com",
                "Engineering", "Chief Scientist", "New York", LocalDate.of(2025, 1, 1),
                EmploymentStatus.ACTIVE, 100);
        employee = employeeRepository.save(employee);

        demand = new ProjectDemand("DEM-ALLOC-01", "Compiler Modernization", "Compiler Engineer",
                "Engineering", "New York", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                60, DemandPriority.CRITICAL, DemandStatus.OPEN, 5.0);
        demand = demandRepository.save(demand);
    }

    @Test
    @DisplayName("Full Allocation Lifecycle: Create -> Confirm -> Revalidate -> Cancel -> Restore Capacity")
    void testFullAllocationLifecycle() throws Exception {
        // 1. Create Pending Allocation (60% allocation)
        CreateAllocationRequest createReq = new CreateAllocationRequest(
                demand.getId(), employee.getId(), LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 60, "Lead Architect"
        );

        String createRespJson = mockMvc.perform(post("/api/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.allocationPercent").value(60))
                .andReturn().getResponse().getContentAsString();

        Long allocationId = objectMapper.readTree(createRespJson).path("data").path("id").asLong();

        // 2. Confirm the Allocation
        mockMvc.perform(post("/api/allocations/{id}/confirm", allocationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.confirmedBy").value("Manager"));

        // 3. Verify availability reflects 40% remaining capacity (100 - 60 = 40%)
        mockMvc.perform(get("/api/employees/{id}/availability", employee.getId())
                        .param("startDate", "2026-12-01")
                        .param("endDate", "2026-12-31")
                        .param("allocationPercent", "40")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.remainingCapacityPercent").value(40));

        // 4. Attempting to allocate 50% more should now fail because only 40% is available
        ProjectDemand secondDemand = demandRepository.save(new ProjectDemand(
                "DEM-ALLOC-02", "Runtime Optimization", "Systems Engineer", "Engineering",
                "New York", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                50, DemandPriority.HIGH, DemandStatus.OPEN, 3.0
        ));

        CreateAllocationRequest secondReq = new CreateAllocationRequest(
                secondDemand.getId(), employee.getId(), LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50, "Lead Architect"
        );

        mockMvc.perform(post("/api/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // 5. Cancel the first allocation
        mockMvc.perform(post("/api/allocations/{id}/cancel", allocationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // 6. Capacity is immediately restored to 100%
        mockMvc.perform(get("/api/employees/{id}/availability", employee.getId())
                        .param("startDate", "2026-12-01")
                        .param("endDate", "2026-12-31")
                        .param("allocationPercent", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.remainingCapacityPercent").value(100));
    }

    @Test
    @DisplayName("Should successfully reject an allocation with reason")
    void testRejectAllocation() throws Exception {
        CreateAllocationRequest createReq = new CreateAllocationRequest(
                demand.getId(), employee.getId(), LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 40, "Staffing Coordinator"
        );

        String createRespJson = mockMvc.perform(post("/api/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long allocationId = objectMapper.readTree(createRespJson).path("data").path("id").asLong();

        RejectAllocationRequest rejectReq = new RejectAllocationRequest("Candidate reserved for critical client work", "Director of Staffing");

        mockMvc.perform(post("/api/allocations/{id}/reject", allocationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("Candidate reserved for critical client work"));
    }

    @Test
    @DisplayName("Should return 404 when allocation ID does not exist")
    void testGetAllocation_notFound() throws Exception {
        mockMvc.perform(get("/api/allocations/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/allocations: returns all allocations with details and supports status filter")
    void testGetAllAllocations() throws Exception {
        CreateAllocationRequest createReq = new CreateAllocationRequest(
                demand.getId(), employee.getId(), LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 40, "Staffing Coordinator"
        );
        mockMvc.perform(post("/api/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        // Test without status filter
        mockMvc.perform(get("/api/allocations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].employeeCode").value("EMP-ALLOC-01"))
                .andExpect(jsonPath("$.data[0].demandCode").value("DEM-ALLOC-01"));

        // Test with matching status filter
        mockMvc.perform(get("/api/allocations")
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        // Test with non-matching status filter
        mockMvc.perform(get("/api/allocations")
                        .param("status", "CONFIRMED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("POST /api/allocations: should reject duplicate active allocation for the same employee and demand")
    void testCreateAllocation_duplicateActiveAllocation_rejected() throws Exception {
        CreateAllocationRequest firstReq = new CreateAllocationRequest(
                demand.getId(), employee.getId(), LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 40, "Staffing Lead"
        );

        mockMvc.perform(post("/api/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        // Second request for same employee and demand must be rejected as duplicate
        CreateAllocationRequest secondReq = new CreateAllocationRequest(
                demand.getId(), employee.getId(), LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 40, "Staffing Lead"
        );

        mockMvc.perform(post("/api/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("An active allocation request (PENDING or CONFIRMED) already exists")));
    }

    @Test
    @DisplayName("POST /api/allocations/{id}/confirm: should reject confirmation if employee lacks mandatory skill required by demand")
    void testConfirmAllocation_missingMandatorySkill_rejected() throws Exception {
        // Create skill and demand with mandatory skill
        Skill java = skillRepository.save(new Skill("Java", SkillCategory.BACKEND, "Java Core"));
        ProjectDemand skillDemand = demandRepository.save(new ProjectDemand(
                "DEM-SKILL-01", "Core API", "Backend Lead", "Engineering",
                "New York", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                50, DemandPriority.HIGH, DemandStatus.OPEN, 3.0
        ));
        demandSkillRepository.save(new DemandSkill(skillDemand, java, true, ProficiencyLevel.ADVANCED));

        // Create allocation (employee does not have Java skill)
        CreateAllocationRequest req = new CreateAllocationRequest(
                skillDemand.getId(), employee.getId(), LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50, "Lead Architect"
        );

        String resp = mockMvc.perform(post("/api/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long allocationId = objectMapper.readTree(resp).path("data").path("id").asLong();

        // Attempting to confirm must fail because employee lacks Java mandatory skill
        mockMvc.perform(post("/api/allocations/{id}/confirm", allocationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Employee lacks mandatory skill requirement: Java")));
    }

    @Test
    @DisplayName("POST /api/allocations/{id}/complete: should mark confirmed allocation as COMPLETED and preserve history")
    void testCompleteAllocation_endpoint_success() throws Exception {
        CreateAllocationRequest req = new CreateAllocationRequest(
                demand.getId(), employee.getId(), LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50, "Lead Architect"
        );
        req.setAutoConfirm(true);

        String resp = mockMvc.perform(post("/api/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long allocationId = objectMapper.readTree(resp).path("data").path("id").asLong();

        // Complete the allocation
        mockMvc.perform(post("/api/allocations/{id}/complete", allocationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // Allocation record is still queryable in history
        mockMvc.perform(get("/api/allocations/{id}", allocationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /api/allocations/{id}/cancel: should preserve allocation history record in CANCELLED status")
    void testCancelAllocation_endpoint_preservesRecord() throws Exception {
        CreateAllocationRequest req = new CreateAllocationRequest(
                demand.getId(), employee.getId(), LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50, "Lead Architect"
        );

        String resp = mockMvc.perform(post("/api/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long allocationId = objectMapper.readTree(resp).path("data").path("id").asLong();

        // Cancel the allocation
        mockMvc.perform(post("/api/allocations/{id}/cancel", allocationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Project scope changed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // Querying employee allocations still returns the cancelled allocation in history
        mockMvc.perform(get("/api/allocations/employee/{employeeId}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$.data[0].rejectionReason").value("Project scope changed"));
    }
}
