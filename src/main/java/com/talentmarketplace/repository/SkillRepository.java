package com.talentmarketplace.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Skill> findByCategory(SkillCategory category);

    @Query("SELECT s FROM Skill s WHERE " +
           "(:category IS NULL OR s.category = :category) AND " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Skill> searchSkills(@Param("category") SkillCategory category,
                            @Param("search") String search);
}
