package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.product.CartItemRequest;
import com.PetHubAI.PetHubAIBackend.dto.product.CartItemResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CartController {

    @Autowired
    private CartService cartService;

    // Get cart items
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCartItems(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<CartItemResponse> cartItems = cartService.getCartItems(user);
            return ResponseEntity.ok(ApiResponse.success("Cart items retrieved successfully", cartItems));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch cart items: " + e.getMessage()));
        }
    }

    // Add item to cart
    @PostMapping
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @Valid @RequestBody CartItemRequest request,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CartItemResponse cartItem = cartService.addToCart(request, user);
            return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cartItem));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to add item to cart: " + e.getMessage()));
        }
    }

    // Update cart item quantity
    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CartItemResponse cartItem = cartService.updateCartItem(cartItemId, quantity, user);
            return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cartItem));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update cart item: " + e.getMessage()));
        }
    }

    // Remove item from cart
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<String>> removeFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            cartService.removeFromCart(cartItemId, user);
            return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to remove item from cart: " + e.getMessage()));
        }
    }

    // Clear cart
    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> clearCart(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            cartService.clearCart(user);
            return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to clear cart: " + e.getMessage()));
        }
    }

    // Get cart summary
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CartService.CartSummary>> getCartSummary(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CartService.CartSummary summary = cartService.getCartSummary(user);
            return ResponseEntity.ok(ApiResponse.success("Cart summary retrieved successfully", summary));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch cart summary: " + e.getMessage()));
        }
    }

    // Get cart item count
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            int count = cartService.getCartItemCount(user);
            return ResponseEntity.ok(ApiResponse.success("Cart item count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch cart item count: " + e.getMessage()));
        }
    }
}
