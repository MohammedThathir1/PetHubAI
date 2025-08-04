package com.PetHubAI.PetHubAIBackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "adoption_requests")
public class AdoptionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @NotBlank(message = "Message is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    // Contact and housing information
    @Column(name = "requester_phone", nullable = false)
    private String requesterPhone;

    @Column(name = "requester_address")
    private String requesterAddress;

    @Column(name = "housing_type")
    private String housingType; // Apartment, House, Farm, etc.

    @Column(name = "has_experience")
    private Boolean hasExperience = false;

    @Column(name = "has_other_pets")
    private Boolean hasOtherPets = false;

    @Column(name = "has_children")
    private Boolean hasChildren = false;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience = 0;

    // Status and workflow
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "owner_notes", columnDefinition = "TEXT")
    private String ownerNotes; // Notes from pet owner

    @Column(name = "requester_notes", columnDefinition = "TEXT")
    private String requesterNotes; // Additional notes from requester

    // Communication tracking
    @Column(name = "contact_shared")
    private Boolean contactShared = false; // Has owner shared contact with requester

    @Column(name = "contact_shared_at")
    private LocalDateTime contactSharedAt;

    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Who reviewed/completed the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    // Constructors
    public AdoptionRequest() {}

    public AdoptionRequest(Pet pet, User requester, String message, String phone) {
        this.pet = pet;
        this.requester = requester;
        this.message = message;
        this.requesterPhone = phone;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enhanced status enum
    public enum RequestStatus {
        PENDING,     // Initial state
        APPROVED,    // Owner approved - contact shared
        REJECTED,    // Owner rejected the request
        ADOPTED,     // Pet was adopted by this requester
        CANCELLED,   // Requester cancelled their request
        WITHDRAWN    // Request withdrawn by requester
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRequesterPhone() { return requesterPhone; }
    public void setRequesterPhone(String requesterPhone) { this.requesterPhone = requesterPhone; }

    public String getRequesterAddress() { return requesterAddress; }
    public void setRequesterAddress(String requesterAddress) { this.requesterAddress = requesterAddress; }

    public String getHousingType() { return housingType; }
    public void setHousingType(String housingType) { this.housingType = housingType; }

    public Boolean getHasExperience() { return hasExperience; }
    public void setHasExperience(Boolean hasExperience) { this.hasExperience = hasExperience; }

    public Boolean getHasOtherPets() { return hasOtherPets; }
    public void setHasOtherPets(Boolean hasOtherPets) { this.hasOtherPets = hasOtherPets; }

    public Boolean getHasChildren() { return hasChildren; }
    public void setHasChildren(Boolean hasChildren) { this.hasChildren = hasChildren; }

    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public String getOwnerNotes() { return ownerNotes; }
    public void setOwnerNotes(String ownerNotes) { this.ownerNotes = ownerNotes; }

    public String getRequesterNotes() { return requesterNotes; }
    public void setRequesterNotes(String requesterNotes) { this.requesterNotes = requesterNotes; }

    public Boolean getContactShared() { return contactShared; }
    public void setContactShared(Boolean contactShared) { this.contactShared = contactShared; }

    public LocalDateTime getContactSharedAt() { return contactSharedAt; }
    public void setContactSharedAt(LocalDateTime contactSharedAt) { this.contactSharedAt = contactSharedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }
}
