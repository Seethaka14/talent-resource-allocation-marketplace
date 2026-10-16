package com.talentmarketplace.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentmarketplace.dto.request.CreateDemandRequest;
import com.talentmarketplace.dto.request.DemandSkillRequest;
import com.talentmarketplace.dto.request.UpdateDemandRequest;
import com.talentmarketplace.dto.response.DemandResponse;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandSkill;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.exception.BusinessRuleValidationException;
import com.talentmarketplace.exception.DuplicateResourceException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.DemandSkillRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.SkillRepository;

@Service
@Transactional
public class ProjectDemandServiceImpl implements ProjectDemandService {

    private final ProjectDemandRepository demandRepository;
    private final SkillRepository skillRepository;
    private final DemandSkillRepository demandSkillRepository;
    private final DemandCodeGeneratorService demandCodeGeneratorService;

    public ProjectDemandServiceImpl(ProjectDemandRepository demandRepository,
                                    SkillRepository skillRepository,
                                    DemandSkillRepository demandSkillRepository,
                                    DemandCodeGeneratorService demandCodeGeneratorService) {
        this.demandRepository = demandRepository;
        this.skillRepository = skillRepository;
        this.demandSkillRepository = demandSkillRepository;
        this.demandCodeGeneratorService = demandCodeGeneratorService;
    }

    @Override
    public DemandResponse createDemand(CreateDemandRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleValidationException("Start date cannot be after end date");
        }

        if (request.getAllocationPercent() < 1 || request.getAllocationPercent() > 100) {
            throw new BusinessRuleValidationException("Allocation percent must be between 1 and 100");
        }

        String trimmedCode;
        if (request.getDemandCode() == null || request.getDemandCode().trim().isEmpty()) {
            trimmedCode = demandCodeGeneratorService.generateNextDemandCode();
        } else {
            trimmedCode = request.getDemandCode().trim();
            if (demandRepository.existsByDemandCode(trimmedCode)) {
                throw new DuplicateResourceException("ProjectDemand", "demandCode", trimmedCode);
            }
        }

        ProjectDemand demand = new ProjectDemand(
                trimmedCode,
                request.getProjectName().trim(),
                request.getRole().trim(),
                request.getDepartment() != null ? request.getDepartment().trim() : null,
                request.getLocation().trim(),
                request.getStartDate(),
                request.getEndDate(),
                request.getAllocationPercent(),
                request.getPriority(),
                DemandStatus.OPEN,
                request.getRequiredExperienceYears()
        );

        // Process required skills if provided
        if (request.getRequiredSkills() != null && !request.getRequiredSkills().isEmpty()) {
            Set<Long> processedSkillIds = new HashSet<>();
            for (DemandSkillRequest skillReq : request.getRequiredSkills()) {
                if (!processedSkillIds.add(skillReq.getSkillId())) {
                    throw new BusinessRuleValidationException(
                            "Duplicate skill requirement detected for skill ID: " + skillReq.getSkillId()
                    );
                }
            }
            for (DemandSkillRequest skillReq : request.getRequiredSkills()) {
                Skill skill = skillRepository.findById(skillReq.getSkillId())
                        .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillReq.getSkillId()));

