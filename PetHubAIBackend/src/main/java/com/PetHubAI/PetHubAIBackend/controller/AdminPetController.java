package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.PetResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.entity.Pet;
import com.PetHubAI.PetHubAIBackend.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/pets") // Clean, dedicated path for pet management
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminPetController {

    @Autowired
    private PetService petService;

    // Get all pets for admin management
    @GetMapping
    @Transactional(readOnly = true) // Fix lazy loading issue
    public ResponseEntity<ApiResponse<Page<PetResponse>>> getAllPets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Pet> petsPage = petService.getAllPetsForAdmin(pageable);

            // Convert to PetResponse with proper error handling
            Page<PetResponse> petResponses = petsPage.map(pet -> {
                try {
                    return new PetResponse(pet);
                } catch (Exception e) {
                    System.err.println("⚠️ Error creating PetResponse for pet " + pet.getId() + ": " + e.getMessage());
                    // Return basic response without images if there's an issue
                    return new PetResponse(pet, false);
                }
            });

            return ResponseEntity.ok(ApiResponse.success("All pets retrieved successfully", petResponses));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch pets: " + e.getMessage()));
        }
    }

    // Delete pet by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePet(@PathVariable Long id) {
        try {
            petService.deletePetById(id);
            return ResponseEntity.ok(ApiResponse.success("Pet deleted successfully", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete pet: " + e.getMessage()));
        }
    }

    // Get pet statistics for admin dashboard
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<PetService.PetStatistics>> getPetStatistics() {
        try {
            PetService.PetStatistics stats = petService.getPetStatistics();
            return ResponseEntity.ok(ApiResponse.success("Pet statistics retrieved successfully", stats));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch pet statistics: " + e.getMessage()));
        }
    }

    // Get pet by ID for admin
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PetResponse>> getPetById(@PathVariable Long id) {
        try {
            Pet pet = petService.findById(id);
            PetResponse petResponse = new PetResponse(pet);
            return ResponseEntity.ok(ApiResponse.success("Pet retrieved successfully", petResponse));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve pet: " + e.getMessage()));
        }
    }
}
