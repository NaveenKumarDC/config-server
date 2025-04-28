package com.example.configserver.config;

import com.example.configserver.model.Role;
import com.example.configserver.model.User;
import com.example.configserver.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initialize() {
        log.info("Initializing default users...");
        
        try {
            // Create admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setEmail("admin@example.com");
                adminUser.setRole(Role.ADMIN);
                adminUser.setEnabled(true);
                userRepository.save(adminUser);
                log.info("Admin user created successfully");
            } else {
                log.info("Admin user already exists");
            }
            
            // Create read-only user if not exists
            if (!userRepository.existsByUsername("user")) {
                User readOnlyUser = new User();
                readOnlyUser.setUsername("user");
                readOnlyUser.setPassword(passwordEncoder.encode("user123"));
                readOnlyUser.setEmail("user@example.com");
                readOnlyUser.setRole(Role.READ_ONLY);
                readOnlyUser.setEnabled(true);
                userRepository.save(readOnlyUser);
                log.info("Read-only user created successfully");
            } else {
                log.info("Read-only user already exists");
            }
            
            log.info("User initialization completed");
        } catch (Exception e) {
            log.error("Error initializing users: {}", e.getMessage(), e);
        }
    }
} 