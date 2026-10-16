package com.talentmarketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) -> {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");
                        String json = String.format(
                                "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required. Your session may have expired. Please log in.\",\"path\":\"%s\"}",
                                java.time.Instant.now(), escapeJson(request.getRequestURI())
                        );
                        response.getWriter().write(json);
                    },
                    request -> request.getRequestURI().startsWith("/api")
                )
                .defaultAccessDeniedHandlerFor(
                    (request, response, accessDeniedException) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");
                        String json = String.format(
                                "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied: insufficient role permissions\",\"path\":\"%s\"}",
                                java.time.Instant.now(), escapeJson(request.getRequestURI())
                        );
                        response.getWriter().write(json);
                    },
                    request -> request.getRequestURI().startsWith("/api")
                )
            )
            .authorizeHttpRequests(auth -> auth
                // Static resources & web login pages
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login",
                    "/login/",
                    "/login/**",
                    "/login.html",
                    "/login-pm.html",
                    "/login-rm.html",
                    "/css/**",
                    "/js/**",
                    "/favicon.ico",
                    "/images/**",
                    "/error"
                ).permitAll()

                // Health check & authentication endpoints
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/auth/**").permitAll()

                // 1. Allocation Approvals & Rejections: RESOURCE_MANAGER ONLY
                .requestMatchers(HttpMethod.POST, "/api/allocations/*/confirm").hasRole("RESOURCE_MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/allocations/*/reject").hasRole("RESOURCE_MANAGER")

                // 2. Allocations Creation, Cancellation, Completion, and Inquiries: Both PM and RM
                .requestMatchers(HttpMethod.POST, "/api/allocations").hasAnyRole("PROJECT_MANAGER", "RESOURCE_MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/allocations/*/cancel").hasAnyRole("PROJECT_MANAGER", "RESOURCE_MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/allocations/*/complete").hasAnyRole("PROJECT_MANAGER", "RESOURCE_MANAGER")
                .requestMatchers("/api/allocations", "/api/allocations/**").hasAnyRole("PROJECT_MANAGER", "RESOURCE_MANAGER")

                // 3. Organization-Wide Analytics: RESOURCE_MANAGER ONLY
                .requestMatchers("/api/analytics", "/api/analytics/**").hasRole("RESOURCE_MANAGER")

                // 4. Project Demands: Both PM and RM
                .requestMatchers("/api/demands", "/api/demands/**").hasAnyRole("PROJECT_MANAGER", "RESOURCE_MANAGER")

                // 5. Candidate Recommendations & Comparison: Both PM and RM
                .requestMatchers("/api/recommendations", "/api/recommendations/**").hasAnyRole("PROJECT_MANAGER", "RESOURCE_MANAGER")

                // 6. Employee & Skill Modifications: RESOURCE_MANAGER ONLY
                .requestMatchers(HttpMethod.POST, "/api/employees", "/api/employees/**").hasRole("RESOURCE_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/employees", "/api/employees/**").hasRole("RESOURCE_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/employees", "/api/employees/**").hasRole("RESOURCE_MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/skills", "/api/skills/**").hasRole("RESOURCE_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/skills", "/api/skills/**").hasRole("RESOURCE_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/skills", "/api/skills/**").hasRole("RESOURCE_MANAGER")

                // 7. Employee & Skill Read Views: Both PM and RM
                .requestMatchers(HttpMethod.GET, "/api/employees", "/api/employees/**").hasAnyRole("PROJECT_MANAGER", "RESOURCE_MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/skills", "/api/skills/**").hasAnyRole("PROJECT_MANAGER", "RESOURCE_MANAGER")

                // 8. AI Assistant Chat: Both PM and RM
                .requestMatchers("/api/assistant", "/api/assistant/**").hasAnyRole("PROJECT_MANAGER", "RESOURCE_MANAGER")

                // Default rule for any remaining /api/** routes: must be authenticated
                .requestMatchers("/api/**").authenticated()

                .anyRequest().permitAll()
            );

        return http.build();
    }
}
