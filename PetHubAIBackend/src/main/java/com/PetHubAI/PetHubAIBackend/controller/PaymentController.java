package com.PetHubAI.PetHubAIBackend.controller;

import com.PetHubAI.PetHubAIBackend.dto.response.ApiResponse;
import com.PetHubAI.PetHubAIBackend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Get Razorpay key for frontend
    @GetMapping("/key")
    public ResponseEntity<ApiResponse<String>> getRazorpayKey() {
        try {
            String key = paymentService.getRazorpayKey();
            return ResponseEntity.ok(ApiResponse.success("Razorpay key retrieved successfully", key));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get payment key: " + e.getMessage()));
        }
    }

    // Verify payment signature
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyPayment(
            @RequestBody PaymentVerificationRequest request) {
        try {
            boolean isValid = paymentService.verifyPayment(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );

            if (isValid) {
                return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", true));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Payment verification failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Payment verification error: " + e.getMessage()));
        }
    }

    // Get payment details
    @GetMapping("/details/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentService.PaymentDetails>> getPaymentDetails(
            @PathVariable String paymentId) {
        try {
            PaymentService.PaymentDetails details = paymentService.getPaymentDetails(paymentId);
            return ResponseEntity.ok(ApiResponse.success("Payment details retrieved successfully", details));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get payment details: " + e.getMessage()));
        }
    }

    // Initiate refund
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<String>> initiateRefund(
            @RequestBody RefundRequest request) {
        try {
            paymentService.initiateRefund(request.getPaymentId(), request.getAmount());
            return ResponseEntity.ok(ApiResponse.success("Refund initiated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Refund failed: " + e.getMessage()));
        }
    }

    // DTOs for payment operations
    public static class PaymentVerificationRequest {
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

    public static class RefundRequest {
        private String paymentId;
        private BigDecimal amount;

        // Getters and Setters
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
