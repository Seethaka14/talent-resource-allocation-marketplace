package com.talentmarketplace.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentmarketplace.entity.EmployeeSkill;
import com.talentmarketplace.entity.ProficiencyLevel;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {

    @Query("SELECT es FROM EmployeeSkill es " +
           "JOIN FETCH es.employee " +
           "JOIN FETCH es.skill " +
           "ORDER BY es.employee.id ASC, es.skill.name ASC")
    List<EmployeeSkill> findAllWithDetails();

    @Query("SELECT es FROM EmployeeSkill es " +
           "JOIN FETCH es.employee " +
           "JOIN FETCH es.skill " +
           "WHERE es.employee.id = :employeeId")
    List<EmployeeSkill> findByEmployeeIdWithDetails(@Param("employeeId") Long employeeId);

    @Query("SELECT es FROM EmployeeSkill es " +
           "JOIN FETCH es.employee " +
           "JOIN FETCH es.skill " +
           "WHERE es.skill.id = :skillId")
    List<EmployeeSkill> findBySkillIdWithDetails(@Param("skillId") Long skillId);

    List<EmployeeSkill> findByEmployeeId(Long employeeId);

    List<EmployeeSkill> findBySkillId(Long skillId);

    Optional<EmployeeSkill> findByEmployeeIdAndSkillId(Long employeeId, Long skillId);

    boolean existsByEmployeeIdAndSkillId(Long employeeId, Long skillId);

    void deleteByEmployeeIdAndSkillId(Long employeeId, Long skillId);

    @Query("SELECT es FROM EmployeeSkill es " +
           "JOIN FETCH es.employee " +
           "JOIN FETCH es.skill " +
           "WHERE es.skill.id = :skillId AND es.proficiencyLevel = :level")
    List<EmployeeSkill> findBySkillIdAndProficiencyLevel(
            @Param("skillId") Long skillId,
            @Param("level") ProficiencyLevel level);
}
