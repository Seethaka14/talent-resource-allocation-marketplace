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
import com.talentmarketplace.repository.DemandSkillRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.SkillRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectDemandControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectDemandRepository demandRepository;

    @Autowired
    private DemandSkillRepository demandSkillRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepository;

    private Skill skillJava;
    private Skill skillReact;

    @BeforeEach
    void cleanDatabase() {
        demandSkillRepository.deleteAll();
        demandRepository.deleteAll();
        employeeSkillRepository.deleteAll();
        skillRepository.deleteAll();

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
}
