package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.product.OrderRequest;
import com.PetHubAI.PetHubAIBackend.dto.product.OrderResponse;
import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    @Autowired
    private OrderService orderService;

    // ✅ NEW: Create COD Order
    @PostMapping("/cod")
    public ResponseEntity<ApiResponse<OrderResponse>> createCODOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();

            System.out.println("🔍 COD Order Request from: " + user.getEmail());
            System.out.println("🔍 Payment Method: COD");

            OrderResponse order = orderService.createCODOrder(request, user);

            return ResponseEntity.ok(ApiResponse.success(
                    "Order placed successfully! You will pay cash on delivery.", order));

        } catch (Exception e) {
            System.err.println("❌ COD Order creation failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to place order: " + e.getMessage()));
        }
    }

    // ✅ EXISTING: Create order (Razorpay)
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderService.CreateOrderRequest request,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            OrderResponse order = orderService.createOrder(request, user);
            return ResponseEntity.ok(ApiResponse.success("Order created successfully", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create order: " + e.getMessage()));
        }
    }

    // Get user orders
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderResponse> orders = orderService.getUserOrders(user, pageable);
            return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch orders: " + e.getMessage()));
        }
    }

    // Get order by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            OrderResponse order = orderService.getOrderById(id, user);
            return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch order: " + e.getMessage()));
        }
    }

    // Confirm payment
    @PostMapping("/confirm-payment")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmPayment(
            @RequestBody PaymentConfirmationRequest request) {
        try {
            OrderResponse order = orderService.confirmPayment(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );
            return ResponseEntity.ok(ApiResponse.success("Payment confirmed successfully", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Payment confirmation failed: " + e.getMessage()));
        }
    }

    // Cancel order
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelOrder(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            orderService.cancelOrder(id, user);
            return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to cancel order: " + e.getMessage()));
        }
    }

    public static class PaymentConfirmationRequest {
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;

        // Getters and Setters
        public String getRazorpayOrderId() { return razorpayOrderId; }
        public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

        public String getRazorpayPaymentId() { return razorpayPaymentId; }
        public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

        public String getRazorpaySignature() { return razorpaySignature; }
        public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }
    }
}
