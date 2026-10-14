package com.talentmarketplace.service;

import java.util.List;

import com.talentmarketplace.dto.request.AssignSkillRequest;
import com.talentmarketplace.dto.request.UpdateEmployeeSkillRequest;
import com.talentmarketplace.dto.response.EmployeeSkillResponse;

public interface EmployeeSkillService {

    EmployeeSkillResponse assignSkillToEmployee(Long employeeId, AssignSkillRequest request);

    List<EmployeeSkillResponse> getEmployeeSkills(Long employeeId);

    EmployeeSkillResponse getEmployeeSkill(Long employeeId, Long skillId);

    EmployeeSkillResponse updateEmployeeSkill(Long employeeId, Long skillId, UpdateEmployeeSkillRequest request);

    void removeSkillFromEmployee(Long employeeId, Long skillId);

    List<EmployeeSkillResponse> getEmployeesBySkill(Long skillId);

    List<EmployeeSkillResponse> getAllEmployeeSkills();
}
