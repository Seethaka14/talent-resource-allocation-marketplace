package com.talentmarketplace.service;

import java.time.LocalDate;
import java.util.List;

import com.talentmarketplace.dto.request.CreateDemandRequest;
import com.talentmarketplace.dto.request.DemandSkillRequest;
import com.talentmarketplace.dto.request.UpdateDemandRequest;
import com.talentmarketplace.dto.response.DemandResponse;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandStatus;

public interface ProjectDemandService {

    DemandResponse createDemand(CreateDemandRequest request);

    List<DemandResponse> searchDemands(DemandStatus status, DemandPriority priority,
                                      String role, String location,
                                      LocalDate fromDate, LocalDate toDate, String search);

    DemandResponse getDemandById(Long id);

    DemandResponse getDemandByCode(String demandCode);

    DemandResponse updateDemand(Long id, UpdateDemandRequest request);

    DemandResponse cancelDemand(Long id, String reason);

    DemandResponse addRequiredSkill(Long demandId, DemandSkillRequest request);

    DemandResponse removeRequiredSkill(Long demandId, Long skillId);
}
