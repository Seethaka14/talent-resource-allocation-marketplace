package com.talentmarketplace.controller;

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
import com.talentmarketplace.dto.request.CreateSkillRequest;
import com.talentmarketplace.dto.request.UpdateSkillRequest;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;
import com.talentmarketplace.config.TestDatabaseSafetyGuard;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;
import com.talentmarketplace.repository.SkillRepository;

import javax.sql.DataSource;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class SkillControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceAllocationRepository allocationRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private com.talentmarketplace.repository.DemandSkillRepository demandSkillRepository;

    @Autowired
    private com.talentmarketplace.repository.ProjectDemandRepository demandRepository;

    @BeforeEach
    void cleanDatabase() {
        TestDatabaseSafetyGuard.assertSafeTestDatabase(dataSource);

        allocationRepository.deleteAll();
        demandSkillRepository.deleteAll();
        demandRepository.deleteAll();
        employeeSkillRepository.deleteAll();
        skillRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/skills: creates skill and returns 201 Created")
    void createSkill_Success() throws Exception {
        CreateSkillRequest request = new CreateSkillRequest("PostgreSQL", SkillCategory.DATABASE, "Relational DB");

        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("PostgreSQL"))
                .andExpect(jsonPath("$.data.category").value("DATABASE"));
    }

    @Test
    @DisplayName("POST /api/skills: returns 409 Conflict when skill name already exists")
    void createSkill_Duplicate_Returns409() throws Exception {
        skillRepository.save(new Skill("Docker", SkillCategory.CLOUD_DEVOPS, "Containerization"));

        CreateSkillRequest duplicateReq = new CreateSkillRequest("docker", SkillCategory.CLOUD_DEVOPS, "Different desc");

        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/skills: returns 400 Bad Request on invalid payload")
    void createSkill_InvalidInput_Returns400() throws Exception {
        CreateSkillRequest invalid = new CreateSkillRequest("", null, null);

        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.category").exists());
    }

    @Test
    @DisplayName("GET /api/skills/{id}: returns 200 when found, 404 when not found")
    void getSkillById() throws Exception {
        Skill saved = skillRepository.save(new Skill("React", SkillCategory.FRONTEND, "UI Library"));

        mockMvc.perform(get("/api/skills/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("React"));

        mockMvc.perform(get("/api/skills/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/skills: filters by category")
    void searchSkills_FilterByCategory() throws Exception {
        skillRepository.save(new Skill("Kubernetes", SkillCategory.CLOUD_DEVOPS, "Orchestration"));
        skillRepository.save(new Skill("Angular", SkillCategory.FRONTEND, "Framework"));

        mockMvc.perform(get("/api/skills")
                        .param("category", "CLOUD_DEVOPS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Kubernetes"));
    }

    @Test
    @DisplayName("PUT /api/skills/{id}: updates skill successfully")
    void updateSkill() throws Exception {
        Skill saved = skillRepository.save(new Skill("Vue.js", SkillCategory.FRONTEND, "Old desc"));

        UpdateSkillRequest updateReq = new UpdateSkillRequest("Vue 3", SkillCategory.FRONTEND, "Updated desc");

        mockMvc.perform(put("/api/skills/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Vue 3"));
    }

    @Test
    @DisplayName("DELETE /api/skills/{id}: deletes skill and returns 204")
    void deleteSkill() throws Exception {
        Skill saved = skillRepository.save(new Skill("GraphQL", SkillCategory.BACKEND, "API spec"));

        mockMvc.perform(delete("/api/skills/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/skills/" + saved.getId()))
                .andExpect(status().isNotFound());
    }
}
