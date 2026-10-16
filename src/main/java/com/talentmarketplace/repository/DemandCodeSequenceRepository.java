package com.talentmarketplace.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentmarketplace.entity.DemandCodeSequence;

import jakarta.persistence.LockModeType;

@Repository
public interface DemandCodeSequenceRepository extends JpaRepository<DemandCodeSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DemandCodeSequence s WHERE s.sequenceName = :name")
    Optional<DemandCodeSequence> findBySequenceNameForUpdate(@Param("name") String name);
}
