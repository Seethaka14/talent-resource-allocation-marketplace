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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.talentmarketplace.dto.request.AssignSkillRequest;
import com.talentmarketplace.dto.request.UpdateEmployeeSkillRequest;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.config.TestDatabaseSafetyGuard;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;
import com.talentmarketplace.repository.SkillRepository;

import javax.sql.DataSource;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class EmployeeSkillControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceAllocationRepository allocationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private com.talentmarketplace.repository.DemandSkillRepository demandSkillRepository;

    @Autowired
    private com.talentmarketplace.repository.ProjectDemandRepository demandRepository;

    @Autowired
    private com.talentmarketplace.repository.EmployeeUnavailabilityRepository unavailabilityRepository;

    private Employee employee;
    private Skill skillJava;
    private Skill skillDocker;

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

        employee = employeeRepository.save(new Employee(
                "EMP-201", "John", "Doe", "john.doe@example.com",
                "Backend", "Software Engineer", "Bangalore",
                LocalDate.of(2023, 1, 1), EmploymentStatus.ACTIVE
        ));

        skillJava = skillRepository.save(new Skill("Java", SkillCategory.BACKEND, "Java Core & Spring"));
        skillDocker = skillRepository.save(new Skill("Docker", SkillCategory.CLOUD_DEVOPS, "Containerization"));
    }

    @Test
    @DisplayName("POST /api/employees/{id}/skills: assigns skill successfully and returns 201 Created")
    void assignSkill_Success() throws Exception {
        AssignSkillRequest request = new AssignSkillRequest(
                skillJava.getId(),
                ProficiencyLevel.ADVANCED,
                4.5,
                LocalDate.of(2024, 2, 1),
                "Tech Lead Assessment",
                "Certified Java Professional with 4+ years hands-on microservices experience"
        );

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP-201"))
                .andExpect(jsonPath("$.data.skillName").value("Java"))
                .andExpect(jsonPath("$.data.proficiencyLevel").value("ADVANCED"))
                .andExpect(jsonPath("$.data.proficiencyWeight").value(3))
                .andExpect(jsonPath("$.data.yearsOfExperience").value(4.5))
                .andExpect(jsonPath("$.data.validatedBy").value("Tech Lead Assessment"));
    }

    @Test
    @DisplayName("POST /api/employees/{id}/skills: returns 409 Conflict when skill already assigned")
    void assignSkill_Duplicate_Returns409() throws Exception {
        AssignSkillRequest request = new AssignSkillRequest(
                skillJava.getId(), ProficiencyLevel.INTERMEDIATE, 2.0,
                LocalDate.now(), "Peer", "Initial assessment"
        );

        // First assignment
        mockMvc.perform(post("/api/employees/" + employee.getId() + "/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate assignment
        mockMvc.perform(post("/api/employees/" + employee.getId() + "/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/employees/{id}/skills: returns 404 when employee or skill does not exist")
    void assignSkill_NotFound() throws Exception {
        AssignSkillRequest request = new AssignSkillRequest(
                skillJava.getId(), ProficiencyLevel.BEGINNER, 1.0,
                LocalDate.now(), "Manager", "Basic"
        );

        // Non-existent employee
        mockMvc.perform(post("/api/employees/999999/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Non-existent skill
        AssignSkillRequest invalidSkillReq = new AssignSkillRequest(
                888888L, ProficiencyLevel.BEGINNER, 1.0,
                LocalDate.now(), "Manager", "Basic"
        );
        mockMvc.perform(post("/api/employees/" + employee.getId() + "/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSkillReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/employees/{id}/skills: returns 400 Bad Request on validation failure")
    void assignSkill_ValidationError() throws Exception {
        AssignSkillRequest invalid = new AssignSkillRequest(
                null, null, -2.0, null, null, null
        );

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.skillId").exists())
                .andExpect(jsonPath("$.validationErrors.proficiencyLevel").exists())
                .andExpect(jsonPath("$.validationErrors.yearsOfExperience").exists());
    }

    @Test
    @DisplayName("GET /api/employees/{id}/skills: returns list of employee skills")
    void getEmployeeSkills() throws Exception {
        AssignSkillRequest req1 = new AssignSkillRequest(
                skillJava.getId(), ProficiencyLevel.ADVANCED, 3.0, LocalDate.now(), "Tech Lead", "Java expert"
        );
        AssignSkillRequest req2 = new AssignSkillRequest(
                skillDocker.getId(), ProficiencyLevel.INTERMEDIATE, 2.0, LocalDate.now(), "DevOps Lead", "Docker user"
        );

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/skills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/skills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)));

        mockMvc.perform(get("/api/employees/" + employee.getId() + "/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("PUT /api/employees/{id}/skills/{skillId}: updates proficiency and evidence")
    void updateEmployeeSkill() throws Exception {
        AssignSkillRequest assignReq = new AssignSkillRequest(
                skillJava.getId(), ProficiencyLevel.BEGINNER, 1.0, LocalDate.now(), "Self", "Learning"
        );
        mockMvc.perform(post("/api/employees/" + employee.getId() + "/skills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignReq)));

        UpdateEmployeeSkillRequest updateReq = new UpdateEmployeeSkillRequest(
                ProficiencyLevel.ADVANCED, 3.5, LocalDate.now(), "Principal Engineer", "Completed certification & delivered Project X"
        );

        mockMvc.perform(put("/api/employees/" + employee.getId() + "/skills/" + skillJava.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.proficiencyLevel").value("ADVANCED"))
                .andExpect(jsonPath("$.data.proficiencyWeight").value(3))
                .andExpect(jsonPath("$.data.yearsOfExperience").value(3.5))
                .andExpect(jsonPath("$.data.validatedBy").value("Principal Engineer"));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id}/skills/{skillId}: removes skill assignment")
    void removeEmployeeSkill() throws Exception {
        AssignSkillRequest assignReq = new AssignSkillRequest(
                skillJava.getId(), ProficiencyLevel.BEGINNER, 1.0, LocalDate.now(), "Self", "Learning"
        );
        mockMvc.perform(post("/api/employees/" + employee.getId() + "/skills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignReq)));

        mockMvc.perform(delete("/api/employees/" + employee.getId() + "/skills/" + skillJava.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/employees/" + employee.getId() + "/skills/" + skillJava.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/skills/{skillId}/employees: returns employees possessing the skill")
    void getEmployeesBySkill() throws Exception {
        AssignSkillRequest assignReq = new AssignSkillRequest(
                skillJava.getId(), ProficiencyLevel.ADVANCED, 3.0, LocalDate.now(), "Lead", "Dev"
        );
        mockMvc.perform(post("/api/employees/" + employee.getId() + "/skills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignReq)));

        mockMvc.perform(get("/api/skills/" + skillJava.getId() + "/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].employeeCode").value("EMP-201"));
    }

    @Test
    @DisplayName("GET /api/employee-skills: returns all employee skill records across enterprise")
    void getAllEmployeeSkills() throws Exception {
        AssignSkillRequest assignReq = new AssignSkillRequest(
                skillJava.getId(), ProficiencyLevel.ADVANCED, 3.0, LocalDate.now(), "Lead", "Dev"
        );
        mockMvc.perform(post("/api/employees/" + employee.getId() + "/skills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignReq)));

        mockMvc.perform(get("/api/employee-skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].employeeCode").value("EMP-201"))
                .andExpect(jsonPath("$.data[0].skillName").value("Java"));
    }
}
