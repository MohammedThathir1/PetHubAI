package com.PetHubAI.PetHubAIBackend.dto.adoption;

import com.PetHubAI.PetHubAIBackend.entity.AdoptionRequest;
import java.time.LocalDateTime;

public class AdoptionRequestResponse {

    private Long id;
    private Long petId;
    private String petName;
    private String petSpecies;
    private String petBreed;
    private String petImageUrl;
    private Long requesterId;
    private String requesterName;
    private String requesterEmail;
    private String requesterPhone;
    private String requesterAddress;
    private String message;
    private String housingType;
    private Boolean hasExperience;
    private Boolean hasOtherPets;
    private Boolean hasChildren;
    private Integer yearsOfExperience;
    private AdoptionRequest.RequestStatus status;
    private String ownerNotes;
    private String requesterNotes;
    private Boolean contactShared;
    private LocalDateTime contactSharedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;

    // Constructors
    public AdoptionRequestResponse() {}

    public AdoptionRequestResponse(AdoptionRequest request) {
        this.id = request.getId();
        this.petId = request.getPet().getId();
        this.petName = request.getPet().getName();
        this.petSpecies = request.getPet().getSpecies();
        this.petBreed = request.getPet().getBreed();
        this.requesterId = request.getRequester().getId();
        this.requesterName = request.getRequester().getFirstName() + " " + request.getRequester().getLastName();
        this.requesterEmail = request.getRequester().getEmail();
        this.requesterPhone = request.getRequesterPhone();
        this.requesterAddress = request.getRequesterAddress();
        this.message = request.getMessage();
        this.housingType = request.getHousingType();
        this.hasExperience = request.getHasExperience();
        this.hasOtherPets = request.getHasOtherPets();
        this.hasChildren = request.getHasChildren();
        this.yearsOfExperience = request.getYearsOfExperience();
        this.status = request.getStatus();
        this.ownerNotes = request.getOwnerNotes();
        this.requesterNotes = request.getRequesterNotes();
        this.contactShared = request.getContactShared();
        this.contactSharedAt = request.getContactSharedAt();
        this.createdAt = request.getCreatedAt();
        this.updatedAt = request.getUpdatedAt();

        // Owner information
        this.ownerName = request.getPet().getPostedBy().getFirstName() + " " +
                request.getPet().getPostedBy().getLastName();
        this.ownerPhone = request.getPet().getPostedBy().getPhone();
        this.ownerEmail = request.getPet().getPostedBy().getEmail();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getPetSpecies() { return petSpecies; }
    public void setPetSpecies(String petSpecies) { this.petSpecies = petSpecies; }

    public String getPetBreed() { return petBreed; }
    public void setPetBreed(String petBreed) { this.petBreed = petBreed; }

    public String getPetImageUrl() { return petImageUrl; }
    public void setPetImageUrl(String petImageUrl) { this.petImageUrl = petImageUrl; }

    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getRequesterEmail() { return requesterEmail; }
    public void setRequesterEmail(String requesterEmail) { this.requesterEmail = requesterEmail; }

    public String getRequesterPhone() { return requesterPhone; }
    public void setRequesterPhone(String requesterPhone) { this.requesterPhone = requesterPhone; }

    public String getRequesterAddress() { return requesterAddress; }
    public void setRequesterAddress(String requesterAddress) { this.requesterAddress = requesterAddress; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

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

    public AdoptionRequest.RequestStatus getStatus() { return status; }
    public void setStatus(AdoptionRequest.RequestStatus status) { this.status = status; }

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

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}
