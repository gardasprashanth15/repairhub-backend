
package com.repairhub.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.repairhub.backend.entity.Admin;
import com.repairhub.backend.repository.AdminRepository;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner createInitialAdmin(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            boolean usernameExists =
                    adminRepository.findByUsername("admin").isPresent();

            boolean emailExists =
                    adminRepository.findByEmail("admin@repairhub.local").isPresent();

            if (!usernameExists && !emailExists) {
                Admin admin = new Admin(
                        "admin",
                        "admin@repairhub.local",
                        passwordEncoder.encode("ChangeMe123!")
                );

                adminRepository.save(admin);
                System.out.println("Initial Repair Hub admin account created.");
            }
        };
    }
}
