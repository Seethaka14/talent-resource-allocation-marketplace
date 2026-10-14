package com.talentmarketplace.service;

import java.util.List;

import com.talentmarketplace.dto.request.CreateSkillRequest;
import com.talentmarketplace.dto.request.UpdateSkillRequest;
import com.talentmarketplace.dto.response.SkillResponse;
import com.talentmarketplace.entity.SkillCategory;

public interface SkillService {

    SkillResponse createSkill(CreateSkillRequest request);

    SkillResponse getSkillById(Long id);

    SkillResponse getSkillByName(String name);

    List<SkillResponse> searchSkills(SkillCategory category, String search);

    SkillResponse updateSkill(Long id, UpdateSkillRequest request);

    void deleteSkill(Long id);
}
