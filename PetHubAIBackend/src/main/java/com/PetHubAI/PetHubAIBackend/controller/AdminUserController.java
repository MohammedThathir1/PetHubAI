package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.request.CreateAdminRequest;
import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.UserResponse;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    // Get all admin users
    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserResponse> admins = adminUserService.getAllAdmins(pageable);
            return ResponseEntity.ok(ApiResponse.success("Admin users retrieved successfully", admins));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch admin users: " + e.getMessage()));
        }
    }

    // Create new admin user (only admins can do this)
    @PostMapping("/create-admin")
    public ResponseEntity<ApiResponse<UserResponse>> createAdmin(
            @Valid @RequestBody CreateAdminRequest request,
            Authentication authentication) {
        try {
            User currentAdmin = (User) authentication.getPrincipal();
            UserResponse newAdmin = adminUserService.createAdmin(request, currentAdmin);
            return ResponseEntity.ok(ApiResponse.success("Admin user created successfully", newAdmin));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create admin user: " + e.getMessage()));
        }
    }

    // Update admin user
    @PutMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody CreateAdminRequest request,
            Authentication authentication) {
        try {
            User currentAdmin = (User) authentication.getPrincipal();
            UserResponse updatedAdmin = adminUserService.updateAdmin(id, request, currentAdmin);
            return ResponseEntity.ok(ApiResponse.success("Admin user updated successfully", updatedAdmin));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update admin user: " + e.getMessage()));
        }
    }

    // Deactivate admin user (cannot delete, only deactivate)
    @PutMapping("/admin/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateAdmin(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User currentAdmin = (User) authentication.getPrincipal();

            // Prevent self-deactivation
            if (currentAdmin.getId().equals(id)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("You cannot deactivate your own admin account"));
            }

            adminUserService.deactivateAdmin(id, currentAdmin);
            return ResponseEntity.ok(ApiResponse.success("Admin user deactivated successfully", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to deactivate admin user: " + e.getMessage()));
        }
    }

    // Reactivate admin user
    @PutMapping("/admin/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateAdmin(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User currentAdmin = (User) authentication.getPrincipal();
            adminUserService.activateAdmin(id, currentAdmin);
            return ResponseEntity.ok(ApiResponse.success("Admin user activated successfully", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to activate admin user: " + e.getMessage()));
        }
    }

    // Get admin statistics
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AdminUserService.AdminStatistics>> getAdminStatistics() {
        try {
            AdminUserService.AdminStatistics stats = adminUserService.getAdminStatistics();
            return ResponseEntity.ok(ApiResponse.success("Admin statistics retrieved successfully", stats));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch admin statistics: " + e.getMessage()));
        }
    }
}
