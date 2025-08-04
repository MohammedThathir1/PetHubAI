package com.PetHubAI.PetHubAIBackend.repository;

import com.PetHubAI.PetHubAIBackend.entity.Product;
import com.PetHubAI.PetHubAIBackend.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find active products
    Page<Product> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    // Find featured products
    List<Product> findByIsActiveTrueAndFeaturedTrueOrderByCreatedAtDesc();

    // Find by category
    Page<Product> findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(ProductCategory category, Pageable pageable);

    // Search by name or description
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Find by brand
    Page<Product> findByBrandContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String brand, Pageable pageable);

    // Find products with low stock
    List<Product> findByStockQuantityLessThanEqualAndIsActiveTrue(Integer minStockLevel);

    // Find by price range
    Page<Product> findByPriceBetweenAndIsActiveTrueOrderByCreatedAtDesc(Double minPrice, Double maxPrice, Pageable pageable);

    // Admin - find all products (including inactive)
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Find product with images
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    // Find by SKU
    Optional<Product> findBySku(String sku);

    long countByIsActiveTrue();

    // NEW: Add these missing count methods
    long countByFeaturedTrueAndIsActiveTrue();

    long countByStockQuantityLessThanEqualAndIsActiveTrue(Integer stockLevel);

    long countByStockQuantityAndIsActiveTrue(Integer stockQuantity);

    // Alternative: You can also use @Query for more complex counts
    @Query("SELECT COUNT(p) FROM Product p WHERE p.featured = true AND p.isActive = true")
    long countFeaturedActiveProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= :stockLevel AND p.isActive = true")
    long countLowStockActiveProducts(@Param("stockLevel") Integer stockLevel);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity = :stockQuantity AND p.isActive = true")
    long countByExactStockAndActive(@Param("stockQuantity") Integer stockQuantity);
}
