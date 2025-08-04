package com.PetHubAI.PetHubAIBackend.dto.product;

import com.PetHubAI.PetHubAIBackend.entity.Product;
import com.PetHubAI.PetHubAIBackend.entity.ProductImage;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private Long categoryId;
    private String brand;
    private String sku;
    private BigDecimal price;
    private BigDecimal discountPercentage;
    private BigDecimal finalPrice;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private BigDecimal weight;
    private String dimensions;
    private List<String> tags;
    private Boolean isActive;
    private Boolean featured;
    private String primaryImageUrl;
    private List<ProductImageResponse> images;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean inStock;

    public ProductResponse() {}

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        this.categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        this.brand = product.getBrand();
        this.sku = product.getSku();
        this.price = product.getPrice();
        this.discountPercentage = product.getDiscountPercentage();
        this.finalPrice = calculateFinalPrice(product.getPrice(), product.getDiscountPercentage());
        this.stockQuantity = product.getStockQuantity();
        this.minStockLevel = product.getMinStockLevel();
        this.weight = product.getWeight();
        this.dimensions = product.getDimensions();
        this.tags = product.getTags();
        this.isActive = product.getIsActive();
        this.featured = product.getFeatured();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
        this.inStock = product.getStockQuantity() != null && product.getStockQuantity() > 0;
        this.createdByName = product.getCreatedBy() != null ?
                product.getCreatedBy().getFirstName() + " " + product.getCreatedBy().getLastName() : null;

        try {
            if (product.getTags() != null && Hibernate.isInitialized(product.getTags())) {
                this.tags = new ArrayList<>(product.getTags());
            } else {
                this.tags = new ArrayList<>();
            }
        } catch (LazyInitializationException e) {
            System.err.println("⚠️ Lazy loading issue with product tags: " + e.getMessage());
            this.tags = new ArrayList<>();
        }

        try {
            if (product.getImages() != null && Hibernate.isInitialized(product.getImages())) {
                this.images = product.getImages().stream()
                        .map(ProductImageResponse::new)
                        .collect(Collectors.toList());
                this.primaryImageUrl = product.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                        .findFirst()
                        .map(ProductImage::getImageUrl)
                        .orElseGet(() -> !product.getImages().isEmpty() ?
                                product.getImages().get(0).getImageUrl() : null);
            } else {
                this.images = new ArrayList<>();
                this.primaryImageUrl = null;
            }
        } catch (LazyInitializationException e) {
            System.err.println("⚠️ Lazy loading issue with product images: " + e.getMessage());
            this.images = new ArrayList<>();
            this.primaryImageUrl = null;
        }
    }

    private BigDecimal calculateFinalPrice(BigDecimal price, BigDecimal discountPercentage) {
        if (price == null || discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }

        BigDecimal discountAmount = price.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return price.subtract(discountAmount);
    }


    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Integer getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(Integer minStockLevel) { this.minStockLevel = minStockLevel; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }

    public String getPrimaryImageUrl() { return primaryImageUrl; }
    public void setPrimaryImageUrl(String primaryImageUrl) { this.primaryImageUrl = primaryImageUrl; }

    public List<ProductImageResponse> getImages() { return images; }
    public void setImages(List<ProductImageResponse> images) { this.images = images; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }

    public static class ProductImageResponse {
        private Long id;
        private String imageUrl;
        private String altText;
        private Boolean isPrimary;
        private Integer sortOrder;

        public ProductImageResponse() {}

        public ProductImageResponse(ProductImage image) {
            this.id = image.getId();
            this.imageUrl = image.getImageUrl();
            this.altText = image.getAltText();
            this.isPrimary = image.getIsPrimary();
            this.sortOrder = image.getSortOrder();
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public String getAltText() { return altText; }
        public void setAltText(String altText) { this.altText = altText; }

        public Boolean getIsPrimary() { return isPrimary; }
        public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}
