package com.talentmarketplace.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.talentmarketplace.entity.EmployeeSkill;
import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.SkillCategory;

public class EmployeeSkillResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Long skillId;
    private String skillName;
    private SkillCategory skillCategory;
    private ProficiencyLevel proficiencyLevel;
    private int proficiencyWeight;
    private Double yearsOfExperience;
    private LocalDate lastValidatedAt;
    private String validatedBy;
    private String evidenceNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeSkillResponse() {
    }

    public EmployeeSkillResponse(Long id, Long employeeId, String employeeCode, String employeeName,
                                 Long skillId, String skillName, SkillCategory skillCategory,
                                 ProficiencyLevel proficiencyLevel, int proficiencyWeight,
                                 Double yearsOfExperience, LocalDate lastValidatedAt,
                                 String validatedBy, String evidenceNotes,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.skillId = skillId;
        this.skillName = skillName;
        this.skillCategory = skillCategory;
        this.proficiencyLevel = proficiencyLevel;
        this.proficiencyWeight = proficiencyWeight;
        this.yearsOfExperience = yearsOfExperience;
        this.lastValidatedAt = lastValidatedAt;
        this.validatedBy = validatedBy;
        this.evidenceNotes = evidenceNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static EmployeeSkillResponse fromEntity(EmployeeSkill es) {
        if (es == null) {
            return null;
        }

        Long empId = es.getEmployee() != null ? es.getEmployee().getId() : null;
        String empCode = es.getEmployee() != null ? es.getEmployee().getEmployeeCode() : null;
        String empName = es.getEmployee() != null ? es.getEmployee().getName() : null;

        Long sId = es.getSkill() != null ? es.getSkill().getId() : null;
        String sName = es.getSkill() != null ? es.getSkill().getName() : null;
        SkillCategory sCat = es.getSkill() != null ? es.getSkill().getCategory() : null;

        int weight = es.getProficiencyLevel() != null ? es.getProficiencyLevel().getLevelWeight() : 0;

        return new EmployeeSkillResponse(
                es.getId(),
                empId,
                empCode,
                empName,
                sId,
                sName,
                sCat,
                es.getProficiencyLevel(),
                weight,
                es.getYearsOfExperience(),
                es.getLastValidatedAt(),
                es.getValidatedBy(),
                es.getEvidenceNotes(),
                es.getCreatedAt(),
                es.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public SkillCategory getSkillCategory() {
        return skillCategory;
    }

    public void setSkillCategory(SkillCategory skillCategory) {
        this.skillCategory = skillCategory;
    }

    public ProficiencyLevel getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public int getProficiencyWeight() {
        return proficiencyWeight;
    }

    public void setProficiencyWeight(int proficiencyWeight) {
        this.proficiencyWeight = proficiencyWeight;
    }

    public Double getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Double yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public LocalDate getLastValidatedAt() {
        return lastValidatedAt;
    }

    public void setLastValidatedAt(LocalDate lastValidatedAt) {
        this.lastValidatedAt = lastValidatedAt;
    }

    public String getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(String validatedBy) {
        this.validatedBy = validatedBy;
    }

    public String getEvidenceNotes() {
        return evidenceNotes;
    }

    public void setEvidenceNotes(String evidenceNotes) {
        this.evidenceNotes = evidenceNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
