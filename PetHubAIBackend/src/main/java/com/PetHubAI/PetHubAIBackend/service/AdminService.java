// src/main/java/com/PetHubAI/PetHubAIBackend/service/AdminService.java
package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.dto.adoption.AdoptionRequestDto;
import com.PetHubAI.PetHubAIBackend.dto.adoption.AdoptionRequestResponse;
import com.PetHubAI.PetHubAIBackend.entity.AdoptionRequest;
import com.PetHubAI.PetHubAIBackend.entity.Pet;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.exception.AdoptionRequestException;
import com.PetHubAI.PetHubAIBackend.repository.AdoptionRequestRepository;
import com.PetHubAI.PetHubAIBackend.repository.PetRepository;
import com.PetHubAI.PetHubAIBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    @Autowired
    private AdoptionRequestRepository adoptionRequestRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all adoption requests with pagination
    public Page<AdoptionRequestResponse> getAllRequests(Pageable pageable) {
        Page<AdoptionRequest> requestsPage = adoptionRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<AdoptionRequestResponse> responses = requestsPage.getContent().stream()
                .map(AdoptionRequestResponse::new)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, requestsPage.getTotalElements());
    }

    // Get adoption request by ID
    public AdoptionRequestResponse getRequestById(Long id) {
        AdoptionRequest request = adoptionRequestRepository.findById(id)
                .orElseThrow(() -> new AdoptionRequestException("Adoption request not found with ID: " + id));
        return new AdoptionRequestResponse(request);
    }

    // Create new adoption request (admin functionality)
    public AdoptionRequestResponse createRequest(AdoptionRequestDto dto, User admin) {
        Pet pet = petRepository.findById(dto.getPetId())
                .orElseThrow(() -> new AdoptionRequestException("Pet not found with ID: " + dto.getPetId()));

        if (pet.getStatus() != Pet.AdoptionStatus.AVAILABLE) {
            throw new AdoptionRequestException("Pet is not available for adoption");
        }

        // For admin creation, we'll need a requester - you can modify this logic
        User requester = admin; // Or find by some other criteria

        AdoptionRequest request = new AdoptionRequest(pet, requester, dto.getMessage(), dto.getRequesterPhone());
        request.setRequesterAddress(dto.getRequesterAddress());
        request.setHousingType(dto.getHousingType());
        request.setHasExperience(dto.getHasExperience() != null ? dto.getHasExperience() : false);
        request.setHasOtherPets(dto.getHasOtherPets() != null ? dto.getHasOtherPets() : false);
        request.setHasChildren(dto.getHasChildren() != null ? dto.getHasChildren() : false);
        request.setYearsOfExperience(dto.getYearsOfExperience() != null ? dto.getYearsOfExperience() : 0);
        request.setRequesterNotes(dto.getRequesterNotes());

        AdoptionRequest savedRequest = adoptionRequestRepository.save(request);
        return new AdoptionRequestResponse(savedRequest);
    }

    // Update adoption request
    public AdoptionRequestResponse updateRequest(Long id, AdoptionRequestDto dto, User admin) {
        AdoptionRequest request = adoptionRequestRepository.findById(id)
                .orElseThrow(() -> new AdoptionRequestException("Adoption request not found with ID: " + id));

        // Update pet if provided
        if (dto.getPetId() != null) {
            Pet pet = petRepository.findById(dto.getPetId())
                    .orElseThrow(() -> new AdoptionRequestException("Pet not found with ID: " + dto.getPetId()));
            request.setPet(pet);
        }

        // Update other fields
        if (dto.getMessage() != null) request.setMessage(dto.getMessage());
        if (dto.getRequesterPhone() != null) request.setRequesterPhone(dto.getRequesterPhone());
        if (dto.getRequesterAddress() != null) request.setRequesterAddress(dto.getRequesterAddress());
        if (dto.getHousingType() != null) request.setHousingType(dto.getHousingType());
        if (dto.getHasExperience() != null) request.setHasExperience(dto.getHasExperience());
        if (dto.getHasOtherPets() != null) request.setHasOtherPets(dto.getHasOtherPets());
        if (dto.getHasChildren() != null) request.setHasChildren(dto.getHasChildren());
        if (dto.getYearsOfExperience() != null) request.setYearsOfExperience(dto.getYearsOfExperience());
        if (dto.getRequesterNotes() != null) request.setRequesterNotes(dto.getRequesterNotes());

        AdoptionRequest savedRequest = adoptionRequestRepository.save(request);
        return new AdoptionRequestResponse(savedRequest);
    }

    // Delete adoption request
    public void deleteRequest(Long id) {
        AdoptionRequest request = adoptionRequestRepository.findById(id)
                .orElseThrow(() -> new AdoptionRequestException("Adoption request not found with ID: " + id));
        adoptionRequestRepository.delete(request);
    }

    // Bulk update request statuses
    public void bulkUpdateStatus(List<Long> requestIds, String status) {
        AdoptionRequest.RequestStatus requestStatus;
        try {
            requestStatus = AdoptionRequest.RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AdoptionRequestException("Invalid status: " + status);
        }

        List<AdoptionRequest> requests = adoptionRequestRepository.findAllById(requestIds);
        requests.forEach(request -> {
            request.setStatus(requestStatus);
            request.setUpdatedAt(LocalDateTime.now());
        });

        adoptionRequestRepository.saveAll(requests);
    }

    // Get adoption statistics
    public AdoptionStatistics getAdoptionStatistics() {
        long totalRequests = adoptionRequestRepository.count();
        long pendingRequests = adoptionRequestRepository.countByStatus(AdoptionRequest.RequestStatus.PENDING);
        long approvedRequests = adoptionRequestRepository.countByStatus(AdoptionRequest.RequestStatus.APPROVED);
        long adoptedRequests = adoptionRequestRepository.countByStatus(AdoptionRequest.RequestStatus.ADOPTED);
        long rejectedRequests = adoptionRequestRepository.countByStatus(AdoptionRequest.RequestStatus.REJECTED);

        return new AdoptionStatistics(totalRequests, pendingRequests, approvedRequests, adoptedRequests, rejectedRequests);
    }

    // Statistics DTO class
    public static class AdoptionStatistics {
        private long totalRequests;
        private long pendingRequests;
        private long approvedRequests;
        private long adoptedRequests;
        private long rejectedRequests;

        public AdoptionStatistics(long totalRequests, long pendingRequests, long approvedRequests,
                                  long adoptedRequests, long rejectedRequests) {
            this.totalRequests = totalRequests;
            this.pendingRequests = pendingRequests;
            this.approvedRequests = approvedRequests;
            this.adoptedRequests = adoptedRequests;
            this.rejectedRequests = rejectedRequests;
        }

        // Getters
        public long getTotalRequests() { return totalRequests; }
        public long getPendingRequests() { return pendingRequests; }
        public long getApprovedRequests() { return approvedRequests; }
        public long getAdoptedRequests() { return adoptedRequests; }
        public long getRejectedRequests() { return rejectedRequests; }
    }
}
