package com.talentmarketplace.controller;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.talentmarketplace.dto.request.CreateDemandRequest;
import com.talentmarketplace.dto.request.DemandSkillRequest;
import com.talentmarketplace.dto.request.UpdateDemandRequest;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;
import com.talentmarketplace.config.TestDatabaseSafetyGuard;
import com.talentmarketplace.repository.DemandCodeSequenceRepository;
import com.talentmarketplace.repository.DemandSkillRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;
import com.talentmarketplace.repository.SkillRepository;

import javax.sql.DataSource;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProjectDemandControllerIntegrationTest {

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
    private SkillRepository skillRepository;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private DemandCodeSequenceRepository demandCodeSequenceRepository;

    private Skill skillJava;
    private Skill skillReact;

    @BeforeEach
    void cleanDatabase() {
        TestDatabaseSafetyGuard.assertSafeTestDatabase(dataSource);

        allocationRepository.deleteAll();
        demandSkillRepository.deleteAll();
        demandRepository.deleteAll();
        employeeSkillRepository.deleteAll();
        skillRepository.deleteAll();
        demandCodeSequenceRepository.deleteAll();

        skillJava = skillRepository.save(new Skill("Java", SkillCategory.BACKEND, "Java programming"));
        skillReact = skillRepository.save(new Skill("React", SkillCategory.FRONTEND, "React library"));
    }

    @Test
    @DisplayName("TC-06-01: Create a demand with valid fields -> created (201 Created)")
    void tc06_01_createDemand_Success() throws Exception {
        CreateDemandRequest request = new CreateDemandRequest(
                "DEM-101",
                "Cloud Migration",
                "Cloud Architect",
                "Cloud Services",
                "Bangalore",
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 8, 31),
                100,
                DemandPriority.HIGH,
                5.0,
                List.of(new DemandSkillRequest(skillJava.getId(), true, ProficiencyLevel.ADVANCED))
        );

        mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.demandCode").value("DEM-101"))
                .andExpect(jsonPath("$.data.role").value("Cloud Architect"))
                .andExpect(jsonPath("$.data.allocationPercent").value(100))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.requiredSkills.length()").value(1))
                .andExpect(jsonPath("$.data.requiredSkills[0].skillName").value("Java"))
                .andExpect(jsonPath("$.data.requiredSkills[0].mandatory").value(true))
                .andExpect(jsonPath("$.data.requiredSkills[0].minimumProficiency").value("ADVANCED"))
                .andExpect(jsonPath("$.data.requiredSkills[0].minimumProficiencyWeight").value(3));
    }

    @Test
    @DisplayName("TC-06-02: Create a demand with missing role or invalid dates -> rejected (400 Bad Request)")
    void tc06_02_createDemand_InvalidDatesOrRole_Rejected() throws Exception {
        // Case A: Missing role
        CreateDemandRequest missingRoleReq = new CreateDemandRequest(
                "DEM-102",
                "Project Alpha",
                "", // Blank role
                "Engineering",
                "Bangalore",
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 8, 31),
                100,
                DemandPriority.MEDIUM,
                2.0,
                List.of()
        );

        mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingRoleReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.role").exists());

        // Case B: Inverted dates (Start > End)
        CreateDemandRequest invertedDatesReq = new CreateDemandRequest(
                "DEM-103",
                "Project Beta",
                "Backend Engineer",
                "Engineering",
                "Bangalore",
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 2, 1), // Invalid!
                100,
                DemandPriority.HIGH,
                3.0,
                List.of()
        );

        mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invertedDatesReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Start date cannot be after end date"));
    }

    @Test
    @DisplayName("POST /api/demands: rejects invalid allocation percentages (<1 or >100)")
    void createDemand_InvalidAllocationPercent_Returns400() throws Exception {
        CreateDemandRequest invalidPercentReq = new CreateDemandRequest(
                "DEM-104",
                "Project Gamma",
                "Fullstack Dev",
                "Engineering",
                "Bangalore",
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 6, 30),
                120, // Exceeds 100%
                DemandPriority.LOW,
                1.0,
                List.of()
        );

        mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPercentReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.allocationPercent").exists());
    }

    @Test
    @DisplayName("POST /api/demands: rejects duplicate demand code (409 Conflict)")
    void createDemand_DuplicateCode_Returns409() throws Exception {
        CreateDemandRequest req1 = new CreateDemandRequest(
                "DEM-105", "Project Delta", "Tester", "QA", "Bangalore",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 4, 1),
                50, DemandPriority.MEDIUM, 1.0, List.of()
        );

        mockMvc.perform(post("/api/demands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/demands: rejects duplicate skill requirements in same request")
    void createDemand_DuplicateSkills_Returns400() throws Exception {
        CreateDemandRequest req = new CreateDemandRequest(
                "DEM-106", "Project Epsilon", "Developer", "Engineering", "Pune",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 4, 1),
                100, DemandPriority.HIGH, 2.0,
                List.of(
                        new DemandSkillRequest(skillJava.getId(), true, ProficiencyLevel.ADVANCED),
                        new DemandSkillRequest(skillJava.getId(), false, ProficiencyLevel.BEGINNER) // Duplicate!
                )
        );

        mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Duplicate skill requirement detected for skill ID: " + skillJava.getId()));
    }

    @Test
    @DisplayName("GET /api/demands: filters demands by priority and status")
    void searchDemands() throws Exception {
        CreateDemandRequest req1 = new CreateDemandRequest(
                "DEM-107", "Project One", "Java Dev", "Backend", "Bangalore",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 1),
                100, DemandPriority.CRITICAL, 3.0, List.of()
        );
        CreateDemandRequest req2 = new CreateDemandRequest(
                "DEM-108", "Project Two", "React Dev", "Frontend", "Bangalore",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 1),
                100, DemandPriority.LOW, 2.0, List.of()
        );

        mockMvc.perform(post("/api/demands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));
        mockMvc.perform(post("/api/demands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)));

        mockMvc.perform(get("/api/demands")
                        .param("priority", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].demandCode").value("DEM-107"));
    }

    @Test
    @DisplayName("POST /api/demands/{id}/cancel: cancels demand and prevents further modifications")
    void cancelDemand() throws Exception {
        CreateDemandRequest req = new CreateDemandRequest(
                "DEM-109", "Project Three", "DevOps Engineer", "Infra", "Hyderabad",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 1),
                100, DemandPriority.MEDIUM, 4.0, List.of()
        );

        String jsonResp = mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        Long demandId = objectMapper.readTree(jsonResp).get("data").get("id").asLong();

        // Cancel
        mockMvc.perform(post("/api/demands/" + demandId + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Project postponed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // Attempt to update cancelled demand
        UpdateDemandRequest updateReq = new UpdateDemandRequest(
                "Project Three", "Updated Role", "Infra", "Hyderabad",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 1),
                100, DemandPriority.MEDIUM, DemandStatus.OPEN, 4.0, List.of()
        );

        mockMvc.perform(put("/api/demands/" + demandId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot modify a cancelled demand"));
    }

    @Test
    @DisplayName("GET /api/demands/next-code: returns next formatted sequence code")
    void previewNextDemandCode_ReturnsGeneratedCode() throws Exception {
        int currentYear = LocalDate.now().getYear();
        String expectedPrefix = "DEM-" + currentYear + "-";

        mockMvc.perform(get("/api/demands/next-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.demandCode").value(org.hamcrest.Matchers.startsWith(expectedPrefix)));
    }

    @Test
    @DisplayName("POST /api/demands without demandCode auto-generates sequential unique codes")
    void createDemand_WithoutDemandCode_AutoGeneratesUniqueCodes() throws Exception {
        int currentYear = LocalDate.now().getYear();

        CreateDemandRequest req1 = new CreateDemandRequest(
                null, "Project Alpha", "Data Engineer", "Data & Analytics", "London",
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 9, 30),
                100, DemandPriority.HIGH, 3.0, List.of()
        );

        mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.demandCode").value("DEM-" + currentYear + "-0001"));

        CreateDemandRequest req2 = new CreateDemandRequest(
                "", "Project Beta", "ML Engineer", "Data & Analytics", "London",
                LocalDate.of(2025, 4, 1), LocalDate.of(2025, 10, 31),
                50, DemandPriority.MEDIUM, 4.0, List.of()
        );

        mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.demandCode").value("DEM-" + currentYear + "-0002"));
    }

    @Test
    @DisplayName("POST /api/demands with existing demandCode returns 409 Conflict")
    void createDemand_DuplicateCode_ReturnsConflict409() throws Exception {
        CreateDemandRequest req1 = new CreateDemandRequest(
                "DEM-UNIQUE-99", "Project First", "Full Stack Dev", "Engineering", "Remote",
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 9, 30),
                100, DemandPriority.HIGH, 3.0, List.of()
        );

        mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        CreateDemandRequest req2 = new CreateDemandRequest(
                "DEM-UNIQUE-99", "Project Second", "Full Stack Dev", "Engineering", "Remote",
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 9, 30),
                100, DemandPriority.HIGH, 3.0, List.of()
        );

        mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("DEM-UNIQUE-99")));
    }

    @Test
    @DisplayName("Concurrent demand creation without demandCode produces unique, non-colliding codes")
    void createDemands_Concurrently_AllGenerateUniqueCodesWithoutCollisions() throws Exception {
        int threadCount = 10;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch readyLatch = new java.util.concurrent.CountDownLatch(threadCount);
        java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch doneLatch = new java.util.concurrent.CountDownLatch(threadCount);

        java.util.Set<String> generatedCodes = java.util.concurrent.ConcurrentHashMap.newKeySet();
        java.util.List<Throwable> errors = new java.util.concurrent.CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    CreateDemandRequest req = new CreateDemandRequest(
                            null,
                            "Concurrent Project " + idx,
                            "Role " + idx,
                            "Platform",
                            "London",
                            LocalDate.of(2025, 5, 1),
                            LocalDate.of(2025, 11, 30),
                            50,
                            DemandPriority.MEDIUM,
                            2.0,
                            List.of()
                    );

                    String response = mockMvc.perform(post("/api/demands")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                            .andExpect(status().isCreated())
                            .andReturn().getResponse().getContentAsString();

                    String code = objectMapper.readTree(response).get("data").get("demandCode").asText();
                    generatedCodes.add(code);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(30, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();

        org.assertj.core.api.Assertions.assertThat(errors).isEmpty();
        org.assertj.core.api.Assertions.assertThat(generatedCodes).hasSize(threadCount);
    }

    @Test
    @DisplayName("PUT /api/demands/{id}: updates a demand that had no skills to include required skills, and verifies they are returned across endpoints")
    void updateDemand_addRequiredSkills_persistsAndReturnsSkills() throws Exception {
        // Step 1: Create a demand with 0 skills (like DEM-2026-0001 initial state)
        CreateDemandRequest initialReq = new CreateDemandRequest(
                "DEM-SKILL-01", "Cloud Infrastructure Modernization", "Senior Cloud Architect",
                "Platform", "Bangalore", LocalDate.of(2026, 11, 1), LocalDate.of(2027, 4, 30),
                100, DemandPriority.HIGH, 5.0, List.of()
        );

        String createResp = mockMvc.perform(post("/api/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.requiredSkills.length()").value(0))
                .andReturn().getResponse().getContentAsString();

        Long demandId = objectMapper.readTree(createResp).get("data").get("id").asLong();

        // Verify GET /api/demands/{id} currently has 0 skills
        mockMvc.perform(get("/api/demands/" + demandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requiredSkills.length()").value(0));

        // Step 2: Edit demand to add Java (Mandatory, ADVANCED) and React (Optional, INTERMEDIATE)
        UpdateDemandRequest updateReq = new UpdateDemandRequest(
                "Cloud Infrastructure Modernization Updated", "Senior Cloud Architect",
                "Platform", "Bangalore", LocalDate.of(2026, 11, 1), LocalDate.of(2027, 4, 30),
                100, DemandPriority.HIGH, DemandStatus.OPEN, 5.0,
                List.of(
                        new DemandSkillRequest(skillJava.getId(), true, ProficiencyLevel.ADVANCED),
                        new DemandSkillRequest(skillReact.getId(), false, ProficiencyLevel.INTERMEDIATE)
                )
        );

        mockMvc.perform(put("/api/demands/" + demandId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectName").value("Cloud Infrastructure Modernization Updated"))
                .andExpect(jsonPath("$.data.requiredSkills.length()").value(2))
                .andExpect(jsonPath("$.data.requiredSkills[?(@.skillName == 'Java')].mandatory").value(true))
                .andExpect(jsonPath("$.data.requiredSkills[?(@.skillName == 'Java')].minimumProficiency").value("ADVANCED"))
                .andExpect(jsonPath("$.data.requiredSkills[?(@.skillName == 'React')].mandatory").value(false))
                .andExpect(jsonPath("$.data.requiredSkills[?(@.skillName == 'React')].minimumProficiency").value("INTERMEDIATE"));

        // Step 3: Verify GET /api/demands/{id} returns the updated skills
        mockMvc.perform(get("/api/demands/" + demandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requiredSkills.length()").value(2));

        // Step 4: Verify GET /api/demands?status=OPEN returns the updated skills
        mockMvc.perform(get("/api/demands").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + demandId + ")].requiredSkills.length()").value(2));

        // Step 5: Verify GET /api/demands/code/{code} returns the updated skills
        mockMvc.perform(get("/api/demands/code/DEM-SKILL-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requiredSkills.length()").value(2));
    }
}
