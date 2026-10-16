package com.talentmarketplace.controller;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.talentmarketplace.config.TestDatabaseSafetyGuard;
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
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;
import com.talentmarketplace.repository.SkillRepository;

import javax.sql.DataSource;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceAllocationRepository allocationRepository;

    @Autowired
    private DemandSkillRepository demandSkillRepository;

    @Autowired
    private ProjectDemandRepository demandRepository;

    @Autowired
    private EmployeeUnavailabilityRepository unavailabilityRepository;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SkillRepository skillRepository;

    @BeforeEach
    void cleanDatabase() {
        TestDatabaseSafetyGuard.assertSafeTestDatabase(dataSource);

        allocationRepository.deleteAll();
        demandSkillRepository.deleteAll();
        demandRepository.deleteAll();
        unavailabilityRepository.deleteAll();
        employeeSkillRepository.deleteAll();
        employeeRepository.deleteAll();
        skillRepository.deleteAll();
    }

    private Employee createEmployee(String code, String name, String dept, String loc,
                                    EmploymentStatus status, int maxCap) {
        Employee e = new Employee();
        e.setEmployeeCode(code);
        e.setFirstName(name.split(" ")[0]);
        e.setLastName(name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : "Doe");
        e.setName(name);
        e.setEmail(code.toLowerCase() + "@example.com");
        e.setDepartment(dept);
        e.setDesignation("Engineer");
        e.setLocation(loc);
        e.setJoiningDate(LocalDate.of(2025, 1, 1));
        e.setEmploymentStatus(status);
        e.setMaxCapacityPercent(maxCap);
        return employeeRepository.save(e);
    }

    private Skill createSkill(String name, SkillCategory cat) {
        Skill s = new Skill();
        s.setName(name);
        s.setCategory(cat);
        s.setDescription(name + " description");
        return skillRepository.save(s);
    }

    private EmployeeSkill assignSkill(Employee e, Skill s, ProficiencyLevel prof, double exp) {
        EmployeeSkill es = new EmployeeSkill();
        es.setEmployee(e);
        es.setSkill(s);
        es.setProficiencyLevel(prof);
        es.setYearsOfExperience(exp);
        return employeeSkillRepository.save(es);
    }

    private ProjectDemand createDemand(String code, String project, String role, String dept, String loc,
                                      LocalDate start, LocalDate end, int allocPercent,
                                      DemandPriority priority, DemandStatus status) {
        ProjectDemand d = new ProjectDemand();
        d.setDemandCode(code);
        d.setProjectName(project);
        d.setRole(role);
        d.setDepartment(dept);
        d.setLocation(loc);
        d.setStartDate(start);
        d.setEndDate(end);
        d.setAllocationPercent(allocPercent);
        d.setPriority(priority);
        d.setStatus(status);
        return demandRepository.save(d);
    }

    private DemandSkill addDemandSkill(ProjectDemand d, Skill s, boolean mandatory, ProficiencyLevel minProf) {
        DemandSkill ds = new DemandSkill();
        ds.setDemand(d);
        ds.setSkill(s);
        ds.setMandatory(mandatory);
        ds.setMinimumProficiency(minProf);
        return demandSkillRepository.save(ds);
    }

    private ResourceAllocation createAllocation(Employee e, ProjectDemand d, LocalDate start, LocalDate end,
                                               int allocPercent, AllocationStatus status) {
        ResourceAllocation a = new ResourceAllocation();
        a.setEmployee(e);
        a.setDemand(d);
        a.setStartDate(start);
        a.setEndDate(end);
        a.setAllocationPercent(allocPercent);
        a.setStatus(status);
        return allocationRepository.save(a);
    }

    // =========================================================================
    // REPORT 1: Open Demands by Role and Priority
    // =========================================================================
    @Test
    @DisplayName("Report 1: Aggregates open demands by role and priority, excluding cancelled/matched")
    void report1_OpenDemands_Success() throws Exception {
        createDemand("DEM-01", "Alpha", "Java Developer", "Engineering", "Bangalore",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31), 50, DemandPriority.HIGH, DemandStatus.OPEN);
        createDemand("DEM-02", "Beta", "Java Developer", "Engineering", "Bangalore",
                LocalDate.of(2026, 11, 15), LocalDate.of(2027, 1, 15), 100, DemandPriority.HIGH, DemandStatus.OPEN);
        createDemand("DEM-03", "Gamma", "DevOps Engineer", "Cloud", "Remote",
                LocalDate.of(2026, 12, 1), LocalDate.of(2027, 2, 28), 80, DemandPriority.CRITICAL, DemandStatus.OPEN);

        // Cancelled and Matched should not be included
        createDemand("DEM-04", "Delta", "Java Developer", "Engineering", "Bangalore",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31), 50, DemandPriority.HIGH, DemandStatus.CANCELLED);
        createDemand("DEM-05", "Epsilon", "Java Developer", "Engineering", "Bangalore",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31), 50, DemandPriority.HIGH, DemandStatus.MATCHED);

        mockMvc.perform(get("/api/analytics/reports/open-demands-by-role-priority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOpenDemands").value(3))
                .andExpect(jsonPath("$.data.totalRequestedCapacityPercent").value(230))
                .andExpect(jsonPath("$.data.groups.length()").value(2));
    }

    @Test
    @DisplayName("Report 1: Empty results when no open demands exist")
    void report1_OpenDemands_Empty() throws Exception {
        mockMvc.perform(get("/api/analytics/reports/open-demands-by-role-priority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOpenDemands").value(0))
                .andExpect(jsonPath("$.data.totalRequestedCapacityPercent").value(0))
                .andExpect(jsonPath("$.data.groups").isEmpty());
    }

    // =========================================================================
    // REPORT 2: Resources by Skill and Proficiency
    // =========================================================================
    @Test
    @DisplayName("Report 2: Aggregates active resources by skill and proficiency, excluding inactive resources")
    void report2_ResourcesBySkill_Success() throws Exception {
        Skill java = createSkill("Java", SkillCategory.BACKEND);
        Skill spring = createSkill("Spring Boot", SkillCategory.BACKEND);

        Employee active1 = createEmployee("EMP-01", "Alice Smith", "Eng", "NYC", EmploymentStatus.ACTIVE, 100);
        Employee active2 = createEmployee("EMP-02", "Bob Jones", "Eng", "NYC", EmploymentStatus.ACTIVE, 100);
        Employee inactive = createEmployee("EMP-03", "Charlie Inactive", "Eng", "NYC", EmploymentStatus.INACTIVE, 100);

        assignSkill(active1, java, ProficiencyLevel.ADVANCED, 5.0);
        assignSkill(active2, java, ProficiencyLevel.ADVANCED, 3.0);
        assignSkill(inactive, java, ProficiencyLevel.ADVANCED, 7.0); // should not count

        assignSkill(active1, spring, ProficiencyLevel.INTERMEDIATE, 2.0);

        mockMvc.perform(get("/api/analytics/reports/resources-by-skill-proficiency")
                        .param("category", "BACKEND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDistinctSkills").value(2))
                .andExpect(jsonPath("$.data.items[?(@.skillName == 'Java')].resourceCount").value(2))
                .andExpect(jsonPath("$.data.items[?(@.skillName == 'Spring Boot')].resourceCount").value(1));
    }

    // =========================================================================
    // REPORT 3: Availability for a Date Range
    // =========================================================================
    @Test
    @DisplayName("Report 3: Assesses availability respecting leaves, allocations, and capacity")
    void report3_AvailabilityDateRange_Success() throws Exception {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        Employee emp1 = createEmployee("EMP-01", "Dave Free", "Eng", "NYC", EmploymentStatus.ACTIVE, 100);
        Employee emp2 = createEmployee("EMP-02", "Emma Partial", "Eng", "NYC", EmploymentStatus.ACTIVE, 100);
        Employee emp3 = createEmployee("EMP-03", "Frank OnLeave", "Eng", "NYC", EmploymentStatus.ACTIVE, 100);

        ProjectDemand d = createDemand("DEM-01", "Proj", "Dev", "Eng", "NYC", start, end, 60, DemandPriority.HIGH, DemandStatus.OPEN);

        // Emp2 has a confirmed 60% allocation
        createAllocation(emp2, d, start, end, 60, AllocationStatus.CONFIRMED);

        // Emp3 has scheduled vacation
        EmployeeUnavailability unavail = new EmployeeUnavailability();
        unavail.setEmployee(emp3);
        unavail.setStartDate(LocalDate.of(2026, 11, 10));
        unavail.setEndDate(LocalDate.of(2026, 11, 20));
        unavail.setType(UnavailabilityType.ANNUAL_LEAVE);
        unavail.setReason("Vacation");
        unavailabilityRepository.save(unavail);

        mockMvc.perform(get("/api/analytics/reports/availability-date-range")
                        .param("startDate", "2026-11-01")
                        .param("endDate", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEmployeesEvaluated").value(3))
                .andExpect(jsonPath("$.data.fullyAvailableCount").value(1))
                .andExpect(jsonPath("$.data.partiallyAvailableCount").value(1))
                .andExpect(jsonPath("$.data.unavailableCount").value(1));
    }

    @Test
    @DisplayName("Report 3: Rejects invalid date range (startDate after endDate)")
    void report3_AvailabilityDateRange_InvalidDates_Returns400() throws Exception {
        mockMvc.perform(get("/api/analytics/reports/availability-date-range")
                        .param("startDate", "2026-12-01")
                        .param("endDate", "2026-11-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Start date cannot be after end date"));
    }

    // =========================================================================
    // REPORT 4: Utilization by Employee and Department
    // =========================================================================
    @Test
    @DisplayName("Report 4: Non-overlapping vs overlapping allocations and zero-allocation employees")
    void report4_Utilization_NonOverlappingVsOverlapping() throws Exception {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30); // 30 days

        Employee empZero = createEmployee("EMP-00", "Zero Alloc", "Eng", "NYC", EmploymentStatus.ACTIVE, 100);
        Employee empNonOverlap = createEmployee("EMP-01", "Non Overlap", "Eng", "NYC", EmploymentStatus.ACTIVE, 100);
        Employee empOverlap = createEmployee("EMP-02", "Overlap", "Eng", "NYC", EmploymentStatus.ACTIVE, 100);

        ProjectDemand d1 = createDemand("DEM-01", "P1", "Dev", "Eng", "NYC", start, end, 50, DemandPriority.HIGH, DemandStatus.OPEN);
        ProjectDemand d2 = createDemand("DEM-02", "P2", "Dev", "Eng", "NYC", start, end, 50, DemandPriority.HIGH, DemandStatus.OPEN);

        // empNonOverlap: Nov 1 - Nov 15 (50%), Nov 16 - Nov 30 (50%) -> Peak simultaneous is 50%, NOT 100%!
        createAllocation(empNonOverlap, d1, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 15), 50, AllocationStatus.CONFIRMED);
        createAllocation(empNonOverlap, d2, LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 30), 50, AllocationStatus.CONFIRMED);

        // empOverlap: Nov 1 - Nov 30 (50%) AND Nov 10 - Nov 20 (50%) -> Peak simultaneous is 100%!
        createAllocation(empOverlap, d1, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), 50, AllocationStatus.CONFIRMED);
        createAllocation(empOverlap, d2, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), 50, AllocationStatus.CONFIRMED);

        // Also add a CANCELLED allocation for empZero to verify it is excluded
        createAllocation(empZero, d1, start, end, 100, AllocationStatus.CANCELLED);

        mockMvc.perform(get("/api/analytics/reports/utilization-by-employee-department")
                        .param("startDate", "2026-11-01")
                        .param("endDate", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEmployees").value(3))
                // empZero has 0 allocations
                .andExpect(jsonPath("$.data.employeeDetails[?(@.employeeCode == 'EMP-00')].peakSimultaneousAllocationPercent").value(0))
                .andExpect(jsonPath("$.data.employeeDetails[?(@.employeeCode == 'EMP-00')].hasConfirmedAllocations").value(false))
                // empNonOverlap peak is 50%
                .andExpect(jsonPath("$.data.employeeDetails[?(@.employeeCode == 'EMP-01')].peakSimultaneousAllocationPercent").value(50))
                // empOverlap peak is 100%
                .andExpect(jsonPath("$.data.employeeDetails[?(@.employeeCode == 'EMP-02')].peakSimultaneousAllocationPercent").value(100));
    }

    // =========================================================================
    // REPORT 5: Demands with No Eligible Candidate
    // =========================================================================
    @Test
    @DisplayName("Report 5: Identifies demands with no eligible candidate due to mandatory skill mismatch")
    void report5_DemandsWithNoEligibleCandidate_Identified() throws Exception {
        Skill python = createSkill("Python", SkillCategory.BACKEND);
        Skill rust = createSkill("Rust", SkillCategory.BACKEND);

        Employee emp = createEmployee("EMP-01", "Pythonista", "Eng", "NYC", EmploymentStatus.ACTIVE, 100);
        assignSkill(emp, python, ProficiencyLevel.ADVANCED, 5.0);

        // Demand requiring Rust (which no employee possesses)
        ProjectDemand unfulfillable = createDemand("DEM-RUST", "Rust Rewrite", "Rust Lead", "Eng", "NYC",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31), 100, DemandPriority.CRITICAL, DemandStatus.OPEN);
        addDemandSkill(unfulfillable, rust, true, ProficiencyLevel.ADVANCED);

        // Demand requiring Python (which emp possesses)
        ProjectDemand fulfillable = createDemand("DEM-PY", "AI Service", "Python Eng", "Eng", "NYC",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31), 50, DemandPriority.MEDIUM, DemandStatus.OPEN);
        addDemandSkill(fulfillable, python, true, ProficiencyLevel.INTERMEDIATE);

        mockMvc.perform(get("/api/analytics/reports/demands-with-no-eligible-candidate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalOpenDemandsEvaluated").value(2))
                .andExpect(jsonPath("$.data.unfulfillableDemandsCount").value(1))
                .andExpect(jsonPath("$.data.unfulfillableDemands[0].demandCode").value("DEM-RUST"))
                .andExpect(jsonPath("$.data.unfulfillableDemands[0].eligibleCandidatesCount").value(0))
                .andExpect(jsonPath("$.data.unfulfillableDemands[0].primaryReason").value(
                        org.hamcrest.Matchers.containsString("No active employees possess the required mandatory skill")));
    }

    // =========================================================================
    // REPORT 6: Skill Gaps by Category
    // =========================================================================
    @Test
    @DisplayName("Report 6: Calculates net deficit per category and avoids double-counting")
    void report6_SkillGapsByCategory_Success() throws Exception {
        Skill k8s = createSkill("Kubernetes", SkillCategory.CLOUD_DEVOPS);
        Skill docker = createSkill("Docker", SkillCategory.CLOUD_DEVOPS);

        Employee devops1 = createEmployee("EMP-01", "Kube Master", "Cloud", "NYC", EmploymentStatus.ACTIVE, 100);
        assignSkill(devops1, k8s, ProficiencyLevel.ADVANCED, 4.0);
        assignSkill(devops1, docker, ProficiencyLevel.ADVANCED, 4.0);

        // 2 open demands requiring Kubernetes at ADVANCED level, but only 1 employee has it
        ProjectDemand d1 = createDemand("DEM-01", "K8s Migration 1", "DevOps", "Cloud", "NYC",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31), 100, DemandPriority.HIGH, DemandStatus.OPEN);
        addDemandSkill(d1, k8s, true, ProficiencyLevel.ADVANCED);

        ProjectDemand d2 = createDemand("DEM-02", "K8s Migration 2", "DevOps", "Cloud", "NYC",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31), 100, DemandPriority.HIGH, DemandStatus.OPEN);
        addDemandSkill(d2, k8s, true, ProficiencyLevel.ADVANCED);

        mockMvc.perform(get("/api/analytics/reports/skill-gaps-by-category")
                        .param("category", "CLOUD_DEVOPS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCategories").value(1))
                .andExpect(jsonPath("$.data.categoriesWithGapsCount").value(1))
                .andExpect(jsonPath("$.data.categoryGaps[0].category").value("CLOUD_DEVOPS"))
                .andExpect(jsonPath("$.data.categoryGaps[0].netDeficit").value(1))
                .andExpect(jsonPath("$.data.categoryGaps[0].skillGaps[0].deficit").value(1));
    }
}
