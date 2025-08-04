package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.product.ProductRequest;
import com.PetHubAI.PetHubAIBackend.dto.product.ProductResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.service.CloudinaryImageService;
import com.PetHubAI.PetHubAIBackend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/products")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CloudinaryImageService cloudinaryImageService;

    // Get all products for admin (including inactive)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductResponse> products = productService.getAllProductsForAdmin(pageable);
            return ResponseEntity.ok(ApiResponse.success("All products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch products: " + e.getMessage()));
        }
    }

    // Create new product
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        System.out.println("=== DEBUG: Product Creation Request ===");
        System.out.println("Name: " + request.getName());
        System.out.println("Price: " + request.getPrice() + " (Type: " + (request.getPrice() != null ? request.getPrice().getClass().getSimpleName() : "null") + ")");
        System.out.println("Discount: " + request.getDiscountPercentage());
        System.out.println("Stock: " + request.getStockQuantity());
        System.out.println("========================================");
        try {
            User admin = (User) authentication.getPrincipal();

            // ✅ Log the incoming request for debugging
            System.out.println("🔍 Received product request: " + request.toString());

            ProductResponse product = productService.createProduct(request, admin);
            return ResponseEntity.ok(ApiResponse.success("Product created successfully", product));
        } catch (Exception e) {
            // ✅ Log the actual error
            System.err.println("❌ Product creation failed: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create product: " + e.getMessage()));
        }
    }

    // Add this method to AdminProductController.java
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        try {
            User admin = (User) authentication.getPrincipal();

            // ✅ Add debug logging
            System.out.println("🔍 Updating product ID: " + id);
            System.out.println("🔍 Update request data: " + request.toString());

            ProductResponse product = productService.updateProduct(id, request, admin);
            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
        } catch (Exception e) {
            // ✅ Enhanced error logging
            System.err.println("❌ Product update failed for ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update product: " + e.getMessage()));
        }
    }



    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductWithImages(
            @PathVariable Long id,
            @RequestPart("product") ProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
            Authentication authentication) {
        try {
            User admin = (User) authentication.getPrincipal();
            ProductResponse product;
            System.out.println("🔍 Updating product ID: " + id);
            System.out.println("🔍 Has images: " + (imageFiles != null && !imageFiles.isEmpty()));
            if (imageFiles != null) {
                System.out.println("🔍 Number of images: " + imageFiles.size());
            }
            if (imageFiles != null && !imageFiles.isEmpty()) {
                product = productService.updateProductWithImages(id, request, imageFiles, admin);
            } else {
                product = productService.updateProduct(id, request, admin);
            }

            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update product: " + e.getMessage()));
        }
    }

    // Get product statistics
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ProductService.ProductStatistics>> getProductStatistics() {
        try {
            ProductService.ProductStatistics stats = productService.getProductStatistics();
            return ResponseEntity.ok(ApiResponse.success("Product statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch product statistics: " + e.getMessage()));
        }
    }


    // Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete product: " + e.getMessage()));
        }
    }

    // Toggle product status
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<String>> toggleProductStatus(@PathVariable Long id) {
        try {
            productService.toggleProductStatus(id);
            return ResponseEntity.ok(ApiResponse.success("Product status updated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update product status: " + e.getMessage()));
        }
    }

    // Update stock
    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<String>> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        try {
            productService.updateStock(id, quantity);
            return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update stock: " + e.getMessage()));
        }
    }

    // Get low stock products
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts() {
        try {
            List<ProductResponse> products = productService.getLowStockProducts();
            return ResponseEntity.ok(ApiResponse.success("Low stock products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch low stock products: " + e.getMessage()));
        }
    }
}
