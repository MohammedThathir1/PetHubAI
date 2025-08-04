package com.PetHubAI.PetHubAIBackend.config;

import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createAdminUser();
    }

    private void createAdminUser() {
        // Check if admin already exists
        if (userRepository.findByEmailAndRole("admin@pethub.com", User.Role.ADMIN).isEmpty()) {
            User admin = new User();
            admin.setFirstName("Super");
            admin.setLastName("Admin");
            admin.setEmail("admin@pethub.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            admin.setIsActive(true);
            admin.setIsVerified(true);

            userRepository.save(admin);
            System.out.println("✅ Default admin user created:");
            System.out.println("📧 Email: admin@pethub.com");
            System.out.println("🔑 Password: admin123");
            System.out.println("⚠️ Please change the password after first login!");
        } else {
            System.out.println("🔍 Admin user already exists");
        }
    }
}

