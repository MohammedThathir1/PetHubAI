package com.PetHubAI.PetHubAIBackend.repository;

import com.PetHubAI.PetHubAIBackend.entity.AdoptionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {

    // Find requests by pet
    List<AdoptionRequest> findByPetIdOrderByCreatedAtDesc(Long petId);

    // Find requests by user
    List<AdoptionRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    // Find requests by status
    Page<AdoptionRequest> findByStatusOrderByCreatedAtDesc(
            AdoptionRequest.RequestStatus status, Pageable pageable);

    // Check if user already has pending request for pet
    Optional<AdoptionRequest> findByPetIdAndRequesterIdAndStatus(
            Long petId, Long requesterId, AdoptionRequest.RequestStatus status);

    // Add this method to AdoptionRequestRepository.java
    @Query("SELECT ar FROM AdoptionRequest ar WHERE ar.pet.postedBy.id = :ownerId ORDER BY ar.createdAt DESC")
    List<AdoptionRequest> findRequestsForOwnerPets(@Param("ownerId") Long ownerId);

    // Count requests for owner's pets
    @Query("SELECT COUNT(ar) FROM AdoptionRequest ar WHERE ar.pet.postedBy.id = :ownerId AND ar.status = :status")
    long countRequestsForOwnerPets(@Param("ownerId") Long ownerId, @Param("status") AdoptionRequest.RequestStatus status);


    // Count pending requests
    long countByStatus(AdoptionRequest.RequestStatus status);

    // Count requests by user
    long countByRequesterId(Long requesterId);

    Page<AdoptionRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // NEW: Count requests by pet and status
    long countByPetIdAndStatus(Long petId, AdoptionRequest.RequestStatus status);

    // NEW: Delete all requests for a pet
    void deleteByPetId(Long petId);
}

