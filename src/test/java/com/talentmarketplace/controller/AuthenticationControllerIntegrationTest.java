package com.talentmarketplace.controller;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;

import com.talentmarketplace.config.TestDatabaseSafetyGuard;
import com.talentmarketplace.dto.request.LoginRequest;
import com.talentmarketplace.entity.User;
import com.talentmarketplace.entity.UserRole;
import com.talentmarketplace.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        TestDatabaseSafetyGuard.assertSafeTestDatabase(dataSource);

        // Ensure clean test accounts
        userRepository.deleteAll();

        // Seed Project Manager
        User pm = new User("PM Tester", "pm@talentmarketplace.com", passwordEncoder.encode("PM_pass123!"), UserRole.PROJECT_MANAGER);
        pm.setEnabled(true);
        userRepository.save(pm);

        // Seed Resource Manager
        User rm = new User("RM Tester", "rm@talentmarketplace.com", passwordEncoder.encode("RM_pass123!"), UserRole.RESOURCE_MANAGER);
        rm.setEnabled(true);
        userRepository.save(rm);

        // Seed Disabled User
        User disabledUser = new User("Disabled Tester", "disabled@talentmarketplace.com", passwordEncoder.encode("Secret123!"), UserRole.PROJECT_MANAGER);
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);
    }

    @Test
    @DisplayName("Successful Project Manager login creates session and returns sanitized user")
    void login_ProjectManager_Success() throws Exception {
        LoginRequest request = new LoginRequest("pm@talentmarketplace.com", "PM_pass123!", UserRole.PROJECT_MANAGER);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("pm@talentmarketplace.com"))
                .andExpect(jsonPath("$.data.role").value("PROJECT_MANAGER"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();

        // Calling /api/auth/me with session returns current user
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("pm@talentmarketplace.com"));
    }

    @Test
    @DisplayName("Successful Resource Manager login creates session and returns sanitized user")
    void login_ResourceManager_Success() throws Exception {
        LoginRequest request = new LoginRequest("rm@talentmarketplace.com", "RM_pass123!", UserRole.RESOURCE_MANAGER);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("rm@talentmarketplace.com"))
                .andExpect(jsonPath("$.data.role").value("RESOURCE_MANAGER"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("Invalid credentials returns 401 Unauthorized")
    void login_InvalidCredentials_Fails() throws Exception {
        LoginRequest request = new LoginRequest("pm@talentmarketplace.com", "WrongPassword!", UserRole.PROJECT_MANAGER);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Disabled user login returns 401 Unauthorized")
    void login_DisabledUser_Fails() throws Exception {
        LoginRequest request = new LoginRequest("disabled@talentmarketplace.com", "Secret123!", UserRole.PROJECT_MANAGER);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Account is disabled. Please contact your administrator."));
    }

    @Test
    @DisplayName("Login through wrong role-specific route rejects with 403 Forbidden")
    void login_WrongRoleRoute_Fails() throws Exception {
        // PM account attempting RM login portal
        LoginRequest request = new LoginRequest("pm@talentmarketplace.com", "PM_pass123!", UserRole.RESOURCE_MANAGER);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This account is not authorized as a RESOURCE MANAGER. Please sign in using the correct role portal."));
    }

    @Test
    @DisplayName("Unauthenticated request to /api/auth/me returns 401")
    void getCurrentUser_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Session creation and logout invalidates session")
    void sessionCreationAndLogout() throws Exception {
        LoginRequest request = new LoginRequest("pm@talentmarketplace.com", "PM_pass123!", UserRole.PROJECT_MANAGER);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        // Logout
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        // Subsequent call with same session fails
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }
}
