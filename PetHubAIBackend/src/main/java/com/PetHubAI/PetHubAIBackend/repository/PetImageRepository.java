package com.PetHubAI.PetHubAIBackend.repository;

import com.PetHubAI.PetHubAIBackend.entity.PetImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetImageRepository extends JpaRepository<PetImage, Long> {

    List<PetImage> findByPetIdOrderByIsPrimaryDescCreatedAtAsc(Long petId);

    Optional<PetImage> findByPetIdAndIsPrimaryTrue(Long petId);

    void deleteByPetId(Long petId);
}

