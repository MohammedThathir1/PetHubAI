package com.PetHubAI.PetHubAIBackend.dto;

import com.PetHubAI.PetHubAIBackend.entity.Pet;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetResponse {

    private Long id;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private String gender;
    private String size;
    private String description;
    private String locationCity;
    private String locationState;
    private String status;
    private Double adoptionFee;
    private String primaryImageUrl;
    private Integer totalImages;
    private String postedByName;
    private String postedByPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public PetResponse() {}

    // Main constructor with full image loading
    public PetResponse(Pet pet) {
        this(pet, true); // ✅ Constructor chaining as first statement
    }

    // Comprehensive constructor with conditional image loading
    public PetResponse(Pet pet, boolean loadImages) {
        // Set basic fields first
        this.id = pet.getId();
        this.name = pet.getName();
        this.species = pet.getSpecies();
        this.breed = pet.getBreed();
        this.age = pet.getAge();
        this.gender = pet.getGender() != null ? pet.getGender().toString() : null;
        this.size = pet.getSize() != null ? pet.getSize().toString() : null;
        this.description = pet.getDescription();
        this.locationCity = pet.getLocationCity();
        this.locationState = pet.getLocationState();
        this.status = pet.getStatus() != null ? pet.getStatus().toString() : null;
        this.adoptionFee = pet.getAdoptionFee();
        this.createdAt = pet.getCreatedAt();
        this.updatedAt = pet.getUpdatedAt();

        // Set poster information safely
        if (pet.getPostedBy() != null) {
            this.postedByName = pet.getPostedBy().getFirstName();
            this.postedByPhone = pet.getPostedBy().getPhone();
        }

        // Handle images based on loadImages flag
        if (loadImages) {
            loadPetImages(pet);
        } else {
            this.primaryImageUrl = null;
            this.totalImages = 0;
        }
    }

    // Helper method to load images with proper error handling
    private void loadPetImages(Pet pet) {
        try {
            if (pet.getImages() != null && !pet.getImages().isEmpty()) {
                System.out.println("🖼️ Pet " + pet.getName() + " has " + pet.getImages().size() + " images");

                // Find primary image first
                this.primaryImageUrl = pet.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                        .findFirst()
                        .map(img -> {
                            System.out.println("✅ Primary image found: " + img.getImageUrl());
                            return img.getImageUrl();
                        })
                        .orElseGet(() -> {
                            // If no primary image, use first image
                            String firstImageUrl = pet.getImages().get(0).getImageUrl();
                            System.out.println("📸 Using first image as primary: " + firstImageUrl);
                            return firstImageUrl;
                        });

                this.totalImages = pet.getImages().size();
            } else {
                System.out.println("❌ Pet " + pet.getName() + " has no images");
                this.primaryImageUrl = null;
                this.totalImages = 0;
            }
        } catch (Exception e) {
            // Handle lazy loading exceptions gracefully
            System.err.println("⚠️ Could not load images for pet " + pet.getName() + ": " + e.getMessage());
            this.primaryImageUrl = null;
            this.totalImages = 0;
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocationCity() { return locationCity; }
    public void setLocationCity(String locationCity) { this.locationCity = locationCity; }

    public String getLocationState() { return locationState; }
    public void setLocationState(String locationState) { this.locationState = locationState; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getAdoptionFee() { return adoptionFee; }
    public void setAdoptionFee(Double adoptionFee) { this.adoptionFee = adoptionFee; }

    public String getPrimaryImageUrl() { return primaryImageUrl; }
    public void setPrimaryImageUrl(String primaryImageUrl) { this.primaryImageUrl = primaryImageUrl; }

    public Integer getTotalImages() { return totalImages; }
    public void setTotalImages(Integer totalImages) { this.totalImages = totalImages; }

    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String postedByName) { this.postedByName = postedByName; }

    public String getPostedByPhone() { return postedByPhone; }
    public void setPostedByPhone(String postedByPhone) { this.postedByPhone = postedByPhone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
