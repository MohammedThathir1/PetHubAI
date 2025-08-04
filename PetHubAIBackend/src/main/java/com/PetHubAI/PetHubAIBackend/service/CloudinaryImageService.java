package com.PetHubAI.PetHubAIBackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryImageService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadPetImage(MultipartFile file, Long petId) throws IOException {
        System.out.println("🔍 Starting image upload for pet ID: " + petId);
        System.out.println("📄 File info - Name: " + file.getOriginalFilename() + ", Size: " + file.getSize() + " bytes");

        try {
            validateImageFile(file);

            // FIX: Create proper transformation using Transformation class
            Transformation transformation = new Transformation()
                    .width(800)
                    .height(600)
                    .crop("fill")
                    .gravity("auto")
                    .quality("auto:good")
                    .fetchFormat("auto");

            // FIX: Use simplified upload options without nested transformation map
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "petcare/pets/" + petId,
                    "public_id", generatePublicId(petId),
                    "resource_type", "image",
                    "transformation", transformation // Pass the Transformation object directly
            );

            System.out.println("📤 Uploading to Cloudinary with transformation...");

            // Upload to Cloudinary
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            System.out.println("✅ Cloudinary upload successful!");
            System.out.println("🔗 Result: " + result);

            String imageUrl = result.get("secure_url").toString();
            System.out.println("🌐 Final image URL: " + imageUrl);

            return imageUrl;

        } catch (Exception e) {
            System.err.println("❌ Cloudinary upload failed: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage());
        }
    }

    public String uploadPetThumbnail(MultipartFile file, Long petId) throws IOException {
        try {
            validateImageFile(file);

            // FIX: Create separate transformation for thumbnails
            Transformation thumbnailTransformation = new Transformation()
                    .width(300)
                    .height(300)
                    .crop("fill")
                    .gravity("auto")
                    .quality("auto:good")
                    .fetchFormat("auto");

            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "petcare/thumbnails/" + petId,
                    "public_id", "thumb_" + generatePublicId(petId),
                    "resource_type", "image",
                    "transformation", thumbnailTransformation
            );

            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            return result.get("secure_url").toString();

        } catch (Exception e) {
            System.err.println("❌ Thumbnail upload failed: " + e.getMessage());
            throw new IOException("Failed to upload thumbnail: " + e.getMessage());
        }
    }

    public boolean deletePetImage(String publicId) {
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(result.get("result"));
        } catch (Exception e) {
            System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
            return false;
        }
    }

    private void validateImageFile(MultipartFile file) throws IOException {
        System.out.println("🔒 Validating image file...");

        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Check file size (max 10MB for free tier)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IOException("File size exceeds 10MB limit");
        }

        // Check content type
        String contentType = file.getContentType();
        System.out.println("📋 Content type: " + contentType);

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

        System.out.println("✅ File validation passed");
    }

    private String generatePublicId(Long petId) {
        return "pet_" + petId + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    // Add these methods to your existing CloudinaryImageService.java

    public String uploadProductImage(MultipartFile file, Long productId) throws IOException {
        System.out.println("🔍 Starting image upload for product ID: " + productId);
        System.out.println("📄 File info - Name: " + file.getOriginalFilename() + ", Size: " + file.getSize() + " bytes");

        try {
            validateImageFile(file); // Same validation as pets

            // Same transformation logic as pets
            Transformation transformation = new Transformation()
                    .width(800)
                    .height(600)
                    .crop("fill")
                    .gravity("auto")
                    .quality("auto:good")
                    .fetchFormat("auto");

            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "petcare/products/" + productId, // Different folder
                    "public_id", generateProductPublicId(productId), // Different prefix
                    "resource_type", "image",
                    "transformation", transformation
            );

            System.out.println("📤 Uploading product image to Cloudinary...");

            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            String imageUrl = result.get("secure_url").toString();
            System.out.println("✅ Product image uploaded: " + imageUrl);

            return imageUrl;

        } catch (Exception e) {
            System.err.println("❌ Product image upload failed: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload product image: " + e.getMessage());
        }
    }

    public String uploadProductThumbnail(MultipartFile file, Long productId) throws IOException {
        try {
            validateImageFile(file);

            Transformation thumbnailTransformation = new Transformation()
                    .width(300)
                    .height(300)
                    .crop("fill")
                    .gravity("auto")
                    .quality("auto:good")
                    .fetchFormat("auto");

            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "petcare/product-thumbnails/" + productId,
                    "public_id", "thumb_" + generateProductPublicId(productId),
                    "resource_type", "image",
                    "transformation", thumbnailTransformation
            );

            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            return result.get("secure_url").toString();

        } catch (Exception e) {
            System.err.println("❌ Product thumbnail upload failed: " + e.getMessage());
            throw new IOException("Failed to upload product thumbnail: " + e.getMessage());
        }
    }

    public boolean deleteProductImage(String publicId) {
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(result.get("result"));
        } catch (Exception e) {
            System.err.println("Failed to delete product image from Cloudinary: " + e.getMessage());
            return false;
        }
    }

    // Helper method for product public IDs
    private String generateProductPublicId(Long productId) {
        return "product_" + productId + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

}
