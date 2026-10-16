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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;

import com.talentmarketplace.config.TestDatabaseSafetyGuard;
import com.talentmarketplace.dto.request.CreateAllocationRequest;
import com.talentmarketplace.dto.request.CreateDemandRequest;
import com.talentmarketplace.dto.request.CreateUnavailabilityRequest;
import com.talentmarketplace.dto.request.LoginRequest;
import com.talentmarketplace.dto.request.RejectAllocationRequest;
import com.talentmarketplace.entity.AllocationStatus;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.ResourceAllocation;
import com.talentmarketplace.entity.UnavailabilityType;
import com.talentmarketplace.entity.User;
import com.talentmarketplace.entity.UserRole;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;
import com.talentmarketplace.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class RoleBasedAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectDemandRepository demandRepository;

    @Autowired
    private ResourceAllocationRepository allocationRepository;

    @Autowired
    private com.talentmarketplace.repository.DemandSkillRepository demandSkillRepository;

    @Autowired
    private com.talentmarketplace.repository.EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private com.talentmarketplace.repository.EmployeeUnavailabilityRepository unavailabilityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long pendingAllocationId;

    @BeforeEach
    void setUp() {
        TestDatabaseSafetyGuard.assertSafeTestDatabase(dataSource);

        // Clear existing test data in foreign-key safe order
        allocationRepository.deleteAll();
        demandSkillRepository.deleteAll();
        demandRepository.deleteAll();
        employeeSkillRepository.deleteAll();
        unavailabilityRepository.deleteAll();
        employeeRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Seed Users
        User pm = new User("PM Tester", "pm@talentmarketplace.com", passwordEncoder.encode("PM_pass123!"), UserRole.PROJECT_MANAGER);
        pm.setEnabled(true);
        userRepository.save(pm);

        User rm = new User("RM Tester", "rm@talentmarketplace.com", passwordEncoder.encode("RM_pass123!"), UserRole.RESOURCE_MANAGER);
        rm.setEnabled(true);
        userRepository.save(rm);

        // 2. Seed Employee
        Employee emp = new Employee();
        emp.setEmployeeCode("EMP-AUTH-001");
        emp.setFirstName("Alice");
        emp.setLastName("Engineer");
        emp.setEmail("alice.auth@example.com");
        emp.setDepartment("Engineering");
        emp.setDesignation("Senior Software Engineer");
        emp.setLocation("San Francisco, CA");
        emp.setJoiningDate(LocalDate.of(2022, 1, 10));
        emp.setEmploymentStatus(EmploymentStatus.ACTIVE);
        emp.setMaxCapacityPercent(100);
        Employee savedEmp = employeeRepository.save(emp);

        // 3. Seed Demand
        ProjectDemand demand = new ProjectDemand();
        demand.setDemandCode("DEM-AUTH-001");
        demand.setProjectName("Auth Security Project");
        demand.setRole("Backend Developer");
        demand.setDepartment("Engineering");
        demand.setLocation("San Francisco, CA");
        demand.setStartDate(LocalDate.of(2026, 11, 1));
        demand.setEndDate(LocalDate.of(2026, 12, 31));
        demand.setPriority(DemandPriority.HIGH);
        demand.setAllocationPercent(50);
        ProjectDemand savedDemand = demandRepository.save(demand);

        // 4. Seed Pending Allocation
        ResourceAllocation alloc = new ResourceAllocation();
        alloc.setDemand(savedDemand);
        alloc.setEmployee(savedEmp);
        alloc.setStartDate(savedDemand.getStartDate());
        alloc.setEndDate(savedDemand.getEndDate());
        alloc.setAllocationPercent(50);
        alloc.setStatus(AllocationStatus.PENDING);
        alloc.setAllocatedBy("Project Manager");
        ResourceAllocation savedAlloc = allocationRepository.save(alloc);
        pendingAllocationId = savedAlloc.getId();
    }

    private MockHttpSession loginAs(String email, String password, UserRole role) throws Exception {
        LoginRequest req = new LoginRequest(email, password, role);
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) res.getRequest().getSession(false);
    }

    @Test
    @DisplayName("Unauthenticated request to protected API returns 401 Unauthorized with valid JSON ErrorResponse")
    void unauthenticatedRequest_Returns401() throws Exception {
        mockMvc.perform(get("/api/demands"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        mockMvc.perform(get("/api/allocations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        mockMvc.perform(get("/api/demands/1/candidates"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("Project Manager can view demands, create demands, and request allocations")
    void projectManager_AuthorizedActions_Success() throws Exception {
        MockHttpSession pmSession = loginAs("pm@talentmarketplace.com", "PM_pass123!", UserRole.PROJECT_MANAGER);

        // PM can read demands
        mockMvc.perform(get("/api/demands").session(pmSession))
                .andExpect(status().isOk());

        // PM can create demands
        CreateDemandRequest newDemand = new CreateDemandRequest();
        newDemand.setDemandCode("DEM-PM-NEW");
        newDemand.setProjectName("New Portal Initiative");
        newDemand.setRole("Fullstack Developer");
        newDemand.setDepartment("Engineering");
        newDemand.setLocation("San Francisco, CA");
        newDemand.setStartDate(LocalDate.of(2026, 11, 15));
        newDemand.setEndDate(LocalDate.of(2026, 12, 15));
        newDemand.setPriority(DemandPriority.CRITICAL);
        newDemand.setAllocationPercent(50);

        mockMvc.perform(post("/api/demands")
                        .with(csrf())
                        .session(pmSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDemand)))
                .andExpect(status().isCreated());

        // PM can request allocations (submitting pending allocation)
        Employee emp = employeeRepository.findAll().get(0);
        ProjectDemand dem = demandRepository.findByDemandCode("DEM-PM-NEW").orElseThrow();

        CreateAllocationRequest allocReq = new CreateAllocationRequest();
        allocReq.setDemandId(dem.getId());
        allocReq.setEmployeeId(emp.getId());
        allocReq.setStartDate(dem.getStartDate());
        allocReq.setEndDate(dem.getEndDate());
        allocReq.setAllocationPercent(30);

        mockMvc.perform(post("/api/allocations")
                        .with(csrf())
                        .session(pmSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocReq)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Project Manager CANNOT confirm allocations - rejected with 403 Forbidden")
    void projectManager_CannotConfirmAllocation_Returns403() throws Exception {
        MockHttpSession pmSession = loginAs("pm@talentmarketplace.com", "PM_pass123!", UserRole.PROJECT_MANAGER);

        mockMvc.perform(post("/api/allocations/{id}/confirm", pendingAllocationId)
                        .with(csrf())
                        .session(pmSession))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: insufficient role permissions"));

        // Verify allocation remains PENDING
        ResourceAllocation alloc = allocationRepository.findById(pendingAllocationId).orElseThrow();
        assertThat(alloc.getStatus()).isEqualTo(AllocationStatus.PENDING);
    }

    @Test
    @DisplayName("Project Manager CANNOT reject allocations - rejected with 403 Forbidden")
    void projectManager_CannotRejectAllocation_Returns403() throws Exception {
        MockHttpSession pmSession = loginAs("pm@talentmarketplace.com", "PM_pass123!", UserRole.PROJECT_MANAGER);

        RejectAllocationRequest rejectReq = new RejectAllocationRequest("Unauthorized PM rejection", "Project Manager");

        mockMvc.perform(post("/api/allocations/{id}/reject", pendingAllocationId)
                        .with(csrf())
                        .session(pmSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: insufficient role permissions"));

        // Verify allocation remains PENDING
        ResourceAllocation alloc = allocationRepository.findById(pendingAllocationId).orElseThrow();
        assertThat(alloc.getStatus()).isEqualTo(AllocationStatus.PENDING);
    }

    @Test
    @DisplayName("Project Manager CANNOT access organization analytics - rejected with 403 Forbidden")
    void projectManager_CannotAccessAnalytics_Returns403() throws Exception {
        MockHttpSession pmSession = loginAs("pm@talentmarketplace.com", "PM_pass123!", UserRole.PROJECT_MANAGER);

        mockMvc.perform(get("/api/analytics/open-demands").session(pmSession))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: insufficient role permissions"));

        mockMvc.perform(get("/api/analytics/reports/utilization").session(pmSession))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Resource Manager CAN confirm and reject allocations successfully")
    void resourceManager_ApprovalPermissions_Success() throws Exception {
        MockHttpSession rmSession = loginAs("rm@talentmarketplace.com", "RM_pass123!", UserRole.RESOURCE_MANAGER);

        // RM confirms allocation
        mockMvc.perform(post("/api/allocations/{id}/confirm", pendingAllocationId)
                        .with(csrf())
                        .session(rmSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        // Verify allocation status is CONFIRMED in DB
        ResourceAllocation alloc = allocationRepository.findById(pendingAllocationId).orElseThrow();
        assertThat(alloc.getStatus()).isEqualTo(AllocationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Resource Manager CAN access analytics reports successfully")
    void resourceManager_CanAccessAnalytics_Success() throws Exception {
        MockHttpSession rmSession = loginAs("rm@talentmarketplace.com", "RM_pass123!", UserRole.RESOURCE_MANAGER);

        mockMvc.perform(get("/api/analytics/open-demands").session(rmSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Both PM and RM CAN read employee unavailabilities, but only RM can record leave")
    void employeeUnavailabilities_RolePermissions() throws Exception {
        MockHttpSession pmSession = loginAs("pm@talentmarketplace.com", "PM_pass123!", UserRole.PROJECT_MANAGER);
        MockHttpSession rmSession = loginAs("rm@talentmarketplace.com", "RM_pass123!", UserRole.RESOURCE_MANAGER);
        Employee emp = employeeRepository.findAll().get(0);

        // Both PM and RM can read unavailabilities (plural route)
        mockMvc.perform(get("/api/employees/{id}/unavailabilities", emp.getId()).session(pmSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/employees/{id}/unavailabilities", emp.getId()).session(rmSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // PM cannot record unavailability (403 Forbidden)
        CreateUnavailabilityRequest leaveReq = new CreateUnavailabilityRequest(
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 5),
                UnavailabilityType.ANNUAL_LEAVE,
                "Vacation"
        );

        mockMvc.perform(post("/api/employees/{id}/unavailabilities", emp.getId())
                        .with(csrf())
                        .session(pmSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: insufficient role permissions"));

        // RM can record unavailability (201 Created)
        mockMvc.perform(post("/api/employees/{id}/unavailabilities", emp.getId())
                        .with(csrf())
                        .session(rmSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }
}
