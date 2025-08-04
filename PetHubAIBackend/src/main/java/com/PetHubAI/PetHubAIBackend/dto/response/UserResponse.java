// src/main/java/com/PetHubAI/PetHubAIBackend/dto/response/UserResponse.java
package com.PetHubAI.PetHubAIBackend.dto.response;

import com.PetHubAI.PetHubAIBackend.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Profile information (flattened, no circular reference)
    private String bio;
    private String avatarUrl;
    private String city;
    private String state;
    private String country;

    // MISSING FIELD: Admin tracking information
    private String createdByAdmin; // Name of admin who created this user

    public UserResponse() {}

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.phone = user.getPhone();
        this.role = user.getRole() != null ? user.getRole().toString() : null;
        this.isVerified = user.getIsVerified();
        this.isActive = user.getIsActive();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();

        // Safely extract profile information without circular reference
        if (user.getProfile() != null) {
            this.bio = user.getProfile().getBio();
            this.avatarUrl = user.getProfile().getAvatarUrl();
            this.city = user.getProfile().getCity();
            this.state = user.getProfile().getState();
            this.country = user.getProfile().getCountry();
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    // FIX: Add missing getter and setter for createdByAdmin
    public String getCreatedByAdmin() { return createdByAdmin; }
    public void setCreatedByAdmin(String createdByAdmin) { this.createdByAdmin = createdByAdmin; }
}