                DemandSkill demandSkill = new DemandSkill(
                        demand,
                        skill,
                        skillReq.isMandatory(),
                        skillReq.getMinimumProficiency()
                );
                demand.addRequiredSkill(demandSkill);
            }
        }

        ProjectDemand saved = demandRepository.save(demand);
        return DemandResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandResponse> searchDemands(DemandStatus status, DemandPriority priority,
                                            String role, String location,
                                            LocalDate fromDate, LocalDate toDate, String search) {
        String trimmedRole = (role != null && !role.isBlank()) ? role.trim() : null;
        String trimmedLoc = (location != null && !location.isBlank()) ? location.trim() : null;
        String trimmedSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        return demandRepository.searchDemands(status, priority, trimmedRole, trimmedLoc, fromDate, toDate, trimmedSearch)
                .stream()
                .map(DemandResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DemandResponse getDemandById(Long id) {
        ProjectDemand demand = demandRepository.findByIdWithSkills(id)
                .or(() -> demandRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("ProjectDemand", "id", id));
        return DemandResponse.fromEntity(demand);
    }

    @Override
    @Transactional(readOnly = true)
    public DemandResponse getDemandByCode(String demandCode) {
        ProjectDemand demand = demandRepository.findByDemandCodeWithSkills(demandCode.trim())
                .or(() -> demandRepository.findByDemandCode(demandCode.trim()))
                .orElseThrow(() -> new ResourceNotFoundException("ProjectDemand", "demandCode", demandCode));
        return DemandResponse.fromEntity(demand);
    }

    @Override
    public DemandResponse updateDemand(Long id, UpdateDemandRequest request) {
        ProjectDemand demand = demandRepository.findByIdWithSkills(id)
                .or(() -> demandRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("ProjectDemand", "id", id));

        if (demand.getStatus() == DemandStatus.CANCELLED) {
            throw new BusinessRuleValidationException("Cannot modify a cancelled demand");
        }

        if (!demand.getStatus().canTransitionTo(request.getStatus())) {
            throw new BusinessRuleValidationException(String.format(
                    "Invalid demand status transition from %s to %s",
                    demand.getStatus(), request.getStatus()
            ));
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleValidationException("Start date cannot be after end date");
        }

        if (request.getAllocationPercent() < 1 || request.getAllocationPercent() > 100) {
            throw new BusinessRuleValidationException("Allocation percent must be between 1 and 100");
        }

        demand.setProjectName(request.getProjectName().trim());
        demand.setRole(request.getRole().trim());
        demand.setDepartment(request.getDepartment() != null ? request.getDepartment().trim() : null);
        demand.setLocation(request.getLocation().trim());
        demand.setStartDate(request.getStartDate());
        demand.setEndDate(request.getEndDate());
        demand.setAllocationPercent(request.getAllocationPercent());
        demand.setPriority(request.getPriority());
        demand.setStatus(request.getStatus());
        if (request.getRequiredExperienceYears() != null) {
            demand.setRequiredExperienceYears(request.getRequiredExperienceYears());
        }

        if (request.getRequiredSkills() != null) {
            demand.getRequiredSkills().clear();
            Set<Long> processedSkillIds = new HashSet<>();
            for (DemandSkillRequest skillReq : request.getRequiredSkills()) {
                if (!processedSkillIds.add(skillReq.getSkillId())) {
                    throw new BusinessRuleValidationException(
                            "Duplicate skill requirement detected for skill ID: " + skillReq.getSkillId()
                    );
                }
            }
            for (DemandSkillRequest skillReq : request.getRequiredSkills()) {
                Skill skill = skillRepository.findById(skillReq.getSkillId())
                        .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillReq.getSkillId()));

                DemandSkill demandSkill = new DemandSkill(
                        demand,
                        skill,
                        skillReq.isMandatory(),
                        skillReq.getMinimumProficiency()
                );
                demand.addRequiredSkill(demandSkill);
            }
        }

        ProjectDemand updated = demandRepository.save(demand);
        return DemandResponse.fromEntity(updated);
    }

    @Override
    public DemandResponse cancelDemand(Long id, String reason) {
        ProjectDemand demand = demandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectDemand", "id", id));

        demand.setStatus(DemandStatus.CANCELLED);
        ProjectDemand cancelled = demandRepository.save(demand);
        return DemandResponse.fromEntity(cancelled);
    }

    @Override
    public DemandResponse addRequiredSkill(Long demandId, DemandSkillRequest request) {
        ProjectDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectDemand", "id", demandId));

        if (demand.getStatus() == DemandStatus.CANCELLED) {
            throw new BusinessRuleValidationException("Cannot add skills to a cancelled demand");
        }

        if (demandSkillRepository.existsByDemandIdAndSkillId(demandId, request.getSkillId())) {
            throw new DuplicateResourceException(
                    "Skill ID " + request.getSkillId() + " is already required for demand ID " + demandId
            );
        }

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", request.getSkillId()));

        DemandSkill demandSkill = new DemandSkill(
                demand,
                skill,
                request.isMandatory(),
                request.getMinimumProficiency()
        );
        demand.addRequiredSkill(demandSkill);

        ProjectDemand saved = demandRepository.save(demand);
        return DemandResponse.fromEntity(saved);
    }

    @Override
    public DemandResponse removeRequiredSkill(Long demandId, Long skillId) {
        ProjectDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectDemand", "id", demandId));

        if (demand.getStatus() == DemandStatus.CANCELLED) {
            throw new BusinessRuleValidationException("Cannot remove skills from a cancelled demand");
        }

        DemandSkill demandSkill = demandSkillRepository.findByDemandIdAndSkillId(demandId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill ID " + skillId + " is not attached to demand ID " + demandId
                ));

        demand.removeRequiredSkill(demandSkill);
        demandSkillRepository.delete(demandSkill);

        return DemandResponse.fromEntity(demand);
    }
}
