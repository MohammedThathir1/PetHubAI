package com.PetHubAI.PetHubAIBackend.repository;

import com.PetHubAI.PetHubAIBackend.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    // FIX: Add JOIN FETCH for images
    @Query("SELECT DISTINCT p FROM Pet p LEFT JOIN FETCH p.images WHERE p.status = 'AVAILABLE' ORDER BY p.createdAt DESC")
    List<Pet> findAvailablePetsWithImages();

    // Also fix the existing method
    @Query("SELECT DISTINCT p FROM Pet p LEFT JOIN FETCH p.postedBy LEFT JOIN FETCH p.images WHERE p.status = :status ORDER BY p.createdAt DESC")
    List<Pet> findByStatusOrderByCreatedAtDesc(@Param("status") Pet.AdoptionStatus status);

    // For individual pet lookup
    @Query("SELECT p FROM Pet p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.postedBy WHERE p.id = :id")
    Optional<Pet> findByIdWithImages(@Param("id") Long id);


    @Query("SELECT p FROM Pet p LEFT JOIN FETCH p.postedBy LEFT JOIN FETCH p.adoptedBy WHERE p.status = 'AVAILABLE' ORDER BY p.createdAt DESC")
    List<Pet> findAvailablePetsWithUser();

    Page<Pet> findByStatusOrderByCreatedAtDesc(Pet.AdoptionStatus status, Pageable pageable);

    // Find pets by species
    Page<Pet> findBySpeciesIgnoreCaseAndStatusOrderByCreatedAtDesc(
            String species, Pet.AdoptionStatus status, Pageable pageable);

    // Find pets by location
    Page<Pet> findByLocationCityIgnoreCaseAndStatusOrderByCreatedAtDesc(
            String city, Pet.AdoptionStatus status, Pageable pageable);

    // Find pets by posted user
    List<Pet> findByPostedByIdOrderByCreatedAtDesc(Long userId);

    // Search pets by name or breed
    @Query("SELECT p FROM Pet p WHERE " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.breed) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "p.status = :status ORDER BY p.createdAt DESC")
    Page<Pet> searchPets(@Param("keyword") String keyword,
                         @Param("status") Pet.AdoptionStatus status,
                         Pageable pageable);

    // Filter pets by multiple criteria
    @Query("SELECT p FROM Pet p WHERE " +
            "(:species IS NULL OR LOWER(p.species) = LOWER(:species)) AND " +
            "(:minAge IS NULL OR p.age >= :minAge) AND " +
            "(:maxAge IS NULL OR p.age <= :maxAge) AND " +
            "(:size IS NULL OR p.size = :size) AND " +
            "(:gender IS NULL OR p.gender = :gender) AND " +
            "(:city IS NULL OR LOWER(p.locationCity) = LOWER(:city)) AND " +
            "p.status = :status ORDER BY p.createdAt DESC")
    Page<Pet> findPetsWithFilters(
            @Param("species") String species,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("size") Pet.Size size,
            @Param("gender") Pet.Gender gender,
            @Param("city") String city,
            @Param("status") Pet.AdoptionStatus status,
            Pageable pageable);

    // Count pets by status
    long countByStatus(Pet.AdoptionStatus status);

    // Count pets posted by user
    long countByPostedById(Long userId);

    // NEW: Find all pets ordered by creation date (for admin)
    @Query("SELECT DISTINCT p FROM Pet p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.postedBy ORDER BY p.createdAt DESC")
    Page<Pet> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

