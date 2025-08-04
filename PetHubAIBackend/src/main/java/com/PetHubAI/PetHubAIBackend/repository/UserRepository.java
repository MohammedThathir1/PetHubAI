package com.PetHubAI.PetHubAIBackend.repository;



import com.PetHubAI.PetHubAIBackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);

    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'USER'")
    long countUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN'")
    long countAdmins();

    // Add to existing UserRepository.java
    Optional<User> findByEmailAndRole(String email, User.Role role);

    // Add these methods to existing UserRepository.java
    Page<User> findByRole(User.Role role, Pageable pageable);
    long countByRole(User.Role role);
    long countByRoleAndIsActive(User.Role role, Boolean isActive);

}
