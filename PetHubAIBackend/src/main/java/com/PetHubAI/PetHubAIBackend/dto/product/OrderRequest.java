package com.PetHubAI.PetHubAIBackend.dto.product;

import jakarta.validation.constraints.NotBlank;

public class OrderRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String billingAddress;

    // ✅ NEW: Additional fields for COD orders
    private String specialInstructions;
    private String paymentMethod; // COD, RAZORPAY
    private String deliveryPreference; // STANDARD, EXPRESS, SCHEDULED
    private String preferredDeliveryDate;
    private String couponCode;

    // Constructors
    public OrderRequest() {}

    public OrderRequest(String shippingAddress, String billingAddress) {
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
    }

    // ✅ EXISTING + NEW Getters and Setters
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    // ✅ NEW: Payment method getter/setter
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDeliveryPreference() { return deliveryPreference; }
    public void setDeliveryPreference(String deliveryPreference) { this.deliveryPreference = deliveryPreference; }

    public String getPreferredDeliveryDate() { return preferredDeliveryDate; }
    public void setPreferredDeliveryDate(String preferredDeliveryDate) { this.preferredDeliveryDate = preferredDeliveryDate; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
}
