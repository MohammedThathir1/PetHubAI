package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.adoption.AdoptionRequestDto;
import com.PetHubAI.PetHubAIBackend.dto.adoption.AdoptionRequestResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.service.AdoptionRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adoption-requests")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdoptionRequestController {

    @Autowired
    private AdoptionRequestService adoptionRequestService;

    // Create adoption request
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdoptionRequestResponse>> createAdoptionRequest(
            @Valid @RequestBody AdoptionRequestDto requestDto,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        AdoptionRequestResponse response = adoptionRequestService.createAdoptionRequest(requestDto, user);

        return ResponseEntity.ok(ApiResponse.success("Adoption request submitted successfully", response));
    }

    // Get my adoption requests
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdoptionRequestResponse>>> getMyRequests(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        List<AdoptionRequestResponse> requests = adoptionRequestService.getMyRequests(user.getId());

        return ResponseEntity.ok(ApiResponse.success("Requests retrieved successfully", requests));
    }

    // Get requests for my pets
    @GetMapping("/my-pets-requests")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdoptionRequestResponse>>> getRequestsForMyPets(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        List<AdoptionRequestResponse> requests = adoptionRequestService.getRequestsForMyPets(user.getId());

        return ResponseEntity.ok(ApiResponse.success("Requests retrieved successfully", requests));
    }

    // Get requests for specific pet
    @GetMapping("/pet/{petId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdoptionRequestResponse>>> getRequestsForPet(
            @PathVariable Long petId) {

        List<AdoptionRequestResponse> requests = adoptionRequestService.getRequestsForPet(petId);
        return ResponseEntity.ok(ApiResponse.success("Requests retrieved successfully", requests));
    }

    // Approve adoption request
    @PutMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdoptionRequestResponse>> approveRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String notes,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        AdoptionRequestResponse response = adoptionRequestService.approveRequest(requestId, notes, user);

        return ResponseEntity.ok(ApiResponse.success("Request approved and contact shared", response));
    }

    // Reject adoption request
    @PutMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdoptionRequestResponse>> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String notes,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        AdoptionRequestResponse response = adoptionRequestService.rejectRequest(requestId, notes, user);

        return ResponseEntity.ok(ApiResponse.success("Request rejected", response));
    }

    // Mark as adopted
    @PutMapping("/{requestId}/adopted")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdoptionRequestResponse>> markAsAdopted(
            @PathVariable Long requestId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        AdoptionRequestResponse response = adoptionRequestService.markAsAdopted(requestId, user);

        return ResponseEntity.ok(ApiResponse.success("Pet marked as adopted! 🎉", response));
    }

    // Cancel request (by requester)
    @PutMapping("/{requestId}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> cancelRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        adoptionRequestService.cancelRequest(requestId, user);

        return ResponseEntity.ok(ApiResponse.success("Request cancelled successfully", null));
    }

    // Delete request
    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        adoptionRequestService.deleteRequest(requestId, user);

        return ResponseEntity.ok(ApiResponse.success("Request deleted successfully", null));
    }

    // Get request details
    @GetMapping("/{requestId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdoptionRequestResponse>> getRequestDetails(
            @PathVariable Long requestId) {

        AdoptionRequestResponse response = adoptionRequestService.findById(requestId);
        return ResponseEntity.ok(ApiResponse.success("Request details retrieved", response));
    }
}
