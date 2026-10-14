package com.talentmarketplace.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentmarketplace.entity.AllocationStatus;
import com.talentmarketplace.entity.ResourceAllocation;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {

    List<ResourceAllocation> findByEmployeeIdOrderByStartDateAsc(Long employeeId);

    List<ResourceAllocation> findByDemandIdOrderByStartDateAsc(Long demandId);

    List<ResourceAllocation> findByStatus(AllocationStatus status);

    @Query("SELECT a FROM ResourceAllocation a " +
           "JOIN FETCH a.employee " +
           "JOIN FETCH a.demand " +
           "WHERE a.id = :id")
    Optional<ResourceAllocation> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT a FROM ResourceAllocation a " +
           "WHERE a.employee.id = :employeeId AND " +
           "a.status = com.talentmarketplace.entity.AllocationStatus.CONFIRMED AND " +
           "a.startDate <= :endDate AND a.endDate >= :startDate " +
           "ORDER BY a.startDate ASC")
    List<ResourceAllocation> findOverlappingConfirmedAllocations(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM ResourceAllocation a " +
           "WHERE a.employee.id = :employeeId AND " +
           "a.id <> :excludeAllocationId AND " +
           "a.status = com.talentmarketplace.entity.AllocationStatus.CONFIRMED AND " +
           "a.startDate <= :endDate AND a.endDate >= :startDate " +
           "ORDER BY a.startDate ASC")
    List<ResourceAllocation> findOverlappingConfirmedAllocationsExcludingId(
            @Param("employeeId") Long employeeId,
            @Param("excludeAllocationId") Long excludeAllocationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(a.allocationPercent), 0) FROM ResourceAllocation a " +
           "WHERE a.demand.id = :demandId AND " +
           "a.status = com.talentmarketplace.entity.AllocationStatus.CONFIRMED")
    Integer sumConfirmedAllocationsForDemand(@Param("demandId") Long demandId);
}
