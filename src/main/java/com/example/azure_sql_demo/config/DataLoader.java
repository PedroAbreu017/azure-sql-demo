package com.example.azure_sql_demo.config;

import com.example.azure_sql_demo.model.Role;
import com.example.azure_sql_demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        loadRoles();
    }

    private void loadRoles() {
        if (roleRepository.count() == 0) {
            log.info("Loading default roles...");

            // Create USER role
            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("Default user role");
            userRole.setIsActive(true);
            userRole.setCreatedAt(LocalDateTime.now());
            userRole.setCreatedBy("system");
            
            // Create ADMIN role
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator role");
            adminRole.setIsActive(true);
            adminRole.setCreatedAt(LocalDateTime.now());
            adminRole.setCreatedBy("system");

            roleRepository.save(userRole);
            roleRepository.save(adminRole);

            log.info("Default roles created successfully: USER, ADMIN");
        } else {
            log.info("Roles already exist, skipping initialization");
        }
    }
}