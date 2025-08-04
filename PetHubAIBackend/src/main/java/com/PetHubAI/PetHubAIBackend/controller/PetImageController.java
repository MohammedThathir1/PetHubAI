package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.entity.PetImage;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.service.PetImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/pets/{petId}/images")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PetImageController {

    @Autowired
    private PetImageService petImageService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PetImage>> uploadPetImage(
            @PathVariable Long petId,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary,
            Authentication authentication) {

        try {
            // Validate file
            if (image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("No image file provided"));
            }

            // Validate file size (10MB max)
            if (image.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Image file too large. Maximum size is 10MB"));
            }

            // Validate content type
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Only image files are allowed"));
            }

            User user = (User) authentication.getPrincipal();

            // Add ownership verification here if needed

            PetImage uploadedImage = petImageService.uploadPetImage(image, petId, isPrimary);

            return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully to Cloudinary", uploadedImage));

        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to upload image: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PetImage>>> getPetImages(@PathVariable Long petId) {
        try {
            List<PetImage> images = petImageService.getPetImages(petId);
            return ResponseEntity.ok(ApiResponse.success("Pet images retrieved successfully", images));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve pet images: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deletePetImage(
            @PathVariable Long petId,
            @PathVariable Long imageId,
            Authentication authentication) {

        try {
            petImageService.deletePetImage(imageId);
            return ResponseEntity.ok(ApiResponse.success("Image deleted successfully from Cloudinary", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete image: " + e.getMessage()));
        }
    }
}
