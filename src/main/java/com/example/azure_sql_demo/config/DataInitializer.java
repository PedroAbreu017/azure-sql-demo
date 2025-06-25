// DataInitializer.java - COMPONENTE DEDICADO
package com.example.azure_sql_demo.config;

import com.example.azure_sql_demo.model.Role;
import com.example.azure_sql_demo.model.User;
import com.example.azure_sql_demo.repository.RoleRepository;
import com.example.azure_sql_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

//@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        log.info("Checking if default roles exist...");
        
        if (roleRepository.count() == 0) {
            log.info("Creating default roles...");
            
            Role userRole = Role.builder()
                .name("USER")
                .description("Default user role")
                .isActive(true)
                .createdBy("SYSTEM")
                .build();
            
            Role adminRole = Role.builder()
                .name("ADMIN")
                .description("Administrator role")
                .isActive(true)
                .createdBy("SYSTEM")
                .build();
            
            Role moderatorRole = Role.builder()
                .name("MODERATOR")
                .description("Moderator role")
                .isActive(true)
                .createdBy("SYSTEM")
                .build();
            
            roleRepository.save(userRole);
            roleRepository.save(adminRole);
            roleRepository.save(moderatorRole);
            
            log.info("✅ Default roles created successfully!");
        } else {
            log.info("✅ Default roles already exist. Total: {}", roleRepository.count());
        }
    }

    private void initializeAdminUser() {
        log.info("Checking if admin user exists...");
        
        if (!userRepository.existsByUsername("admin")) {
            log.info("Creating default admin user...");
            
            Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
            
            User adminUser = User.builder()
                .username("admin")
                .email("admin@azuresqldemo.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("System")
                .lastName("Administrator")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .roles(Set.of(adminRole))
                .createdBy("SYSTEM")
                .build();
            
            userRepository.save(adminUser);
            log.info("✅ Default admin user created! (username: admin, password: admin123)");
        } else {
            log.info("✅ Admin user already exists.");
        }
    }
}