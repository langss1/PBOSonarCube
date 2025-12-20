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
                        System.out.println("FIXED: Set email for PENGELOLA user to pengelola@msu.com");
                    }
                } else if (user.getRole() == com.Habb.InventarisMSU.model.Role.PENGURUS) {
                    if (!"pengurus@msu.com".equals(user.getEmail())) {
                        user.setEmail("pengurus@msu.com");
                        userRepository.save(user);
                        System.out.println("FIXED: Set email for PENGURUS user to pengurus@msu.com");
                    }
                }
            }

            // Standard password update if needed
            updateUserPasswordIfPlainText(userRepository, passwordEncoder, "pengelola@msu.com", "password");
            updateUserPasswordIfPlainText(userRepository, passwordEncoder, "pengurus@msu.com", "password");
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
                System.out.println("UPDATED password for user: " + email);
            } else {
                System.out.println("Password for user " + email + " already seems hashed or different.");
            }
        }
    }
}
