package com.talentmarketplace.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentmarketplace.entity.DemandSkill;

@Repository
public interface DemandSkillRepository extends JpaRepository<DemandSkill, Long> {

    @Query("SELECT ds FROM DemandSkill ds JOIN FETCH ds.skill WHERE ds.demand.id = :demandId")
    List<DemandSkill> findByDemandIdWithSkill(@Param("demandId") Long demandId);

    List<DemandSkill> findByDemandId(Long demandId);

    Optional<DemandSkill> findByDemandIdAndSkillId(Long demandId, Long skillId);

    boolean existsByDemandIdAndSkillId(Long demandId, Long skillId);

    void deleteByDemandIdAndSkillId(Long demandId, Long skillId);
}
