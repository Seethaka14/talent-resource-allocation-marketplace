package com.talentmarketplace.dto.request;

import com.talentmarketplace.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private UserRole requiredRole;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password, UserRole requiredRole) {
        this.email = email;
        this.password = password;
        this.requiredRole = requiredRole;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(UserRole requiredRole) {
        this.requiredRole = requiredRole;
    }
}
