package com.PetHubAI.PetHubAIBackend.repository;

import com.PetHubAI.PetHubAIBackend.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    // Find active categories
    List<ProductCategory> findByIsActiveTrueOrderBySortOrderAsc();

    // Find root categories (no parent)
    List<ProductCategory> findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();

    // Find subcategories by parent
    List<ProductCategory> findByParentAndIsActiveTrueOrderBySortOrderAsc(ProductCategory parent);

    // Find categories with product count
    @Query("SELECT c FROM ProductCategory c WHERE c.isActive = true AND SIZE(c.products) > 0 ORDER BY c.sortOrder ASC")
    List<ProductCategory> findCategoriesWithProducts();
}
