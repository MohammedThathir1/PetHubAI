package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.entity.AdoptionRequest;
import com.PetHubAI.PetHubAIBackend.entity.Pet;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.exception.PetNotFoundException;
import com.PetHubAI.PetHubAIBackend.repository.AdoptionRequestRepository;
import com.PetHubAI.PetHubAIBackend.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PetService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private AdoptionRequestRepository adoptionRequestRepository;

    // Create new pet
    public Pet createPet(Pet pet, User owner) {
        pet.setPostedBy(owner);
        pet.setStatus(Pet.AdoptionStatus.AVAILABLE);
        return petRepository.save(pet);
    }

    // Get all available pets
    public List<Pet> getAllAvailablePets() {
        return petRepository.findAvailablePetsWithImages();
    }

    // Get available pets with pagination
    public Page<Pet> getAvailablePets(Pageable pageable) {
        return petRepository.findByStatusOrderByCreatedAtDesc(Pet.AdoptionStatus.AVAILABLE, pageable);
    }

    // Alternative method name that's more explicit
    public Page<Pet> getAvailablePetsOrderedByDate(Pageable pageable) {
        return petRepository.findByStatusOrderByCreatedAtDesc(Pet.AdoptionStatus.AVAILABLE, pageable);
    }

    // If you want all pets regardless of status
    public Page<Pet> getAllPetsForAdmin(Pageable pageable) {
        return petRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // Find pet by ID
    public Pet findById(Long petId) {
        return petRepository.findByIdWithImages(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found with ID: " + petId));
    }

    // Update pet
    public Pet updatePet(Long petId, Pet petDetails, User user) {
        Pet pet = findById(petId);

        // Check ownership or admin rights
        if (!pet.getPostedBy().getId().equals(user.getId()) &&
                !user.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("You can only update your own pets");
        }

        pet.setName(petDetails.getName());
        pet.setSpecies(petDetails.getSpecies());
        pet.setBreed(petDetails.getBreed());
        pet.setAge(petDetails.getAge());
        pet.setGender(petDetails.getGender());
        pet.setSize(petDetails.getSize());
        pet.setDescription(petDetails.getDescription());
        pet.setHealthStatus(petDetails.getHealthStatus());
        pet.setVaccinationStatus(petDetails.getVaccinationStatus());
        pet.setIsNeutered(petDetails.getIsNeutered());
        pet.setIsHouseTrained(petDetails.getIsHouseTrained());
        pet.setGoodWithKids(petDetails.getGoodWithKids());
        pet.setGoodWithPets(petDetails.getGoodWithPets());
        pet.setAdoptionFee(petDetails.getAdoptionFee());
        pet.setLocationCity(petDetails.getLocationCity());
        pet.setLocationState(petDetails.getLocationState());
        pet.setLocationCountry(petDetails.getLocationCountry());

        return petRepository.save(pet);
    }

    // Delete pet
    public void deletePet(Long petId, User user) {
        Pet pet = findById(petId);

        // Check ownership or admin rights
        if (!pet.getPostedBy().getId().equals(user.getId()) &&
                !user.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("You can only delete your own pets");
        }

        petRepository.delete(pet);
    }

    // Search pets
    public Page<Pet> searchPets(String keyword, Pageable pageable) {
        return petRepository.searchPets(keyword, Pet.AdoptionStatus.AVAILABLE, pageable);
    }

    // Filter pets
    public Page<Pet> filterPets(String species, Integer minAge, Integer maxAge,
                                Pet.Size size, Pet.Gender gender, String city, Pageable pageable) {
        return petRepository.findPetsWithFilters(species, minAge, maxAge, size, gender, city,
                Pet.AdoptionStatus.AVAILABLE, pageable);
    }

    // Get pets by species
    public Page<Pet> getPetsBySpecies(String species, Pageable pageable) {
        return petRepository.findBySpeciesIgnoreCaseAndStatusOrderByCreatedAtDesc(
                species, Pet.AdoptionStatus.AVAILABLE, pageable);
    }

    // Get pets by location
    public Page<Pet> getPetsByLocation(String city, Pageable pageable) {
        return petRepository.findByLocationCityIgnoreCaseAndStatusOrderByCreatedAtDesc(
                city, Pet.AdoptionStatus.AVAILABLE, pageable);
    }

    // Get pets posted by user
    public List<Pet> getPetsByOwner(Long ownerId) {
        return petRepository.findByPostedByIdOrderByCreatedAtDesc(ownerId);
    }

    // Update pet status
    public Pet updatePetStatus(Long petId, Pet.AdoptionStatus status, User user) {
        Pet pet = findById(petId);

        // Check ownership or admin rights
        if (!pet.getPostedBy().getId().equals(user.getId()) &&
                !user.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("You can only update status of your own pets");
        }

        pet.setStatus(status);
        return petRepository.save(pet);
    }

    // Get statistics
    public long countAvailablePets() {
        return petRepository.countByStatus(Pet.AdoptionStatus.AVAILABLE);
    }

    public long countPetsByOwner(Long ownerId) {
        return petRepository.countByPostedById(ownerId);
    }

    // Mark pet as adopted
    public Pet markAsAdopted(Long petId, User adopter, User owner) {
        Pet pet = findById(petId);

        // Check ownership
        if (!pet.getPostedBy().getId().equals(owner.getId())) {
            throw new RuntimeException("You can only mark your own pets as adopted");
        }

        pet.setStatus(Pet.AdoptionStatus.ADOPTED);
        pet.setAdoptedBy(adopter);
        return petRepository.save(pet);
    }

    public void deletePetById(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet not found with ID: " + petId));

        // Check if pet has pending adoption requests
        long pendingRequests = adoptionRequestRepository.countByPetIdAndStatus(
                petId, AdoptionRequest.RequestStatus.PENDING
        );

        if (pendingRequests > 0) {
            throw new IllegalStateException(
                    "Cannot delete pet with pending adoption requests. Please resolve requests first."
            );
        }
        adoptionRequestRepository.deleteByPetId(petId);

        // Delete pet images from Cloudinary (if you want to clean up cloud storage)
        if (pet.getImages() != null && !pet.getImages().isEmpty()) {
            // You can implement cloud cleanup here if needed
            // cloudinaryImageService.deleteAllPetImages(petId);
        }

        // Delete the pet
        petRepository.delete(pet);

        System.out.println("✅ Pet deleted successfully: " + pet.getName() + " (ID: " + petId + ")");
    }


    // NEW: Get pet statistics for admin dashboard
    public PetStatistics getPetStatistics() {
        long totalPets = petRepository.count();
        long availablePets = petRepository.countByStatus(Pet.AdoptionStatus.AVAILABLE);
        long adoptedPets = petRepository.countByStatus(Pet.AdoptionStatus.ADOPTED);
        long pendingPets = petRepository.countByStatus(Pet.AdoptionStatus.PENDING);

        return new PetStatistics(totalPets, availablePets, adoptedPets, pendingPets);
    }
    public static class PetStatistics {
        private long totalPets;
        private long availablePets;
        private long adoptedPets;
        private long pendingPets;

        public PetStatistics(long totalPets, long availablePets, long adoptedPets, long pendingPets) {
            this.totalPets = totalPets;
            this.availablePets = availablePets;
            this.adoptedPets = adoptedPets;
            this.pendingPets = pendingPets;
        }

        // Getters
        public long getTotalPets() { return totalPets; }
        public long getAvailablePets() { return availablePets; }
        public long getAdoptedPets() { return adoptedPets; }
        public long getPendingPets() { return pendingPets; }
    }
}


