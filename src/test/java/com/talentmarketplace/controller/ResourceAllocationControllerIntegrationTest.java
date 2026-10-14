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
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.repository.DemandSkillRepository;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;
import com.talentmarketplace.repository.SkillRepository;

@SpringBootTest
@AutoConfigureMockMvc
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
}
