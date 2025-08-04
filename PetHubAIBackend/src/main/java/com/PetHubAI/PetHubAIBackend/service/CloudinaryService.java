package com.PetHubAI.PetHubAIBackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// src/main/java/com/PetHubAI/PetHubAIBackend/service/CloudinaryService.java
@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadProductImage(MultipartFile file) throws Exception {
        Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "pet-products",
                        "resource_type", "image",
                        "format", "jpg",
                        "quality", "auto:good"
                )
        );

        return (String) uploadResult.get("secure_url");
    }

    public List<String> uploadMultipleProductImages(List<MultipartFile> files) throws Exception {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            String url = uploadProductImage(file);
            imageUrls.add(url);
        }

        return imageUrls;
    }

    public void deleteImage(String imageUrl) throws Exception {
        // Extract public_id from URL and delete
        String publicId = extractPublicIdFromUrl(imageUrl);
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        // Extract public_id from Cloudinary URL
        // Implementation depends on your URL structure
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
    }
}
