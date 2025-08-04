package com.PetHubAI.PetHubAIBackend.dto.adoption;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdoptionRequestDto {

    @NotNull(message = "Pet ID is required")
    private Long petId;

    @NotBlank(message = "Message is required")
    @Size(min = 50, max = 1000, message = "Message must be between 50 and 1000 characters")
    private String message;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String requesterPhone;

    private String requesterAddress;

    private String housingType;

    private Boolean hasExperience = false;

    private Boolean hasOtherPets = false;

    private Boolean hasChildren = false;

    private Integer yearsOfExperience = 0;

    private String requesterNotes;

    // Constructors
    public AdoptionRequestDto() {}

    // Getters and Setters
    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequesterPhone() {
        return requesterPhone;
    }

    public void setRequesterPhone(String requesterPhone) {
        this.requesterPhone = requesterPhone;
    }

    public String getRequesterAddress() {
        return requesterAddress;
    }

    public void setRequesterAddress(String requesterAddress) {
        this.requesterAddress = requesterAddress;
    }

    public String getHousingType() {
        return housingType;
    }

    public void setHousingType(String housingType) {
        this.housingType = housingType;
    }

    public Boolean getHasExperience() {
        return hasExperience;
    }

    public void setHasExperience(Boolean hasExperience) {
        this.hasExperience = hasExperience;
    }

    public Boolean getHasOtherPets() {
        return hasOtherPets;
    }

    public void setHasOtherPets(Boolean hasOtherPets) {
        this.hasOtherPets = hasOtherPets;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getRequesterNotes() {
        return requesterNotes;
    }

    public void setRequesterNotes(String requesterNotes) {
        this.requesterNotes = requesterNotes;
    }
}
