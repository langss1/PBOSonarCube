package com.Habb.InventarisMSU.config;

import com.Habb.InventarisMSU.model.User;
import com.Habb.InventarisMSU.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class DatabaseSeeder {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class);
    private static final String DEFAULT_PASSWORD = "password";

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // FIX: Check for users with empty emails and fix them based on Role
            java.util.List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                if (user.getRole() == com.Habb.InventarisMSU.model.Role.PENGELOLA) {
                    if (!"pengelola@msu.com".equals(user.getEmail())) {
                        user.setEmail("pengelola@msu.com");
                        userRepository.save(user);
                        logger.info("FIXED: Set email for PENGELOLA user to pengelola@msu.com");
                    }
                } else if (user.getRole() == com.Habb.InventarisMSU.model.Role.PENGURUS) {
                    if (!"pengurus@msu.com".equals(user.getEmail())) {
                        user.setEmail("pengurus@msu.com");
                        userRepository.save(user);
                        logger.info("FIXED: Set email for PENGURUS user to pengurus@msu.com");
                    }
                }
            }

            // Standard password update if needed
            updateUserPasswordIfPlainText(userRepository, passwordEncoder, "pengelola@msu.com", DEFAULT_PASSWORD);
            updateUserPasswordIfPlainText(userRepository, passwordEncoder, "pengurus@msu.com", DEFAULT_PASSWORD);
        };
    }

    private void updateUserPasswordIfPlainText(UserRepository userRepository, PasswordEncoder passwordEncoder,
            String email, String plainPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Check if password stored is exactly the plain text one (meaning it hasn't
            // been hashed yet)
            // Note: In a real scenario, detecting if a string is a BCrypt hash is better,
            // but here we know the specific problem is "password" stored as "password".
            if (!user.getPassword().startsWith("$2a$") && user.getPassword().equals(plainPassword)) {
                String encodedPassword = passwordEncoder.encode(plainPassword);
                user.setPassword(encodedPassword);
                userRepository.save(user);
                logger.info("UPDATED password for user: {}", email);
            } else {
                logger.info("Password for user {} already seems hashed or different.", email);
            }
        }
    }
}
