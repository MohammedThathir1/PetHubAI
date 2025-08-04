package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.entity.Pet;
import com.PetHubAI.PetHubAIBackend.entity.PetImage;
import com.PetHubAI.PetHubAIBackend.repository.PetImageRepository;
import com.PetHubAI.PetHubAIBackend.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class PetImageService {

    @Autowired
    private CloudinaryImageService cloudinaryImageService;

    @Autowired
    private PetImageRepository petImageRepository;

    @Autowired
    private PetRepository petRepository;

    public PetImage uploadPetImage(MultipartFile file, Long petId, Boolean isPrimary) throws IOException {
        // Find pet
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found with ID: " + petId));

        // Upload to Cloudinary
        String imageUrl = cloudinaryImageService.uploadPetImage(file, petId);
        String thumbnailUrl = cloudinaryImageService.uploadPetThumbnail(file, petId);

        // If this is set as primary, make sure no other image is primary
        if (isPrimary != null && isPrimary) {
            List<PetImage> existingImages = petImageRepository.findByPetIdOrderByIsPrimaryDescCreatedAtAsc(petId);
            for (PetImage existingImage : existingImages) {
                if (existingImage.getIsPrimary() != null && existingImage.getIsPrimary()) {
                    existingImage.setIsPrimary(false);
                    petImageRepository.save(existingImage);
                }
            }
        }

        // Create and save pet image record
        PetImage petImage = new PetImage();
        petImage.setPet(pet);
        petImage.setImageUrl(imageUrl);
        petImage.setThumbnailUrl(thumbnailUrl);
        petImage.setImageName(file.getOriginalFilename());
        petImage.setImageSize(file.getSize());
        petImage.setIsPrimary(isPrimary != null ? isPrimary : false);
        petImage.setSource(PetImage.ImageSource.CLOUDINARY);

        return petImageRepository.save(petImage);
    }

    public void deletePetImage(Long imageId) {
        PetImage petImage = petImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Pet image not found with ID: " + imageId));

        // Extract public ID from Cloudinary URL for deletion
        String publicId = extractPublicIdFromUrl(petImage.getImageUrl());
        String thumbPublicId = extractPublicIdFromUrl(petImage.getThumbnailUrl());

        // Delete from Cloudinary
        if (publicId != null && !publicId.isEmpty()) {
            cloudinaryImageService.deletePetImage(publicId);
        }
        if (thumbPublicId != null && !thumbPublicId.isEmpty()) {
            cloudinaryImageService.deletePetImage(thumbPublicId);
        }

        // Delete from database
        petImageRepository.delete(petImage);
    }

    public List<PetImage> getPetImages(Long petId) {
        return petImageRepository.findByPetIdOrderByIsPrimaryDescCreatedAtAsc(petId);
    }

    public PetImage getPrimaryImage(Long petId) {
        return petImageRepository.findByPetIdAndIsPrimaryTrue(petId).orElse(null);
    }

    public PetImage findById(Long imageId) {
        return petImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Pet image not found with ID: " + imageId));
    }

    public PetImage updateImageAsPrimary(Long imageId, Long petId) {
        // Find the image to be set as primary
        PetImage newPrimaryImage = findById(imageId);

        // Verify that this image belongs to the specified pet
        if (!newPrimaryImage.getPet().getId().equals(petId)) {
            throw new RuntimeException("Image does not belong to the specified pet");
        }

        // Set all other images for this pet as non-primary
        List<PetImage> existingImages = petImageRepository.findByPetIdOrderByIsPrimaryDescCreatedAtAsc(petId);
        for (PetImage existingImage : existingImages) {
            if (existingImage.getIsPrimary() != null && existingImage.getIsPrimary()) {
                existingImage.setIsPrimary(false);
                petImageRepository.save(existingImage);
            }
        }

        // Set the new image as primary
        newPrimaryImage.setIsPrimary(true);
        return petImageRepository.save(newPrimaryImage);
    }

    public long countImagesByPet(Long petId) {
        return petImageRepository.findByPetIdOrderByIsPrimaryDescCreatedAtAsc(petId).size();
    }

    public void deleteAllPetImages(Long petId) {
        List<PetImage> petImages = petImageRepository.findByPetIdOrderByIsPrimaryDescCreatedAtAsc(petId);

        for (PetImage petImage : petImages) {
            // Extract public ID from Cloudinary URL for deletion
            String publicId = extractPublicIdFromUrl(petImage.getImageUrl());
            String thumbPublicId = extractPublicIdFromUrl(petImage.getThumbnailUrl());

            // Delete from Cloudinary
            if (publicId != null && !publicId.isEmpty()) {
                cloudinaryImageService.deletePetImage(publicId);
            }
            if (thumbPublicId != null && !thumbPublicId.isEmpty()) {
                cloudinaryImageService.deletePetImage(thumbPublicId);
            }
        }

        // Delete all from database
        petImageRepository.deleteByPetId(petId);
    }

    private String extractPublicIdFromUrl(String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            return null;
        }

        try {
            // Extract public_id from Cloudinary
            // URL format: https://res.cloudinary.com/cloud_name/image/upload/v1234567890/folder/public_id.format
            String[] parts = cloudinaryUrl.split("/");
            if (parts.length > 0) {
                String fileWithFormat = parts[parts.length - 1];
                // Remove file extension if present
                int lastDotIndex = fileWithFormat.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    return fileWithFormat.substring(0, lastDotIndex);
                }
                return fileWithFormat;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error extracting public ID from URL: " + cloudinaryUrl + " - " + e.getMessage());
            return null;
        }
    }

    // Utility method to validate image file
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Check file size (max 10MB for free tier)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IOException("File size exceeds 10MB limit");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Only image files are allowed");
        }

        // Allowed formats
        String[] allowedFormats = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
        boolean isValidFormat = false;
        for (String format : allowedFormats) {
            if (format.equals(contentType)) {
                isValidFormat = true;
                break;
            }
        }

        if (!isValidFormat) {
            throw new IOException("Unsupported image format. Allowed: JPG, PNG, GIF, WebP");
        }
    }
}
