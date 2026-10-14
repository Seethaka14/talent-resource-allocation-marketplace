package com.talentmarketplace.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.talentmarketplace.dto.request.AssignSkillRequest;
import com.talentmarketplace.dto.request.UpdateEmployeeSkillRequest;
import com.talentmarketplace.dto.response.EmployeeSkillResponse;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeSkill;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;
import com.talentmarketplace.exception.DuplicateResourceException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.SkillRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeSkillServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;

    @InjectMocks
    private EmployeeSkillServiceImpl employeeSkillService;

    private Employee employee;
    private Skill skill;
    private EmployeeSkill employeeSkill;
    private AssignSkillRequest assignRequest;

    @BeforeEach
    void setUp() {
        employee = new Employee(
                "EMP-001", "Alice", "Smith", "alice@example.com",
                "Engineering", "Senior Dev", "Bangalore",
                LocalDate.of(2023, 1, 1), EmploymentStatus.ACTIVE
        );
        employee.setId(1L);

        skill = new Skill("Spring Boot", SkillCategory.BACKEND, "Framework");
        skill.setId(10L);

        employeeSkill = new EmployeeSkill(
                employee, skill, ProficiencyLevel.ADVANCED, 4.0,
                LocalDate.of(2024, 1, 1), "Tech Lead", "Built core microservices"
        );
        employeeSkill.setId(100L);

        assignRequest = new AssignSkillRequest(
                10L, ProficiencyLevel.ADVANCED, 4.0,
                LocalDate.of(2024, 1, 1), "Tech Lead", "Built core microservices"
        );
    }

    @Test
    @DisplayName("assignSkillToEmployee: successfully assigns skill")
    void assignSkill_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
        when(employeeSkillRepository.existsByEmployeeIdAndSkillId(1L, 10L)).thenReturn(false);
        when(employeeSkillRepository.save(any(EmployeeSkill.class))).thenReturn(employeeSkill);

        EmployeeSkillResponse response = employeeSkillService.assignSkillToEmployee(1L, assignRequest);

        assertNotNull(response);
        assertEquals("Spring Boot", response.getSkillName());
        assertEquals(ProficiencyLevel.ADVANCED, response.getProficiencyLevel());
        assertEquals(3, response.getProficiencyWeight());
        verify(employeeSkillRepository).save(any(EmployeeSkill.class));
    }

    @Test
    @DisplayName("assignSkillToEmployee: throws ResourceNotFoundException when employee not found")
    void assignSkill_EmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                employeeSkillService.assignSkillToEmployee(999L, assignRequest)
        );
        verify(employeeSkillRepository, never()).save(any(EmployeeSkill.class));
    }

    @Test
    @DisplayName("assignSkillToEmployee: throws ResourceNotFoundException when skill not found")
    void assignSkill_SkillNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(skillRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                employeeSkillService.assignSkillToEmployee(1L, assignRequest)
        );
        verify(employeeSkillRepository, never()).save(any(EmployeeSkill.class));
    }

    @Test
    @DisplayName("assignSkillToEmployee: throws DuplicateResourceException when already assigned")
    void assignSkill_AlreadyAssigned() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
        when(employeeSkillRepository.existsByEmployeeIdAndSkillId(1L, 10L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                employeeSkillService.assignSkillToEmployee(1L, assignRequest)
        );
        verify(employeeSkillRepository, never()).save(any(EmployeeSkill.class));
    }

    @Test
    @DisplayName("getEmployeeSkills: returns skills for employee")
    void getEmployeeSkills_Success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(employeeSkillRepository.findByEmployeeIdWithDetails(1L)).thenReturn(List.of(employeeSkill));

        List<EmployeeSkillResponse> skills = employeeSkillService.getEmployeeSkills(1L);

        assertEquals(1, skills.size());
        assertEquals("Spring Boot", skills.get(0).getSkillName());
    }

    @Test
    @DisplayName("updateEmployeeSkill: updates proficiency and notes")
    void updateEmployeeSkill_Success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(skillRepository.existsById(10L)).thenReturn(true);
        when(employeeSkillRepository.findByEmployeeIdAndSkillId(1L, 10L)).thenReturn(Optional.of(employeeSkill));
        when(employeeSkillRepository.save(any(EmployeeSkill.class))).thenReturn(employeeSkill);

        UpdateEmployeeSkillRequest updateReq = new UpdateEmployeeSkillRequest(
                ProficiencyLevel.INTERMEDIATE, 5.0, LocalDate.now(), "Peer", "Updated evidence"
        );

        EmployeeSkillResponse response = employeeSkillService.updateEmployeeSkill(1L, 10L, updateReq);

        assertNotNull(response);
        verify(employeeSkillRepository).save(employeeSkill);
    }

    @Test
    @DisplayName("removeSkillFromEmployee: removes assignment")
    void removeSkill_Success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(skillRepository.existsById(10L)).thenReturn(true);
        when(employeeSkillRepository.findByEmployeeIdAndSkillId(1L, 10L)).thenReturn(Optional.of(employeeSkill));

        employeeSkillService.removeSkillFromEmployee(1L, 10L);

        verify(employeeSkillRepository).delete(employeeSkill);
    }
}
