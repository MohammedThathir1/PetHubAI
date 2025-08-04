package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.entity.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class PaymentService {

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    private RazorpayClient razorpayClient;

    public PaymentService() {
        try {
            // Initialize with demo keys for development
            this.razorpayClient = new RazorpayClient("rzp_test_sample", "sample_secret");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            System.out.println("✅ Razorpay client initialized successfully");
        } catch (RazorpayException e) {
            System.err.println("❌ Failed to initialize Razorpay client: " + e.getMessage());
            throw new RuntimeException("Razorpay initialization failed", e);
        }
    }

    public String getRazorpayKey() {
        return razorpayKeyId;
    }


    public String createRazorpayOrder(Order order) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", order.getFinalAmount().multiply(BigDecimal.valueOf(100))); // Amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", order.getOrderNumber());

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            return razorpayOrder.get("id");
        } catch (RazorpayException e) {
            throw new RuntimeException("Error creating Razorpay order: " + e.getMessage());
        }
    }

    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            return Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
        } catch (RazorpayException e) {
            return false;
        }
    }

    public void initiateRefund(String paymentId, BigDecimal amount) {
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", (amount.multiply(BigDecimal.valueOf(100)))); // Amount in paise

            razorpayClient.payments.refund(paymentId, refundRequest);
        } catch (RazorpayException e) {
            throw new RuntimeException("Error initiating refund: " + e.getMessage());
        }
    }

    public PaymentDetails getPaymentDetails(String paymentId) {
        try {
            com.razorpay.Payment payment = razorpayClient.payments.fetch(paymentId);

            PaymentDetails details = new PaymentDetails();
            details.setPaymentId(payment.get("id"));
            details.setAmount(payment.get("amount"));
            details.setStatus(payment.get("status"));
            details.setMethod(payment.get("method"));
            details.setCreatedAt(payment.get("created_at"));

            return details;
        } catch (RazorpayException e) {
            throw new RuntimeException("Error fetching payment details: " + e.getMessage());
        }
    }

    public static class PaymentDetails {
        private String paymentId;
        private Object amount;
        private String status;
        private String method;
        private Object createdAt;

        // Getters and Setters
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

        public Object getAmount() { return amount; }
        public void setAmount(Object amount) { this.amount = amount; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public Object getCreatedAt() { return createdAt; }
        public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
    }
}
