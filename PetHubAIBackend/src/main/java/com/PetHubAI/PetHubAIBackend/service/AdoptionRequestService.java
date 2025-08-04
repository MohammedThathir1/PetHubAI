package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.dto.adoption.AdoptionRequestDto;
import com.PetHubAI.PetHubAIBackend.dto.adoption.AdoptionRequestResponse;
import com.PetHubAI.PetHubAIBackend.entity.AdoptionRequest;
import com.PetHubAI.PetHubAIBackend.entity.Pet;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.exception.AdoptionRequestException;
import com.PetHubAI.PetHubAIBackend.repository.AdoptionRequestRepository;
import com.PetHubAI.PetHubAIBackend.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdoptionRequestService {

    @Autowired
    private AdoptionRequestRepository adoptionRequestRepository;

    @Autowired
    private PetRepository petRepository;

    public AdoptionRequestResponse createAdoptionRequest(AdoptionRequestDto dto, User requester) {
        // Check if pet exists and is available
        Pet pet = petRepository.findById(dto.getPetId())
                .orElseThrow(() -> new AdoptionRequestException("Pet not found"));

        if (pet.getStatus() != Pet.AdoptionStatus.AVAILABLE) {
            throw new AdoptionRequestException("Pet is not available for adoption");
        }

        // Check if user already has a pending request for this pet
        if (adoptionRequestRepository.findByPetIdAndRequesterIdAndStatus(
                dto.getPetId(), requester.getId(), AdoptionRequest.RequestStatus.PENDING).isPresent()) {
            throw new AdoptionRequestException("You already have a pending request for this pet");
        }

        // Check if user is trying to adopt their own pet
        if (pet.getPostedBy().getId().equals(requester.getId())) {
            throw new AdoptionRequestException("You cannot adopt your own pet");
        }

        // Create adoption request
        AdoptionRequest request = new AdoptionRequest(pet, requester, dto.getMessage(), dto.getRequesterPhone());
        request.setRequesterAddress(dto.getRequesterAddress());
        request.setHousingType(dto.getHousingType());
        request.setHasExperience(dto.getHasExperience());
        request.setHasOtherPets(dto.getHasOtherPets());
        request.setHasChildren(dto.getHasChildren());
        request.setYearsOfExperience(dto.getYearsOfExperience());
        request.setRequesterNotes(dto.getRequesterNotes());

        AdoptionRequest savedRequest = adoptionRequestRepository.save(request);
        return new AdoptionRequestResponse(savedRequest);
    }

    public AdoptionRequestResponse approveRequest(Long requestId, String ownerNotes, User owner) {
        AdoptionRequest request = findRequestById(requestId);

        // Verify ownership
        if (!request.getPet().getPostedBy().getId().equals(owner.getId())) {
            throw new AdoptionRequestException("You can only approve requests for your own pets");
        }

        if (request.getStatus() != AdoptionRequest.RequestStatus.PENDING) {
            throw new AdoptionRequestException("Only pending requests can be approved");
        }

        // Approve request and share contact
        request.setStatus(AdoptionRequest.RequestStatus.APPROVED);
        request.setOwnerNotes(ownerNotes);
        request.setContactShared(true);
        request.setContactSharedAt(LocalDateTime.now());
        request.setReviewedBy(owner);
        request.setReviewedAt(LocalDateTime.now());

        AdoptionRequest savedRequest = adoptionRequestRepository.save(request);
        return new AdoptionRequestResponse(savedRequest);
    }

    public AdoptionRequestResponse rejectRequest(Long requestId, String ownerNotes, User owner) {
        AdoptionRequest request = findRequestById(requestId);

        // Verify ownership
        if (!request.getPet().getPostedBy().getId().equals(owner.getId())) {
            throw new AdoptionRequestException("You can only reject requests for your own pets");
        }

        if (request.getStatus() != AdoptionRequest.RequestStatus.PENDING) {
            throw new AdoptionRequestException("Only pending requests can be rejected");
        }

        request.setStatus(AdoptionRequest.RequestStatus.REJECTED);
        request.setOwnerNotes(ownerNotes);
        request.setReviewedBy(owner);
        request.setReviewedAt(LocalDateTime.now());

        AdoptionRequest savedRequest = adoptionRequestRepository.save(request);
        return new AdoptionRequestResponse(savedRequest);
    }

    public AdoptionRequestResponse markAsAdopted(Long requestId, User owner) {
        AdoptionRequest request = findRequestById(requestId);

        // Verify ownership
        if (!request.getPet().getPostedBy().getId().equals(owner.getId())) {
            throw new AdoptionRequestException("You can only mark your own pet requests as adopted");
        }

        if (request.getStatus() != AdoptionRequest.RequestStatus.APPROVED) {
            throw new AdoptionRequestException("Only approved requests can be marked as adopted");
        }

        // Mark request as adopted
        request.setStatus(AdoptionRequest.RequestStatus.ADOPTED);
        request.setCompletedAt(LocalDateTime.now());

        // Update pet status and set adopter
        Pet pet = request.getPet();
        pet.setStatus(Pet.AdoptionStatus.ADOPTED);
        pet.setAdoptedBy(request.getRequester());
        pet.setAdoptedAt(LocalDateTime.now());
        petRepository.save(pet);

        // Reject all other pending requests for this pet
        List<AdoptionRequest> otherRequests = adoptionRequestRepository.findByPetIdOrderByCreatedAtDesc(pet.getId());
        for (AdoptionRequest otherRequest : otherRequests) {
            if (!otherRequest.getId().equals(requestId) &&
                    otherRequest.getStatus() == AdoptionRequest.RequestStatus.PENDING) {
                otherRequest.setStatus(AdoptionRequest.RequestStatus.REJECTED);
                otherRequest.setOwnerNotes("Pet has been adopted by another applicant");
                otherRequest.setReviewedBy(owner);
                otherRequest.setReviewedAt(LocalDateTime.now());
            }
        }

        AdoptionRequest savedRequest = adoptionRequestRepository.save(request);
        return new AdoptionRequestResponse(savedRequest);
    }

    public void cancelRequest(Long requestId, User requester) {
        AdoptionRequest request = findRequestById(requestId);

        // Verify requester
        if (!request.getRequester().getId().equals(requester.getId())) {
            throw new AdoptionRequestException("You can only cancel your own requests");
        }

        if (request.getStatus() == AdoptionRequest.RequestStatus.ADOPTED) {
            throw new AdoptionRequestException("Cannot cancel an adopted request");
        }

        request.setStatus(AdoptionRequest.RequestStatus.CANCELLED);
        adoptionRequestRepository.save(request);
    }

    public void deleteRequest(Long requestId, User user) {
        AdoptionRequest request = findRequestById(requestId);

        // Allow deletion by requester or pet owner
        boolean canDelete = request.getRequester().getId().equals(user.getId()) ||
                request.getPet().getPostedBy().getId().equals(user.getId());

        if (!canDelete) {
            throw new AdoptionRequestException("You can only delete your own requests or requests for your pets");
        }

        if (request.getStatus() == AdoptionRequest.RequestStatus.ADOPTED) {
            throw new AdoptionRequestException("Cannot delete an adopted request");
        }

        adoptionRequestRepository.delete(request);
    }

    // Get requests by different criteria
    public List<AdoptionRequestResponse> getRequestsForPet(Long petId) {
        return adoptionRequestRepository.findByPetIdOrderByCreatedAtDesc(petId)
                .stream()
                .map(AdoptionRequestResponse::new)
                .collect(Collectors.toList());
    }

    public List<AdoptionRequestResponse> getMyRequests(Long userId) {
        return adoptionRequestRepository.findByRequesterIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(AdoptionRequestResponse::new)
                .collect(Collectors.toList());
    }

    public List<AdoptionRequestResponse> getRequestsForMyPets(Long ownerId) {
        return adoptionRequestRepository.findRequestsForOwnerPets(ownerId)
                .stream()
                .map(AdoptionRequestResponse::new)
                .collect(Collectors.toList());
    }

    public AdoptionRequestResponse findById(Long requestId) {
        AdoptionRequest request = findRequestById(requestId);
        return new AdoptionRequestResponse(request);
    }

    private AdoptionRequest findRequestById(Long requestId) {
        return adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new AdoptionRequestException("Adoption request not found"));
    }
}
