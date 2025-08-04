// src/main/java/com/PetHubAI/PetHubAIBackend/controller/AdminController.java
package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.adoption.AdoptionRequestDto;
import com.PetHubAI.PetHubAIBackend.dto.adoption.AdoptionRequestResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.service.AdminService;
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
@RequestMapping("/admin/adoption-requests") // Keep this for adoption requests only
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Get all adoption requests with pagination
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdoptionRequestResponse>>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AdoptionRequestResponse> requests = adminService.getAllRequests(pageable);
            return ResponseEntity.ok(ApiResponse.success("All adoption requests retrieved successfully", requests));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch adoption requests: " + e.getMessage()));
        }
    }

    // Get adoption request by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdoptionRequestResponse>> getRequestById(@PathVariable Long id) {
        try {
            AdoptionRequestResponse request = adminService.getRequestById(id);
            return ResponseEntity.ok(ApiResponse.success("Adoption request retrieved successfully", request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve adoption request: " + e.getMessage()));
        }
    }

    // Create new adoption request (admin can create requests for testing)
    @PostMapping
    public ResponseEntity<ApiResponse<AdoptionRequestResponse>> createRequest(
            @Valid @RequestBody AdoptionRequestDto dto,
            Authentication authentication) {
        try {
            User admin = (User) authentication.getPrincipal();
            AdoptionRequestResponse created = adminService.createRequest(dto, admin);
            return ResponseEntity.ok(ApiResponse.success("Adoption request created successfully", created));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create adoption request: " + e.getMessage()));
        }
    }

    // Update existing adoption request
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdoptionRequestResponse>> updateRequest(
            @PathVariable Long id,
            @RequestBody AdoptionRequestDto dto,
            Authentication authentication) {
        try {
            User admin = (User) authentication.getPrincipal();
            AdoptionRequestResponse updated = adminService.updateRequest(id, dto, admin);
            return ResponseEntity.ok(ApiResponse.success("Adoption request updated successfully", updated));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update adoption request: " + e.getMessage()));
        }
    }

    // Delete adoption request
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRequest(@PathVariable Long id) {
        try {
            adminService.deleteRequest(id);
            return ResponseEntity.ok(ApiResponse.success("Adoption request deleted successfully", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete adoption request: " + e.getMessage()));
        }
    }

    // Bulk update request statuses
    @PutMapping("/bulk-status")
    public ResponseEntity<ApiResponse<String>> bulkUpdateStatus(
            @RequestParam List<Long> requestIds,
            @RequestParam String status) {
        try {
            adminService.bulkUpdateStatus(requestIds, status);
            return ResponseEntity.ok(ApiResponse.success("Bulk status update completed successfully", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update statuses: " + e.getMessage()));
        }
    }

    // Get adoption statistics for admin dashboard
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AdminService.AdoptionStatistics>> getStatistics() {
        try {
            AdminService.AdoptionStatistics stats = adminService.getAdoptionStatistics();
            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch statistics: " + e.getMessage()));
        }
    }
}
