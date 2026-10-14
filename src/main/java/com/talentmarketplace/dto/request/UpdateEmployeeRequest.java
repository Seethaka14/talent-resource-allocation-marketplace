package com.talentmarketplace.dto.request;

import java.time.LocalDate;

import com.talentmarketplace.entity.EmploymentStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateEmployeeRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @NotBlank(message = "Department is required")
    @Size(max = 50, message = "Department cannot exceed 50 characters")
    private String department;

    @NotBlank(message = "Designation is required")
    @Size(max = 50, message = "Designation cannot exceed 50 characters")
    private String designation;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotNull(message = "Employment status is required")
    private EmploymentStatus employmentStatus;

    @Min(value = 0, message = "Max capacity percent cannot be less than 0")
    @Max(value = 100, message = "Max capacity percent cannot exceed 100")
    private Integer maxCapacityPercent = 100;

    public UpdateEmployeeRequest() {
    }

    public UpdateEmployeeRequest(String firstName, String lastName, String email, String department,
                                 String designation, String location, LocalDate joiningDate,
                                 EmploymentStatus employmentStatus, Integer maxCapacityPercent) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.location = location;
        this.joiningDate = joiningDate;
        this.employmentStatus = employmentStatus;
        this.maxCapacityPercent = maxCapacityPercent != null ? maxCapacityPercent : 100;
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
}
