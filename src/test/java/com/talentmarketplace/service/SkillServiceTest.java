package com.talentmarketplace.service;

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

import com.talentmarketplace.dto.request.CreateSkillRequest;
import com.talentmarketplace.dto.request.UpdateSkillRequest;
import com.talentmarketplace.dto.response.SkillResponse;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;
import com.talentmarketplace.exception.DuplicateResourceException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.SkillRepository;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillServiceImpl skillService;

    private CreateSkillRequest createRequest;
    private Skill sampleSkill;

    @BeforeEach
    void setUp() {
        createRequest = new CreateSkillRequest("Java", SkillCategory.BACKEND, "Java programming language");
        sampleSkill = new Skill("Java", SkillCategory.BACKEND, "Java programming language");
        sampleSkill.setId(1L);
    }

    @Test
    @DisplayName("createSkill: creates skill when name is unique")
    void createSkill_Success() {
        when(skillRepository.existsByNameIgnoreCase("Java")).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenReturn(sampleSkill);

        SkillResponse response = skillService.createSkill(createRequest);

        assertNotNull(response);
        assertEquals("Java", response.getName());
        assertEquals(SkillCategory.BACKEND, response.getCategory());
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    @DisplayName("createSkill: throws DuplicateResourceException when name exists")
    void createSkill_DuplicateName() {
        when(skillRepository.existsByNameIgnoreCase("Java")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                skillService.createSkill(createRequest)
        );
        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    @DisplayName("getSkillById: returns skill when found")
    void getSkillById_Success() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(sampleSkill));

        SkillResponse response = skillService.getSkillById(1L);

        assertNotNull(response);
        assertEquals("Java", response.getName());
    }

    @Test
    @DisplayName("getSkillById: throws ResourceNotFoundException when not found")
    void getSkillById_NotFound() {
        when(skillRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                skillService.getSkillById(999L)
        );
    }

    @Test
    @DisplayName("updateSkill: updates skill successfully")
    void updateSkill_Success() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(sampleSkill));
        when(skillRepository.existsByNameIgnoreCaseAndIdNot("Java 21", 1L)).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenReturn(sampleSkill);

        UpdateSkillRequest updateReq = new UpdateSkillRequest("Java 21", SkillCategory.BACKEND, "Updated desc");
        SkillResponse response = skillService.updateSkill(1L, updateReq);

        assertNotNull(response);
        verify(skillRepository).save(sampleSkill);
    }

    @Test
    @DisplayName("deleteSkill: deletes existing skill")
    void deleteSkill_Success() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(sampleSkill));

        skillService.deleteSkill(1L);

        verify(skillRepository).delete(sampleSkill);
    }

    @Test
    @DisplayName("searchSkills: returns filtered list")
    void searchSkills_Success() {
        when(skillRepository.searchSkills(SkillCategory.BACKEND, "Java"))
                .thenReturn(List.of(sampleSkill));

        List<SkillResponse> list = skillService.searchSkills(SkillCategory.BACKEND, " Java ");

        assertEquals(1, list.size());
        assertEquals("Java", list.get(0).getName());
    }
}
