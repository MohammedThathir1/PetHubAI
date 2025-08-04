package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.PetResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.entity.Pet;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.service.PetService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pets")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PetController {

    @Autowired
    private PetService petService;

    // Get all available pets - FIX: Use PetResponse DTO
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getAvailablePets() {
        try {
            List<Pet> pets = petService.getAllAvailablePets();
            List<PetResponse> petResponses = pets.stream()
                    .map(PetResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Available pets retrieved successfully", petResponses));
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch available pets: " + e.getMessage()));
        }
    }

    // Get available pets with pagination
    @GetMapping("/available/paginated")
    public ResponseEntity<ApiResponse<Page<PetResponse>>> getAvailablePetsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Pet> pets = petService.getAvailablePets(pageable);
            Page<PetResponse> petResponses = pets.map(PetResponse::new);

            return ResponseEntity.ok(ApiResponse.success("Available pets retrieved successfully", petResponses));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch paginated pets: " + e.getMessage()));
        }
    }

    // Get pet by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PetResponse>> getPetById(@PathVariable Long id) {
        try {
            Pet pet = petService.findById(id);
            return ResponseEntity.ok(ApiResponse.success("Pet retrieved successfully", new PetResponse(pet)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Pet not found: " + e.getMessage()));
        }
    }

    // Create new pet - FIX: Return PetResponse
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PetResponse>> createPet(
            @Valid @RequestBody Pet pet,
            Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();
            Pet createdPet = petService.createPet(pet, user);

            return ResponseEntity.ok(ApiResponse.success("Pet listed for adoption successfully", new PetResponse(createdPet)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create pet: " + e.getMessage()));
        }
    }

    // Update pet
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PetResponse>> updatePet(
            @PathVariable Long id,
            @Valid @RequestBody Pet petDetails,
            Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();
            Pet updatedPet = petService.updatePet(id, petDetails, user);

            return ResponseEntity.ok(ApiResponse.success("Pet updated successfully", new PetResponse(updatedPet)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update pet: " + e.getMessage()));
        }
    }

    // Delete pet
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deletePet(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();
            petService.deletePet(id, user);

            return ResponseEntity.ok(ApiResponse.success("Pet deleted successfully", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete pet: " + e.getMessage()));
        }
    }

    // Get my pets
    @GetMapping("/my-pets")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getMyPets(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<Pet> pets = petService.getPetsByOwner(user.getId());
            List<PetResponse> petResponses = pets.stream()
                    .map(PetResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Your pets retrieved successfully", petResponses));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch your pets: " + e.getMessage()));
        }
    }
}
