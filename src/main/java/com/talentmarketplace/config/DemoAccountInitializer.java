package com.talentmarketplace.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.talentmarketplace.entity.User;
import com.talentmarketplace.entity.UserRole;
import com.talentmarketplace.repository.UserRepository;

@Component
public class DemoAccountInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoAccountInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo.pm.name:Project Manager}")
    private String pmName;

    @Value("${app.demo.pm.email:${APP_DEMO_PM_EMAIL:pm@talentmarketplace.com}}")
    private String pmEmail;

    @Value("${app.demo.pm.password:${APP_DEMO_PM_PASSWORD:PM_pass123!}}")
    private String pmPassword;

    @Value("${app.demo.rm.name:Resource Manager}")
    private String rmName;

    @Value("${app.demo.rm.email:${APP_DEMO_RM_EMAIL:rm@talentmarketplace.com}}")
    private String rmEmail;

    @Value("${app.demo.rm.password:${APP_DEMO_RM_PASSWORD:RM_pass123!}}")
    private String rmPassword;

    public DemoAccountInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initializeAccount(pmName, pmEmail, pmPassword, UserRole.PROJECT_MANAGER);
        initializeAccount(rmName, rmEmail, rmPassword, UserRole.RESOURCE_MANAGER);
    }

    private void initializeAccount(String name, String rawEmail, String initialPassword, UserRole role) {
        if (rawEmail == null || rawEmail.isBlank()) {
            return;
        }

        String email = rawEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            log.info("Demo account for {} [{}] already exists. Preserving existing record.", role, email);
            return;
        }

        String hashedPassword = passwordEncoder.encode(initialPassword);
        User user = new User(name, email, hashedPassword, role);
        user.setEnabled(true);
        userRepository.save(user);

        log.info("Initialized local demo account for {} [{}] with BCrypt hashed credentials.", role, email);
    }
}
