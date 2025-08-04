// src/main/java/com/PetHubAI/PetHubAIBackend/service/ProductService.java
package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.dto.product.ProductRequest;
import com.PetHubAI.PetHubAIBackend.dto.product.ProductResponse;
import com.PetHubAI.PetHubAIBackend.entity.Product;
import com.PetHubAI.PetHubAIBackend.entity.ProductCategory;
import com.PetHubAI.PetHubAIBackend.entity.ProductImage;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.exception.ProductNotFoundException;
import com.PetHubAI.PetHubAIBackend.repository.ProductCategoryRepository;
import com.PetHubAI.PetHubAIBackend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private CloudinaryImageService cloudinaryImageService; // Add Cloudinary integration

    // Public methods - for customers
    public Page<ProductResponse> getActiveProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        return products.map(ProductResponse::new);
    }

    public Page<ProductResponse> getActiveProductsSorted(int page, int size, String sortBy) {
        Sort sort = createSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        return products.map(ProductResponse::new);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        if (!product.getIsActive()) {
            throw new ProductNotFoundException("Product is not available");
        }

        return new ProductResponse(product);
    }

    public List<ProductResponse> getFeaturedProducts() {
        List<Product> products = productRepository.findByIsActiveTrueAndFeaturedTrueOrderByCreatedAtDesc();
        return products.stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }

    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        Page<Product> products = productRepository.searchByKeyword(keyword, pageable);
        return products.map(ProductResponse::new);
    }

    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Page<Product> products = productRepository.findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(category, pageable);
        return products.map(ProductResponse::new);
    }

    public Page<ProductResponse> getProductsByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByPriceBetweenAndIsActiveTrueOrderByCreatedAtDesc(minPrice, maxPrice, pageable);
        return products.map(ProductResponse::new);
    }

    public Page<ProductResponse> getProductsByBrand(String brand, Pageable pageable) {
        Page<Product> products = productRepository.findByBrandContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(brand, pageable);
        return products.map(ProductResponse::new);
    }

    // Admin methods
    public Page<ProductResponse> getAllProductsForAdmin(Pageable pageable) {
        Page<Product> products = productRepository.findAllByOrderByCreatedAtDesc(pageable);
        return products.map(ProductResponse::new);
    }

    public ProductResponse createProduct(ProductRequest request, User createdBy) {
        Product product = new Product();
        mapRequestToProduct(request, product, createdBy);

        // Generate SKU if not provided
        if (request.getSku() == null || request.getSku().trim().isEmpty()) {
            product.setSku(generateSku(product.getName()));
        }

        Product savedProduct = productRepository.save(product);

        // Handle image URLs if provided (for manual URL input)
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            addImageUrlsToProduct(savedProduct, request.getImageUrls());
        }

        return new ProductResponse(savedProduct);
    }

    // NEW: Create product with file uploads using Cloudinary
    public ProductResponse createProductWithImages(ProductRequest request, List<MultipartFile> imageFiles, User createdBy) {
        Product product = new Product();
        mapRequestToProduct(request, product, createdBy);

        // Generate SKU if not provided
        if (request.getSku() == null || request.getSku().trim().isEmpty()) {
            product.setSku(generateSku(product.getName()));
        }

        Product savedProduct = productRepository.save(product);

        // Upload images to Cloudinary
        if (imageFiles != null && !imageFiles.isEmpty()) {
            uploadProductImages(savedProduct, imageFiles);
        }

        return new ProductResponse(savedProduct);
    }

    // Complete fix for ProductService.java
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, User updatedBy) {
        try {
            Product product = productRepository.findByIdWithImages(id)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

            // Update basic fields (price, name, description, etc.)
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setBrand(request.getBrand());
            product.setSku(request.getSku());
            product.setPrice(request.getPrice());
            product.setDiscountPercentage(request.getDiscountPercentage());
            product.setWeight(request.getWeight());
            product.setStockQuantity(request.getStockQuantity());
            product.setMinStockLevel(request.getMinStockLevel());
            product.setDimensions(request.getDimensions());
            product.setTags(request.getTags());
            product.setIsActive(request.getIsActive());
            product.setFeatured(request.getFeatured());

            if (request.getCategoryId() != null) {
                ProductCategory category = categoryRepository.findById(request.getCategoryId()).orElse(null);
                product.setCategory(category);
            }

            // ✅ Handle image updates safely
            if (request.getImageUrls() != null) {
                updateProductImages(product, request.getImageUrls());
            }

            Product savedProduct = productRepository.save(product);
            return new ProductResponse(savedProduct);

        } catch (Exception e) {
            System.err.println("❌ Update failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update product: " + e.getMessage());
        }
    }

    public void updateProductImages(Product product, List<String> newImageUrls) {
        List<ProductImage> existingImages = product.getImages();

        // Remove images not in the new list
        existingImages.removeIf(image -> !newImageUrls.contains(image.getImageUrl()));

        // Add new images
        for (int i = 0; i < newImageUrls.size(); i++) {
            String imageUrl = newImageUrls.get(i);

            boolean exists = existingImages.stream()
                    .anyMatch(img -> img.getImageUrl().equals(imageUrl));

            if (!exists) {
                ProductImage newImage = new ProductImage();
                newImage.setProduct(product);
                newImage.setImageUrl(imageUrl);
                newImage.setIsPrimary(i == 0 && existingImages.isEmpty());
                newImage.setSortOrder(i);
                newImage.setAltText(product.getName() + " - Image " + (i + 1));

                existingImages.add(newImage);
            }
        }

        // Update sort orders
        for (int i = 0; i < existingImages.size(); i++) {
            existingImages.get(i).setSortOrder(i);
            existingImages.get(i).setIsPrimary(i == 0);
        }
    }


    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        // Delete images from Cloudinary
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            deleteProductImages(product);
        }

        productRepository.delete(product);
    }

    public void toggleProductStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        product.setIsActive(!product.getIsActive());
        productRepository.save(product);
    }

    public void updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        product.setStockQuantity(quantity);
        productRepository.save(product);
    }

    public List<ProductResponse> getLowStockProducts() {
        List<Product> products = productRepository.findByStockQuantityLessThanEqualAndIsActiveTrue(5);
        return products.stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }

    public ProductStatistics getProductStatistics() {
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByIsActiveTrue();
        long featuredProducts = productRepository.countByFeaturedTrueAndIsActiveTrue();
        long lowStockProducts = productRepository.countByStockQuantityLessThanEqualAndIsActiveTrue(5);
        long outOfStockProducts = productRepository.countByStockQuantityAndIsActiveTrue(0);

        return new ProductStatistics(totalProducts, activeProducts, featuredProducts, lowStockProducts, outOfStockProducts);
    }

    // Cloudinary image upload methods
    private void uploadProductImages(Product product, List<MultipartFile> imageFiles) {
        List<ProductImage> productImages = new ArrayList<>();

        for (int i = 0; i < imageFiles.size(); i++) {
            try {
                String imageUrl = cloudinaryImageService.uploadProductImage(imageFiles.get(i), product.getId());

                ProductImage productImage = new ProductImage();
                productImage.setProduct(product);
                productImage.setImageUrl(imageUrl);
                productImage.setIsPrimary(i == 0); // First image is primary
                productImage.setSortOrder(i);
                productImage.setAltText(product.getName() + " - Image " + (i + 1));

                productImages.add(productImage);

                System.out.println("✅ Uploaded product image " + (i + 1) + ": " + imageUrl);
            } catch (IOException e) {
                System.err.println("❌ Failed to upload product image " + (i + 1) + ": " + e.getMessage());
            }
        }

        if (!productImages.isEmpty()) {
            product.setImages(productImages);
            productRepository.save(product);
            System.out.println("✅ Saved " + productImages.size() + " images for product: " + product.getName());
        }
    }

    private void deleteProductImages(Product product) {
        for (ProductImage image : product.getImages()) {
            try {
                // Extract public_id from Cloudinary URL for deletion
                String publicId = extractPublicIdFromUrl(image.getImageUrl());
                cloudinaryImageService.deleteProductImage(publicId);
                System.out.println("✅ Deleted product image: " + image.getImageUrl());
            } catch (Exception e) {
                System.err.println("❌ Failed to delete product image: " + e.getMessage());
            }
        }
    }

    // Helper methods
    private void mapRequestToProduct(ProductRequest request, Product product, User user) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setDiscountPercentage(request.getDiscountPercentage());
        product.setStockQuantity(request.getStockQuantity());
        product.setMinStockLevel(request.getMinStockLevel());
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());
        product.setTags(request.getTags());
        product.setIsActive(request.getIsActive());
        product.setFeatured(request.getFeatured());
        product.setCreatedBy(user);

        if (request.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElse(null);
            product.setCategory(category);
        }
    }

    private void addImageUrlsToProduct(Product product, List<String> imageUrls) {
        List<ProductImage> productImages = new ArrayList<>();

        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(imageUrls.get(i));
            image.setIsPrimary(i == 0); // First image is primary
            image.setSortOrder(i);
            image.setAltText(product.getName() + " - Image " + (i + 1));
            productImages.add(image);
        }

        product.setImages(productImages);
        productRepository.save(product);
    }

    private String generateSku(String productName) {
        String baseSku = productName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "")
                .substring(0, Math.min(6, productName.length()));

        long timestamp = System.currentTimeMillis() % 10000;
        return baseSku + timestamp;
    }

    private Sort createSort(String sortBy) {
        switch (sortBy) {
            case "price-low":
                return Sort.by(Sort.Direction.ASC, "price");
            case "price-high":
                return Sort.by(Sort.Direction.DESC, "price");
            case "name":
                return Sort.by(Sort.Direction.ASC, "name");
            case "popular":
                return Sort.by(Sort.Direction.DESC, "featured").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "newest":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        // Extract public_id from Cloudinary URL
        // Example: https://res.cloudinary.com/cloud/image/upload/v1234/petcare/products/1/product_1_abc123.jpg
        try {
            String[] parts = imageUrl.split("/");
            String fileName = parts[parts.length - 1];
            return fileName.substring(0, fileName.lastIndexOf("."));
        } catch (Exception e) {
            System.err.println("Failed to extract public_id from URL: " + imageUrl);
            return "";
        }
    }

    public ProductResponse updateProductWithImages(Long id, ProductRequest request, List<MultipartFile> imageFiles, User updatedBy) {
        System.out.println("🔍 Updating product with images for ID: " + id);

        try {
            Product product = productRepository.findByIdWithImages(id)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

            // Update basic fields using existing helper method
            mapRequestToProduct(request, product, updatedBy);

            // Handle image file uploads
            if (imageFiles != null && !imageFiles.isEmpty()) {
                // Clear existing images safely
                updateProductImages(product, new ArrayList<>()); // Clear images first

                // Upload new images
                uploadProductImages(product, imageFiles);
            }
            Product savedProduct = productRepository.save(product);
            return new ProductResponse(savedProduct);

        } catch (Exception e) {
            System.err.println("❌ Failed to update product with images: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    // Statistics DTO class
    public static class ProductStatistics {
        private long totalProducts;
        private long activeProducts;
        private long featuredProducts;
        private long lowStockProducts;
        private long outOfStockProducts;

        public ProductStatistics(long totalProducts, long activeProducts, long featuredProducts,
                                 long lowStockProducts, long outOfStockProducts) {
            this.totalProducts = totalProducts;
            this.activeProducts = activeProducts;
            this.featuredProducts = featuredProducts;
            this.lowStockProducts = lowStockProducts;
            this.outOfStockProducts = outOfStockProducts;
        }

        // Getters
        public long getTotalProducts() { return totalProducts; }
        public long getActiveProducts() { return activeProducts; }
        public long getFeaturedProducts() { return featuredProducts; }
        public long getLowStockProducts() { return lowStockProducts; }
        public long getOutOfStockProducts() { return outOfStockProducts; }
    }
}
