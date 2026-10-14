package com.talentmarketplace.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentmarketplace.dto.request.CreateSkillRequest;
import com.talentmarketplace.dto.request.UpdateSkillRequest;
import com.talentmarketplace.dto.response.SkillResponse;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;
import com.talentmarketplace.exception.DuplicateResourceException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.SkillRepository;

@Service
@Transactional
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    public SkillServiceImpl(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    public SkillResponse createSkill(CreateSkillRequest request) {
        String trimmedName = request.getName().trim();
        if (skillRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException("Skill", "name", trimmedName);
        }

        Skill skill = new Skill(
                trimmedName,
                request.getCategory(),
                request.getDescription() != null ? request.getDescription().trim() : null
        );

        Skill saved = skillRepository.save(skill);
        return SkillResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SkillResponse getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));
        return SkillResponse.fromEntity(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public SkillResponse getSkillByName(String name) {
        Skill skill = skillRepository.findByNameIgnoreCase(name.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "name", name));
        return SkillResponse.fromEntity(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> searchSkills(SkillCategory category, String search) {
        String trimmedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        return skillRepository.searchSkills(category, trimmedSearch)
                .stream()
                .map(SkillResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public SkillResponse updateSkill(Long id, UpdateSkillRequest request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));

        String trimmedName = request.getName().trim();
        if (skillRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new DuplicateResourceException("Skill", "name", trimmedName);
        }

        skill.setName(trimmedName);
        skill.setCategory(request.getCategory());
        skill.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Skill updated = skillRepository.save(skill);
        return SkillResponse.fromEntity(updated);
    }

    @Override
    public void deleteSkill(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));
        skillRepository.delete(skill);
    }
}
