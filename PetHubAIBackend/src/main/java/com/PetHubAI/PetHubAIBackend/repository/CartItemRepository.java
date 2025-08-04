package com.PetHubAI.PetHubAIBackend.repository;

import com.PetHubAI.PetHubAIBackend.entity.CartItem;
import com.PetHubAI.PetHubAIBackend.entity.Product;
import com.PetHubAI.PetHubAIBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find cart items by user
    List<CartItem> findByUserOrderByAddedAtDesc(User user);

    // Find specific cart item
    Optional<CartItem> findByUserAndProduct(User user, Product product);

    // Count cart items by user
    int countByUser(User user);

    // Calculate total cart value
    @Query("SELECT SUM(c.quantity * p.price) FROM CartItem c JOIN c.product p WHERE c.user = :user")
    Double calculateCartTotal(@Param("user") User user);

    // Delete all cart items for user
    void deleteByUser(User user);

    // Find cart items with product details
    @Query("SELECT c FROM CartItem c JOIN FETCH c.product p LEFT JOIN FETCH p.images WHERE c.user = :user ORDER BY c.addedAt DESC")
    List<CartItem> findByUserWithProductDetails(@Param("user") User user);
}
