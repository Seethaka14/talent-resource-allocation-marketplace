package com.talentmarketplace.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.talentmarketplace.entity.EmploymentStatus;

public class CandidateRecommendationResponse {

    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String department;
    private String designation;
    private String location;
    private EmploymentStatus employmentStatus;

    private boolean eligible;
    private double totalScore;
    private double skillScore;
    private double availabilityScore;
    private double locationScore;

    private List<SkillMatchDetail> matchedSkills = new ArrayList<>();
    private List<SkillMatchDetail> missingMandatorySkills = new ArrayList<>();
    private List<SkillMatchDetail> missingOptionalSkills = new ArrayList<>();

    private AvailabilityAssessmentResponse availabilityAssessment;
    private List<String> exclusionReasons = new ArrayList<>();
    private String matchExplanation;

    private com.talentmarketplace.entity.AllocationStatus existingAllocationStatus;
    private Long existingAllocationId;

    public CandidateRecommendationResponse() {
    }

    public CandidateRecommendationResponse(Long employeeId, String employeeCode, String employeeName,
                                           String department, String designation, String location,
                                           EmploymentStatus employmentStatus, boolean eligible,
                                           double totalScore, double skillScore, double availabilityScore,
                                           double locationScore, List<SkillMatchDetail> matchedSkills,
                                           List<SkillMatchDetail> missingMandatorySkills,
                                           List<SkillMatchDetail> missingOptionalSkills,
                                           AvailabilityAssessmentResponse availabilityAssessment,
                                           List<String> exclusionReasons, String matchExplanation) {
        this(employeeId, employeeCode, employeeName, department, designation, location,
                employmentStatus, eligible, totalScore, skillScore, availabilityScore,
                locationScore, matchedSkills, missingMandatorySkills, missingOptionalSkills,
                availabilityAssessment, exclusionReasons, matchExplanation, null, null);
    }

    public CandidateRecommendationResponse(Long employeeId, String employeeCode, String employeeName,
                                           String department, String designation, String location,
                                           EmploymentStatus employmentStatus, boolean eligible,
                                           double totalScore, double skillScore, double availabilityScore,
                                           double locationScore, List<SkillMatchDetail> matchedSkills,
                                           List<SkillMatchDetail> missingMandatorySkills,
                                           List<SkillMatchDetail> missingOptionalSkills,
                                           AvailabilityAssessmentResponse availabilityAssessment,
                                           List<String> exclusionReasons, String matchExplanation,
                                           com.talentmarketplace.entity.AllocationStatus existingAllocationStatus,
                                           Long existingAllocationId) {
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.department = department;
        this.designation = designation;
        this.location = location;
        this.employmentStatus = employmentStatus;
        this.eligible = eligible;
        this.totalScore = totalScore;
        this.skillScore = skillScore;
        this.availabilityScore = availabilityScore;
        this.locationScore = locationScore;
        this.matchedSkills = matchedSkills != null ? matchedSkills : new ArrayList<>();
        this.missingMandatorySkills = missingMandatorySkills != null ? missingMandatorySkills : new ArrayList<>();
        this.missingOptionalSkills = missingOptionalSkills != null ? missingOptionalSkills : new ArrayList<>();
        this.availabilityAssessment = availabilityAssessment;
        this.exclusionReasons = exclusionReasons != null ? exclusionReasons : new ArrayList<>();
        this.matchExplanation = matchExplanation;
        this.existingAllocationStatus = existingAllocationStatus;
        this.existingAllocationId = existingAllocationId;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public double getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(double skillScore) {
        this.skillScore = skillScore;
    }

    public double getAvailabilityScore() {
        return availabilityScore;
    }

    public void setAvailabilityScore(double availabilityScore) {
        this.availabilityScore = availabilityScore;
    }

    public double getLocationScore() {
        return locationScore;
    }

    public void setLocationScore(double locationScore) {
        this.locationScore = locationScore;
    }

    public List<SkillMatchDetail> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<SkillMatchDetail> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<SkillMatchDetail> getMissingMandatorySkills() {
        return missingMandatorySkills;
    }

    public void setMissingMandatorySkills(List<SkillMatchDetail> missingMandatorySkills) {
        this.missingMandatorySkills = missingMandatorySkills;
    }

    public List<SkillMatchDetail> getMissingOptionalSkills() {
        return missingOptionalSkills;
    }

    public void setMissingOptionalSkills(List<SkillMatchDetail> missingOptionalSkills) {
        this.missingOptionalSkills = missingOptionalSkills;
    }

    public AvailabilityAssessmentResponse getAvailabilityAssessment() {
        return availabilityAssessment;
    }

    public void setAvailabilityAssessment(AvailabilityAssessmentResponse availabilityAssessment) {
        this.availabilityAssessment = availabilityAssessment;
    }

    public List<String> getExclusionReasons() {
        return exclusionReasons;
    }

    public void setExclusionReasons(List<String> exclusionReasons) {
        this.exclusionReasons = exclusionReasons;
    }

    public String getMatchExplanation() {
        return matchExplanation;
    }

    public void setMatchExplanation(String matchExplanation) {
        this.matchExplanation = matchExplanation;
    }

    public com.talentmarketplace.entity.AllocationStatus getExistingAllocationStatus() {
        return existingAllocationStatus;
    }

    public void setExistingAllocationStatus(com.talentmarketplace.entity.AllocationStatus existingAllocationStatus) {
        this.existingAllocationStatus = existingAllocationStatus;
    }

    public Long getExistingAllocationId() {
        return existingAllocationId;
    }

    public void setExistingAllocationId(Long existingAllocationId) {
        this.existingAllocationId = existingAllocationId;
    }
}
