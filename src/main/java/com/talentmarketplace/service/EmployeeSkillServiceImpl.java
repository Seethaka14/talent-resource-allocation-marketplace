package com.talentmarketplace.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentmarketplace.dto.request.AssignSkillRequest;
import com.talentmarketplace.dto.request.UpdateEmployeeSkillRequest;
import com.talentmarketplace.dto.response.EmployeeSkillResponse;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeSkill;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.exception.DuplicateResourceException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.SkillRepository;

@Service
@Transactional
public class EmployeeSkillServiceImpl implements EmployeeSkillService {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;

    public EmployeeSkillServiceImpl(EmployeeRepository employeeRepository,
                                    SkillRepository skillRepository,
                                    EmployeeSkillRepository employeeSkillRepository) {
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
        this.employeeSkillRepository = employeeSkillRepository;
    }

    @Override
    public EmployeeSkillResponse assignSkillToEmployee(Long employeeId, AssignSkillRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", request.getSkillId()));

        if (employeeSkillRepository.existsByEmployeeIdAndSkillId(employeeId, request.getSkillId())) {
            throw new DuplicateResourceException(
                    String.format("Skill '%s' is already assigned to employee '%s'", skill.getName(), employee.getEmployeeCode())
            );
        }

        EmployeeSkill employeeSkill = new EmployeeSkill(
                employee,
                skill,
                request.getProficiencyLevel(),
                request.getYearsOfExperience() != null ? request.getYearsOfExperience() : 0.0,
                request.getLastValidatedAt() != null ? request.getLastValidatedAt() : LocalDate.now(),
                request.getValidatedBy() != null ? request.getValidatedBy().trim() : null,
                request.getEvidenceNotes() != null ? request.getEvidenceNotes().trim() : null
        );

        EmployeeSkill saved = employeeSkillRepository.save(employeeSkill);
        return EmployeeSkillResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSkillResponse> getEmployeeSkills(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        return employeeSkillRepository.findByEmployeeIdWithDetails(employeeId)
                .stream()
                .map(EmployeeSkillResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeSkillResponse getEmployeeSkill(Long employeeId, Long skillId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
        if (!skillRepository.existsById(skillId)) {
            throw new ResourceNotFoundException("Skill", "id", skillId);
        }

        EmployeeSkill employeeSkill = employeeSkillRepository.findByEmployeeIdAndSkillId(employeeId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Skill ID %d is not assigned to Employee ID %d", skillId, employeeId)
                ));

        return EmployeeSkillResponse.fromEntity(employeeSkill);
    }

    @Override
    public EmployeeSkillResponse updateEmployeeSkill(Long employeeId, Long skillId, UpdateEmployeeSkillRequest request) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
        if (!skillRepository.existsById(skillId)) {
            throw new ResourceNotFoundException("Skill", "id", skillId);
        }

        EmployeeSkill employeeSkill = employeeSkillRepository.findByEmployeeIdAndSkillId(employeeId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Skill ID %d is not assigned to Employee ID %d", skillId, employeeId)
                ));

        employeeSkill.setProficiencyLevel(request.getProficiencyLevel());
        if (request.getYearsOfExperience() != null) {
            employeeSkill.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getLastValidatedAt() != null) {
            employeeSkill.setLastValidatedAt(request.getLastValidatedAt());
        }
        if (request.getValidatedBy() != null) {
            employeeSkill.setValidatedBy(request.getValidatedBy().trim());
        }
        if (request.getEvidenceNotes() != null) {
            employeeSkill.setEvidenceNotes(request.getEvidenceNotes().trim());
        }

        EmployeeSkill updated = employeeSkillRepository.save(employeeSkill);
        return EmployeeSkillResponse.fromEntity(updated);
    }

    @Override
    public void removeSkillFromEmployee(Long employeeId, Long skillId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
        if (!skillRepository.existsById(skillId)) {
            throw new ResourceNotFoundException("Skill", "id", skillId);
        }

        EmployeeSkill employeeSkill = employeeSkillRepository.findByEmployeeIdAndSkillId(employeeId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Skill ID %d is not assigned to Employee ID %d", skillId, employeeId)
                ));

        employeeSkillRepository.delete(employeeSkill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSkillResponse> getEmployeesBySkill(Long skillId) {
        if (!skillRepository.existsById(skillId)) {
            throw new ResourceNotFoundException("Skill", "id", skillId);
        }

        return employeeSkillRepository.findBySkillIdWithDetails(skillId)
                .stream()
                .map(EmployeeSkillResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSkillResponse> getAllEmployeeSkills() {
        return employeeSkillRepository.findAllWithDetails()
                .stream()
                .map(EmployeeSkillResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
