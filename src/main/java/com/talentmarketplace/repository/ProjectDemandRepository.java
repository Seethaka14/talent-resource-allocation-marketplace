package com.talentmarketplace.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.ProjectDemand;

@Repository
public interface ProjectDemandRepository extends JpaRepository<ProjectDemand, Long> {

    Optional<ProjectDemand> findByDemandCode(String demandCode);

    @Query("SELECT DISTINCT d FROM ProjectDemand d LEFT JOIN FETCH d.requiredSkills rs LEFT JOIN FETCH rs.skill WHERE d.id = :id")
    Optional<ProjectDemand> findByIdWithSkills(@Param("id") Long id);

    boolean existsByDemandCode(String demandCode);

    boolean existsByDemandCodeAndIdNot(String demandCode, Long id);

    List<ProjectDemand> findByStatus(DemandStatus status);

    List<ProjectDemand> findByPriority(DemandPriority priority);

    @Query("SELECT DISTINCT d FROM ProjectDemand d LEFT JOIN FETCH d.requiredSkills rs LEFT JOIN FETCH rs.skill WHERE " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:priority IS NULL OR d.priority = :priority) AND " +
           "(:role IS NULL OR LOWER(d.role) LIKE LOWER(CONCAT('%', :role, '%'))) AND " +
           "(:location IS NULL OR LOWER(d.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:fromDate IS NULL OR d.endDate >= :fromDate) AND " +
           "(:toDate IS NULL OR d.startDate <= :toDate) AND " +
           "(:search IS NULL OR LOWER(d.demandCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.projectName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.role) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.department) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY d.startDate ASC")
    List<ProjectDemand> searchDemands(
            @Param("status") DemandStatus status,
            @Param("priority") DemandPriority priority,
            @Param("role") String role,
            @Param("location") String location,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("search") String search);

    @Query("SELECT d FROM ProjectDemand d WHERE d.status = com.talentmarketplace.entity.DemandStatus.OPEN " +
           "ORDER BY d.priority DESC, d.startDate ASC")
    List<ProjectDemand> findOpenDemandsByPriority();
}
