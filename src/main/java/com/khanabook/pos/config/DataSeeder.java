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

import java.util.Set;

 @Configuration @RequiredArgsConstructor @Slf4j
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
                      "+919999999999", "Super Administrator", Set.of(Role.ROLE_SUPERADMIN));

            // 2. ADMIN
            createUser("admin", "Admin@123", "admin@khanabook.com", 
                      "+919999999998", "Restaurant Admin", Set.of(Role.ROLE_ADMIN));

            // 3. MANAGER
            createUser("manager", "Manager@123", "manager@khanabook.com", 
                      "+919999999997", "Restaurant Manager", Set.of(Role.ROLE_MANAGER));

            // 4. CHEF
            createUser("chef_ravi", "Chef@123", "chef@khanabook.com", 
                      "+919999999996", "Chef Ravi Kumar", Set.of(Role.ROLE_CHEF));

            // 5. WAITER
            createUser("waiter_rahul", "Waiter@123", "waiter@khanabook.com", 
                      "+919999999995", "Rahul Sharma", Set.of(Role.ROLE_WAITER));

            // 6. CASHIER
            createUser("cashier_priya", "Cashier@123", "cashier@khanabook.com", 
                      "+919999999994", "Priya Singh", Set.of(Role.ROLE_CASHIER));

            // Additional users with multiple roles
            createUser("owner", "Owner@123", "owner@khanabook.com", 
                      "+919999999993", "Restaurant Owner", 
                      Set.of(Role.ROLE_ADMIN, Role.ROLE_MANAGER));

            createUser("head_chef", "HeadChef@123", "headchef@khanabook.com", 
                      "+919999999992", "Head Chef Amit", 
                      Set.of(Role.ROLE_CHEF, Role.ROLE_MANAGER));

            log.info("User seeding completed successfully!");
        };
    }

    private void createUser(String username, String password, String email, 
                           String phone, String fullName, Set<Role> roles) {
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
                .active(true)
                .build();

        userRepository.save(user);
        log.info("Created user: {} with roles: {}", username, roles);
    }
}
