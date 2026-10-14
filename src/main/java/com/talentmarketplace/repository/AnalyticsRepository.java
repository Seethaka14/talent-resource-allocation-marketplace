package com.talentmarketplace.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentmarketplace.entity.ProjectDemand;

@Repository
public interface AnalyticsRepository extends JpaRepository<ProjectDemand, Long> {

    // =========================================================================
    // REPORT 1: Open Demands by Role and Priority
    // =========================================================================
    @Query(value = """
        SELECT 
            d.role AS role,
            d.priority AS priority,
            COUNT(d.id) AS demandCount,
            COALESCE(SUM(d.allocation_percent), 0) AS totalAllocationPercent,
            MIN(d.start_date) AS earliestStartDate,
            MAX(d.end_date) AS latestEndDate
        FROM project_demands d
        WHERE d.status = 'OPEN'
        GROUP BY d.role, d.priority
        ORDER BY d.role ASC, 
            CASE d.priority
                WHEN 'CRITICAL' THEN 1
                WHEN 'HIGH' THEN 2
                WHEN 'MEDIUM' THEN 3
                WHEN 'LOW' THEN 4
                ELSE 5
            END ASC
    """, nativeQuery = true)
    List<RolePriorityDemandProjection> findOpenDemandsByRoleAndPriority();

    interface RolePriorityDemandProjection {
        String getRole();
        String getPriority();
        Long getDemandCount();
        Long getTotalAllocationPercent();
        LocalDate getEarliestStartDate();
        LocalDate getLatestEndDate();
    }

    // =========================================================================
    // REPORT 2: Resources by Skill and Proficiency
    // =========================================================================
    @Query(value = """
        SELECT 
            s.id AS skillId,
            s.name AS skillName,
            s.category AS category,
            es.proficiency_level AS proficiencyLevel,
            COUNT(DISTINCT e.id) AS resourceCount,
            COALESCE(ROUND(AVG(es.years_of_experience), 1), 0.0) AS avgYearsExperience
        FROM skills s
        JOIN employee_skills es ON s.id = es.skill_id
        JOIN employees e ON es.employee_id = e.id
        WHERE e.employment_status = 'ACTIVE'
          AND (:skillId IS NULL OR s.id = :skillId)
          AND (:category IS NULL OR s.category = :category)
          AND (:proficiencyLevel IS NULL OR es.proficiency_level = :proficiencyLevel)
        GROUP BY s.id, s.name, s.category, es.proficiency_level
        ORDER BY s.category ASC, s.name ASC, 
            CASE es.proficiency_level
                WHEN 'ADVANCED' THEN 1
                WHEN 'INTERMEDIATE' THEN 2
                WHEN 'BEGINNER' THEN 3
                ELSE 4
            END ASC
    """, nativeQuery = true)
    List<SkillProficiencyResourceProjection> findResourcesBySkillAndProficiency(
            @Param("skillId") Long skillId,
            @Param("category") String category,
            @Param("proficiencyLevel") String proficiencyLevel);

    interface SkillProficiencyResourceProjection {
        Long getSkillId();
        String getSkillName();
        String getCategory();
        String getProficiencyLevel();
        Long getResourceCount();
        Double getAvgYearsExperience();
    }

    // =========================================================================
    // REPORT 5: Candidate Open Demands for Eligibility Evaluation
    // =========================================================================
    @Query(value = """
        SELECT 
            d.id AS demandId,
            d.demand_code AS demandCode,
            d.project_name AS projectName,
            d.role AS role,
            d.department AS department,
            d.location AS location,
            d.start_date AS startDate,
            d.end_date AS endDate,
            d.allocation_percent AS allocationPercent,
            d.priority AS priority,
            COUNT(ds.id) AS totalRequiredSkills,
            COALESCE(SUM(CASE WHEN ds.mandatory = true THEN 1 ELSE 0 END), 0) AS mandatorySkillsCount
        FROM project_demands d
        LEFT JOIN demand_skills ds ON d.id = ds.demand_id
        WHERE d.status = 'OPEN'
        GROUP BY d.id, d.demand_code, d.project_name, d.role, d.department, d.location, 
                 d.start_date, d.end_date, d.allocation_percent, d.priority
        ORDER BY 
            CASE d.priority
                WHEN 'CRITICAL' THEN 1
                WHEN 'HIGH' THEN 2
                WHEN 'MEDIUM' THEN 3
                WHEN 'LOW' THEN 4
                ELSE 5
            END ASC,
            d.start_date ASC
    """, nativeQuery = true)
    List<CandidateOpenDemandProjection> findOpenDemandsForEligibilityAssessment();

    interface CandidateOpenDemandProjection {
        Long getDemandId();
        String getDemandCode();
        String getProjectName();
        String getRole();
        String getDepartment();
        String getLocation();
        LocalDate getStartDate();
        LocalDate getEndDate();
        Integer getAllocationPercent();
        String getPriority();
        Long getTotalRequiredSkills();
        Long getMandatorySkillsCount();
    }

    // =========================================================================
    // REPORT 6: Skill Demand Requirements by Category
    // =========================================================================
    @Query(value = """
        SELECT 
            s.category AS category,
            s.id AS skillId,
            s.name AS skillName,
            ds.minimum_proficiency AS minimumProficiency,
            COUNT(DISTINCT d.id) AS demandCount,
            COALESCE(SUM(CASE WHEN ds.mandatory = true THEN 1 ELSE 0 END), 0) AS mandatoryCount,
            COALESCE(SUM(CASE WHEN ds.mandatory = false THEN 1 ELSE 0 END), 0) AS optionalCount
        FROM demand_skills ds
        JOIN skills s ON ds.skill_id = s.id
        JOIN project_demands d ON ds.demand_id = d.id
        WHERE d.status = 'OPEN'
          AND (:category IS NULL OR s.category = :category)
        GROUP BY s.category, s.id, s.name, ds.minimum_proficiency
        ORDER BY s.category ASC, s.name ASC
    """, nativeQuery = true)
    List<DemandSkillRequirementProjection> findOpenDemandSkillRequirements(@Param("category") String category);

    interface DemandSkillRequirementProjection {
        String getCategory();
        Long getSkillId();
        String getSkillName();
        String getMinimumProficiency();
        Long getDemandCount();
        Long getMandatoryCount();
        Long getOptionalCount();
    }

    // =========================================================================
    // REPORT 6: Active Employee Skill Supply by Category
    // =========================================================================
    @Query(value = """
        SELECT 
            s.category AS category,
            s.id AS skillId,
            s.name AS skillName,
            es.proficiency_level AS proficiencyLevel,
            COUNT(DISTINCT e.id) AS resourceCount
        FROM employee_skills es
        JOIN skills s ON es.skill_id = s.id
        JOIN employees e ON es.employee_id = e.id
        WHERE e.employment_status = 'ACTIVE'
          AND (:category IS NULL OR s.category = :category)
        GROUP BY s.category, s.id, s.name, es.proficiency_level
        ORDER BY s.category ASC, s.name ASC
    """, nativeQuery = true)
    List<EmployeeSkillSupplyProjection> findActiveEmployeeSkillSupply(@Param("category") String category);

    interface EmployeeSkillSupplyProjection {
        String getCategory();
        Long getSkillId();
        String getSkillName();
        String getProficiencyLevel();
        Long getResourceCount();
    }
}
