package com.talentmarketplace.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmploymentStatus;

public class EmployeeResponse {

    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String name;
    private String email;
    private String department;
    private String designation;
    private String location;
    private LocalDate joiningDate;
    private EmploymentStatus employmentStatus;
    private Integer maxCapacityPercent;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeResponse() {
    }

    public EmployeeResponse(Long id, String employeeCode, String firstName, String lastName,
                            String name, String email, String department, String designation,
                            String location, LocalDate joiningDate, EmploymentStatus employmentStatus,
                            Integer maxCapacityPercent, Long version, LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.location = location;
        this.joiningDate = joiningDate;
        this.employmentStatus = employmentStatus;
        this.maxCapacityPercent = maxCapacityPercent;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static EmployeeResponse fromEntity(Employee entity) {
        if (entity == null) {
            return null;
        }
        return new EmployeeResponse(
                entity.getId(),
                entity.getEmployeeCode(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getName(),
                entity.getEmail(),
                entity.getDepartment(),
                entity.getDesignation(),
                entity.getLocation(),
                entity.getJoiningDate(),
                entity.getEmploymentStatus(),
                entity.getMaxCapacityPercent(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public Integer getMaxCapacityPercent() {
        return maxCapacityPercent;
    }

    public void setMaxCapacityPercent(Integer maxCapacityPercent) {
        this.maxCapacityPercent = maxCapacityPercent;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
