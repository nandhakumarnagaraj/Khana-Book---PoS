package com.khanabook.pos.config;

import com.khanabook.pos.model.Role;
import com.khanabook.pos.model.User;
import com.khanabook.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        @Bean
        CommandLineRunner seedUsers() {
                return args -> {
                        // Only seed if database is empty
                        if (userRepository.count() > 0) {
                                log.info("Database already contains users. Skipping seeding.");
                                return;
                        }

                        log.info("Seeding initial users...");

                        // 1. SUPERADMIN
                        createUser("superadmin", "SuperAdmin@123", "superadmin@khanabook.com",
                                        "+919999999999", "Super Administrator", Role.ADMIN);

                        log.info("User seeding completed successfully!");
                };
        }

        private void createUser(String username, String password, String email,
                        String phone, String fullName, Role role) {
                if (userRepository.existsByUsername(username)) {
                        log.info("User {} already exists. Skipping.", username);
                        return;
                }

                User user = User.builder()
                                .username(username)
                                .password(passwordEncoder.encode(password))
                                .email(email)
                                .phoneNumber(phone)
                                .fullName(fullName)
                                .fullName(fullName)
                                .role(role)
                                .active(true)
                                .build();

                userRepository.save(user);
                log.info("Created user: {} with role: {}", username, role);
        }
}
