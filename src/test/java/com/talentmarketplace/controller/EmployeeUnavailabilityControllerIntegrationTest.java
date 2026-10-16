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
import com.talentmarketplace.dto.request.CreateUnavailabilityRequest;
import com.talentmarketplace.dto.request.UpdateUnavailabilityRequest;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.config.TestDatabaseSafetyGuard;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.UnavailabilityType;
import com.talentmarketplace.repository.DemandSkillRepository;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;

import javax.sql.DataSource;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class EmployeeUnavailabilityControllerIntegrationTest {

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
    private EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private EmployeeUnavailabilityRepository unavailabilityRepository;

    private Employee employee;

    @BeforeEach
    void cleanDatabase() {
        TestDatabaseSafetyGuard.assertSafeTestDatabase(dataSource);

        allocationRepository.deleteAll();
        demandSkillRepository.deleteAll();
        demandRepository.deleteAll();
        unavailabilityRepository.deleteAll();
        employeeSkillRepository.deleteAll();
        employeeRepository.deleteAll();

        employee = employeeRepository.save(new Employee(
                "EMP-301", "Emma", "Watson", "emma.watson@example.com",
                "Product", "Product Manager", "Mumbai",
                LocalDate.of(2022, 1, 1), EmploymentStatus.ACTIVE
        ));
    }

    @Test
    @DisplayName("POST /api/employees/{id}/unavailability: records leave successfully and returns 201 Created")
    void createUnavailability_Success() throws Exception {
        CreateUnavailabilityRequest request = new CreateUnavailabilityRequest(
                LocalDate.of(2025, 7, 10),
                LocalDate.of(2025, 7, 20),
                UnavailabilityType.ANNUAL_LEAVE,
                "Summer holiday"
        );

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/unavailability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP-301"))
                .andExpect(jsonPath("$.data.type").value("ANNUAL_LEAVE"))
                .andExpect(jsonPath("$.data.startDate").value("2025-07-10"))
                .andExpect(jsonPath("$.data.endDate").value("2025-07-20"));
    }

    @Test
    @DisplayName("POST /api/employees/{id}/unavailability: returns 400 Bad Request when dates are inverted")
    void createUnavailability_InvalidDates_Returns400() throws Exception {
        CreateUnavailabilityRequest request = new CreateUnavailabilityRequest(
                LocalDate.of(2025, 7, 20),
                LocalDate.of(2025, 7, 10), // End before start
                UnavailabilityType.SICK_LEAVE,
                "Invalid dates"
        );

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/unavailability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Start date cannot be after end date"));
    }

    @Test
    @DisplayName("POST /api/employees/{id}/unavailability: returns 400 Bad Request when period overlaps existing leave")
    void createUnavailability_OverlappingLeave_Returns400() throws Exception {
        CreateUnavailabilityRequest request1 = new CreateUnavailabilityRequest(
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 15),
                UnavailabilityType.ANNUAL_LEAVE,
                "Trip"
        );

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/unavailability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Overlapping request (Aug 10 to Aug 25)
        CreateUnavailabilityRequest overlappingReq = new CreateUnavailabilityRequest(
                LocalDate.of(2025, 8, 10),
                LocalDate.of(2025, 8, 25),
                UnavailabilityType.ANNUAL_LEAVE,
                "Overlapping trip"
        );

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/unavailability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/employees/{id}/unavailability: returns 404 when employee does not exist")
    void createUnavailability_EmployeeNotFound_Returns404() throws Exception {
        CreateUnavailabilityRequest request = new CreateUnavailabilityRequest(
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 5),
                UnavailabilityType.SICK_LEAVE,
                "Medical"
        );

        mockMvc.perform(post("/api/employees/999999/unavailability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/employees/{id}/unavailability: returns empty array when employee has zero leave records")
    void getEmployeeUnavailabilities_ZeroRecords_ReturnsEmptyArray_Singular() throws Exception {
        mockMvc.perform(get("/api/employees/" + employee.getId() + "/unavailability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/employees/{id}/unavailabilities (plural): returns empty array when employee has zero leave records")
    void getEmployeeUnavailabilities_ZeroRecords_ReturnsEmptyArray_Plural() throws Exception {
        mockMvc.perform(get("/api/employees/" + employee.getId() + "/unavailabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/employees/{id}/unavailabilities (plural): returns employee leaves when records exist")
    void getEmployeeUnavailabilities_WithRecords_PluralRoute() throws Exception {
        CreateUnavailabilityRequest req = new CreateUnavailabilityRequest(
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 5),
                UnavailabilityType.TRAINING,
                "Tech workshop"
        );

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/unavailabilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/employees/" + employee.getId() + "/unavailabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].type").value("TRAINING"))
                .andExpect(jsonPath("$.data[0].reason").value("Tech workshop"));
    }

    @Test
    @DisplayName("GET /api/employees/{id}/unavailabilities (plural): returns 404 when employee does not exist")
    void getEmployeeUnavailabilities_EmployeeNotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/employees/999999/unavailabilities"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/employees/{id}/unavailability: returns employee leaves")
    void getEmployeeUnavailabilities() throws Exception {
        CreateUnavailabilityRequest req = new CreateUnavailabilityRequest(
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 5),
                UnavailabilityType.TRAINING,
                "Tech workshop"
        );

        mockMvc.perform(post("/api/employees/" + employee.getId() + "/unavailability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(get("/api/employees/" + employee.getId() + "/unavailability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/employees/{id}/unavailability/{id}: updates leave dates")
    void updateUnavailability() throws Exception {
        CreateUnavailabilityRequest req = new CreateUnavailabilityRequest(
                LocalDate.of(2025, 11, 1),
                LocalDate.of(2025, 11, 5),
                UnavailabilityType.SICK_LEAVE,
                "Flu"
        );

        String jsonResp = mockMvc.perform(post("/api/employees/" + employee.getId() + "/unavailability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        Long unavailId = objectMapper.readTree(jsonResp).get("data").get("id").asLong();

        UpdateUnavailabilityRequest updateReq = new UpdateUnavailabilityRequest(
                LocalDate.of(2025, 11, 2),
                LocalDate.of(2025, 11, 6),
                UnavailabilityType.SICK_LEAVE,
                "Extended flu"
        );

        mockMvc.perform(put("/api/employees/" + employee.getId() + "/unavailability/" + unavailId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.startDate").value("2025-11-02"))
                .andExpect(jsonPath("$.data.endDate").value("2025-11-06"));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id}/unavailability/{id}: removes leave record")
    void deleteUnavailability() throws Exception {
        CreateUnavailabilityRequest req = new CreateUnavailabilityRequest(
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5),
                UnavailabilityType.ANNUAL_LEAVE,
                "Winter break"
        );

        String jsonResp = mockMvc.perform(post("/api/employees/" + employee.getId() + "/unavailability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        Long unavailId = objectMapper.readTree(jsonResp).get("data").get("id").asLong();

        mockMvc.perform(delete("/api/employees/" + employee.getId() + "/unavailability/" + unavailId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/employees/" + employee.getId() + "/unavailability/" + unavailId))
                .andExpect(status().isNotFound());
    }
}
