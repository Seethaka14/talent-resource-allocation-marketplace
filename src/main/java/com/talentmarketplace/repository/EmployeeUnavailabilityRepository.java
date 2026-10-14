package com.talentmarketplace.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentmarketplace.entity.EmployeeUnavailability;

@Repository
public interface EmployeeUnavailabilityRepository extends JpaRepository<EmployeeUnavailability, Long> {

    List<EmployeeUnavailability> findByEmployeeIdOrderByStartDateAsc(Long employeeId);

    @Query("SELECT u FROM EmployeeUnavailability u WHERE u.employee.id = :employeeId AND " +
           "u.startDate <= :endDate AND u.endDate >= :startDate ORDER BY u.startDate ASC")
    List<EmployeeUnavailability> findOverlappingUnavailabilities(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT u FROM EmployeeUnavailability u WHERE u.employee.id = :employeeId AND " +
           "u.id <> :excludeId AND u.startDate <= :endDate AND u.endDate >= :startDate ORDER BY u.startDate ASC")
    List<EmployeeUnavailability> findOverlappingUnavailabilitiesExcludingId(
            @Param("employeeId") Long employeeId,
            @Param("excludeId") Long excludeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT u FROM EmployeeUnavailability u JOIN FETCH u.employee e WHERE " +
           "u.startDate <= :endDate AND u.endDate >= :startDate ORDER BY u.employee.id ASC, u.startDate ASC")
    List<EmployeeUnavailability> findAllOverlappingUnavailabilities(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
