package com.talentmarketplace.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmploymentStatus;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);

    boolean existsByEmployeeCodeAndIdNot(String employeeCode, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Employee> findByEmploymentStatus(EmploymentStatus employmentStatus);

    List<Employee> findByDepartmentIgnoreCase(String department);

    List<Employee> findByLocationIgnoreCase(String location);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:department IS NULL OR LOWER(e.department) = LOWER(:department)) AND " +
           "(:location IS NULL OR LOWER(e.location) = LOWER(:location)) AND " +
           "(:status IS NULL OR e.employmentStatus = :status) AND " +
           "(:search IS NULL OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Employee> searchEmployees(
            @Param("department") String department,
            @Param("location") String location,
            @Param("status") EmploymentStatus status,
            @Param("search") String search);
}
