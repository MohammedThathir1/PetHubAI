package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.dto.request.CreateAdminRequest;
import com.PetHubAI.PetHubAIBackend.dto.response.UserResponse;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.exception.UserAlreadyExistsException;
import com.PetHubAI.PetHubAIBackend.exception.UserNotFoundException;
import com.PetHubAI.PetHubAIBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable; // CORRECT import - Spring's Pageable
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Get all admin users with pagination
    public Page<UserResponse> getAllAdmins(Pageable pageable) {
        Page<User> adminsPage = userRepository.findByRole(User.Role.ADMIN, pageable);
        List<UserResponse> responses = adminsPage.getContent().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, adminsPage.getTotalElements());
    }

    // Create new admin user
    public UserResponse createAdmin(CreateAdminRequest request, User createdBy) {
        // Check if user with email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Create new admin user
        User newAdmin = new User();
        newAdmin.setFirstName(request.getFirstName());
        newAdmin.setLastName(request.getLastName());
        newAdmin.setEmail(request.getEmail());
        // FIX: Use setPassword instead of setPasswordHash
        newAdmin.setPassword(passwordEncoder.encode(request.getPassword()));
        newAdmin.setPhone(request.getPhone());
        newAdmin.setRole(User.Role.ADMIN);
        newAdmin.setIsActive(true);
        newAdmin.setIsVerified(true); // Admin-created accounts are auto-verified
        newAdmin.setCreatedAt(LocalDateTime.now());
        newAdmin.setUpdatedAt(LocalDateTime.now());

        User savedAdmin = userRepository.save(newAdmin);

        // Log admin creation
        System.out.println("✅ New admin created by " + createdBy.getEmail() + ": " + savedAdmin.getEmail());

        UserResponse response = new UserResponse(savedAdmin);
        response.setCreatedByAdmin(createdBy.getFirstName() + " " + createdBy.getLastName());

        return response;
    }

    // Update admin user
    public UserResponse updateAdmin(Long adminId, CreateAdminRequest request, User updatedBy) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin user not found with ID: " + adminId));

        // Ensure the user being updated is actually an admin
        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }

        // Check if email is being changed and if new email already exists
        if (!admin.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
            }
            admin.setEmail(request.getEmail());
        }

        // Update fields
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setPhone(request.getPhone());

        // FIX: Only update password if provided - use setPassword
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        admin.setUpdatedAt(LocalDateTime.now());

        User savedAdmin = userRepository.save(admin);

        // Log admin update
        System.out.println("📝 Admin updated by " + updatedBy.getEmail() + ": " + savedAdmin.getEmail());

        return new UserResponse(savedAdmin);
    }

    // Deactivate admin user
    public void deactivateAdmin(Long adminId, User deactivatedBy) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin user not found with ID: " + adminId));

        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }

        // Prevent deactivating the last active admin
        long activeAdminCount = userRepository.countByRoleAndIsActive(User.Role.ADMIN, true);
        if (activeAdminCount <= 1) {
            throw new IllegalStateException("Cannot deactivate the last active admin user");
        }

        admin.setIsActive(false);
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);

        // Log admin deactivation
        System.out.println("🔒 Admin deactivated by " + deactivatedBy.getEmail() + ": " + admin.getEmail());
    }

    // Activate admin user
    public void activateAdmin(Long adminId, User activatedBy) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin user not found with ID: " + adminId));

        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }

        admin.setIsActive(true);
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);

        // Log admin activation
        System.out.println("🔓 Admin activated by " + activatedBy.getEmail() + ": " + admin.getEmail());
    }

    // Get admin statistics
    public AdminStatistics getAdminStatistics() {
        long totalAdmins = userRepository.countByRole(User.Role.ADMIN);
        long activeAdmins = userRepository.countByRoleAndIsActive(User.Role.ADMIN, true);
        long inactiveAdmins = totalAdmins - activeAdmins;

        return new AdminStatistics(totalAdmins, activeAdmins, inactiveAdmins);
    }

    // Statistics DTO class
    public static class AdminStatistics {
        private long totalAdmins;
        private long activeAdmins;
        private long inactiveAdmins;

        public AdminStatistics(long totalAdmins, long activeAdmins, long inactiveAdmins) {
            this.totalAdmins = totalAdmins;
            this.activeAdmins = activeAdmins;
            this.inactiveAdmins = inactiveAdmins;
        }

        // Getters
        public long getTotalAdmins() { return totalAdmins; }
        public long getActiveAdmins() { return activeAdmins; }
        public long getInactiveAdmins() { return inactiveAdmins; }
    }
}
