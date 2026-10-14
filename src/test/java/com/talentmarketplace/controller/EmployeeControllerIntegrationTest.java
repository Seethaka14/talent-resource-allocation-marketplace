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
import com.talentmarketplace.dto.request.CreateEmployeeRequest;
import com.talentmarketplace.dto.request.UpdateEmployeeRequest;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.repository.EmployeeRepository;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private com.talentmarketplace.repository.EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private com.talentmarketplace.repository.EmployeeUnavailabilityRepository unavailabilityRepository;

    @Autowired
    private com.talentmarketplace.repository.DemandSkillRepository demandSkillRepository;

    @Autowired
    private com.talentmarketplace.repository.ProjectDemandRepository demandRepository;

    @BeforeEach
    void cleanDatabase() {
        demandSkillRepository.deleteAll();
        demandRepository.deleteAll();
        unavailabilityRepository.deleteAll();
        employeeSkillRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/employees: creates employee and returns 201 Created with Location header")
    void createEmployee_Success() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "EMP-101",
                "Bob",
                "Williams",
                "bob.williams@example.com",
                "Engineering",
                "Software Engineer",
                "Bangalore",
                LocalDate.of(2023, 5, 10),
                EmploymentStatus.ACTIVE,
                100
        );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.employeeCode").value("EMP-101"))
                .andExpect(jsonPath("$.data.name").value("Bob Williams"))
                .andExpect(jsonPath("$.data.email").value("bob.williams@example.com"))
                .andExpect(jsonPath("$.data.maxCapacityPercent").value(100));
    }

    @Test
    @DisplayName("POST /api/employees: returns 409 Conflict when employee code already exists")
    void createEmployee_DuplicateCode_Returns409() throws Exception {
        Employee existing = new Employee(
                "EMP-102",
                "Charlie",
                "Brown",
                "charlie.brown@example.com",
                "Design",
                "UI Designer",
                "Pune",
                LocalDate.of(2022, 3, 1),
                EmploymentStatus.ACTIVE
        );
        employeeRepository.save(existing);

        CreateEmployeeRequest duplicateRequest = new CreateEmployeeRequest(
                "EMP-102",
                "Different",
                "Name",
                "other.email@example.com",
                "Design",
                "UI Designer",
                "Pune",
                LocalDate.of(2022, 3, 1),
                EmploymentStatus.ACTIVE,
                100
        );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Employee already exists with employeeCode: 'EMP-102'"));
    }

    @Test
    @DisplayName("POST /api/employees: returns 400 Bad Request when validation fails")
    void createEmployee_InvalidInput_Returns400() throws Exception {
        CreateEmployeeRequest invalidRequest = new CreateEmployeeRequest(
                "invalid code with spaces!", // fails regex
                "",                          // blank first name
                "ValidLastName",
                "not-an-email",              // bad email
                "Engineering",
                "Developer",
                "Bangalore",
                null,                        // missing joining date
                null,                        // missing status
                150                          // exceeds 100 max
        );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.firstName").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.employeeCode").exists())
                .andExpect(jsonPath("$.validationErrors.joiningDate").exists())
                .andExpect(jsonPath("$.validationErrors.employmentStatus").exists())
                .andExpect(jsonPath("$.validationErrors.maxCapacityPercent").exists());
    }

    @Test
    @DisplayName("GET /api/employees/{id}: returns employee when found, 404 when not found")
    void getEmployeeById() throws Exception {
        Employee saved = employeeRepository.save(new Employee(
                "EMP-103",
                "Diana",
                "Prince",
                "diana.prince@example.com",
                "Security",
                "Security Lead",
                "Hyderabad",
                LocalDate.of(2021, 6, 1),
                EmploymentStatus.ACTIVE
        ));

        mockMvc.perform(get("/api/employees/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP-103"))
                .andExpect(jsonPath("$.data.name").value("Diana Prince"));

        mockMvc.perform(get("/api/employees/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/employees: returns filtered employees list")
    void searchEmployees() throws Exception {
        employeeRepository.save(new Employee(
                "EMP-104", "Evan", "Wright", "evan@example.com",
                "Engineering", "Frontend Dev", "Bangalore",
                LocalDate.of(2023, 1, 1), EmploymentStatus.ACTIVE
        ));
        employeeRepository.save(new Employee(
                "EMP-105", "Fiona", "Gallagher", "fiona@example.com",
                "Marketing", "Marketing Specialist", "Mumbai",
                LocalDate.of(2023, 2, 1), EmploymentStatus.ACTIVE
        ));

        mockMvc.perform(get("/api/employees")
                        .param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].employeeCode").value("EMP-104"));
    }

    @Test
    @DisplayName("PUT /api/employees/{id}: updates employee successfully")
    void updateEmployee() throws Exception {
        Employee saved = employeeRepository.save(new Employee(
                "EMP-106", "George", "Clark", "george@example.com",
                "Engineering", "Junior Dev", "Bangalore",
                LocalDate.of(2023, 3, 1), EmploymentStatus.ACTIVE
        ));

        UpdateEmployeeRequest updateReq = new UpdateEmployeeRequest(
                "George",
                "Clarkson",
                "george.clarkson@example.com",
                "Engineering",
                "Mid-Level Dev",
                "Bangalore",
                LocalDate.of(2023, 3, 1),
                EmploymentStatus.ACTIVE,
                75
        );

        mockMvc.perform(put("/api/employees/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("George Clarkson"))
                .andExpect(jsonPath("$.data.designation").value("Mid-Level Dev"))
                .andExpect(jsonPath("$.data.maxCapacityPercent").value(75));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id}: deletes employee and returns 204")
    void deleteEmployee() throws Exception {
        Employee saved = employeeRepository.save(new Employee(
                "EMP-107", "Hannah", "Abbott", "hannah@example.com",
                "HR", "HR Specialist", "Bangalore",
                LocalDate.of(2023, 4, 1), EmploymentStatus.ACTIVE
        ));

        mockMvc.perform(delete("/api/employees/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/employees/" + saved.getId()))
                .andExpect(status().isNotFound());
    }
}
