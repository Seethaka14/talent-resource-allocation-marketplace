package com.talentmarketplace.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Employee code is required")
    @Size(max = 30, message = "Employee code cannot exceed 30 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]{3,30}$",
            message = "Employee code must contain only letters, numbers, hyphens and underscores"
    )
    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Department is required")
    @Column(nullable = false, length = 50)
    private String department;

    @NotBlank(message = "Designation is required")
    @Column(nullable = false, length = 50)
    private String designation;

    @NotBlank(message = "Location is required")
    @Column(nullable = false, length = 100)
    private String location;

    @NotNull(message = "Joining date is required")
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @NotNull(message = "Employment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 20)
    private EmploymentStatus employmentStatus;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "max_capacity_percent", nullable = false)
    private Integer maxCapacityPercent = 100;

    @jakarta.persistence.Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
        syncName();
        if (this.maxCapacityPercent == null) {
            this.maxCapacityPercent = 100;
        }
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
        syncName();
        if (this.maxCapacityPercent == null) {
            this.maxCapacityPercent = 100;
        }
    }

    private void syncName() {
        if (this.firstName != null && this.lastName != null) {
            this.name = (this.firstName.trim() + " " + this.lastName.trim()).trim();
        } else if (this.firstName != null) {
            this.name = this.firstName.trim();
        } else if (this.lastName != null) {
            this.name = this.lastName.trim();
        }
    }

    public Employee() {
    }

    public Employee(String employeeCode,
                    String firstName,
                    String lastName,
                    String email,
                    String department,
                    String designation,
                    String location,
                    LocalDate joiningDate,
                    EmploymentStatus employmentStatus) {

        this.employeeCode = employeeCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.location = location;
        this.joiningDate = joiningDate;
        this.employmentStatus = employmentStatus;
        this.maxCapacityPercent = 100;
        syncName();
    }

    public Employee(String employeeCode,
                    String firstName,
                    String lastName,
                    String email,
                    String department,
                    String designation,
                    String location,
                    LocalDate joiningDate,
                    EmploymentStatus employmentStatus,
                    Integer maxCapacityPercent) {

        this(employeeCode, firstName, lastName, email, department, designation, location, joiningDate, employmentStatus);
        this.maxCapacityPercent = maxCapacityPercent != null ? maxCapacityPercent : 100;
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
        syncName();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        syncName();
    }

    public String getName() {
        if (this.name == null) {
            syncName();
        }
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

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", employeeCode='" + employeeCode + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", designation='" + designation + '\'' +
                ", location='" + location + '\'' +
                ", joiningDate=" + joiningDate +
                ", employmentStatus=" + employmentStatus +
                ", maxCapacityPercent=" + maxCapacityPercent +
                '}';
    }
}