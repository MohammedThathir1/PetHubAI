package com.PetHubAI.PetHubAIBackend.dto.product;

import com.PetHubAI.PetHubAIBackend.entity.Order;
import com.PetHubAI.PetHubAIBackend.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {

    private Long id;
    private String orderNumber;
    private BigDecimal totalAmount;
    // ✅ NEW: Add subtotal field
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal finalAmount;
    private String currency;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String shippingAddress;
    private String billingAddress;
    // ✅ NEW: Add special instructions
    private String specialInstructions;
    // ✅ NEW: Add tracking number
    private String trackingNumber;
    private LocalDate estimatedDelivery;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // ✅ NEW: Add formatted dates for frontend
    private String createdAtFormatted;
    private String estimatedDeliveryFormatted;
    private List<OrderItemResponse> orderItems;

    // ✅ NEW: Customer information
    private String customerName;
    private String customerEmail;

    public OrderResponse() {}

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.totalAmount = order.getTotalAmount();
        // ✅ NEW: Set subtotal
        this.subtotal = order.getSubtotal();
        this.discountAmount = order.getDiscountAmount();
        this.taxAmount = order.getTaxAmount();
        this.shippingCost = order.getShippingCost();
        this.finalAmount = order.getFinalAmount();
        this.currency = order.getCurrency();
        this.status = order.getStatus() != null ? order.getStatus().toString() : null;
        this.paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus().toString() : null;
        this.paymentMethod = order.getPaymentMethod();
        this.razorpayOrderId = order.getRazorpayOrderId();
        this.razorpayPaymentId = order.getRazorpayPaymentId();
        this.shippingAddress = order.getShippingAddress();
        this.billingAddress = order.getBillingAddress();
        // ✅ NEW: Set special instructions and tracking
        this.specialInstructions = order.getSpecialInstructions();
        this.trackingNumber = order.getTrackingNumber();
        this.estimatedDelivery = order.getEstimatedDelivery();
        this.deliveredAt = order.getDeliveredAt();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();

        // ✅ NEW: Format dates for frontend display
        if (order.getCreatedAt() != null) {
            this.createdAtFormatted = order.getCreatedAt().format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        }
        if (order.getEstimatedDelivery() != null) {
            this.estimatedDeliveryFormatted = order.getEstimatedDelivery().format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy"));
        }

        // ✅ NEW: Set customer information
        if (order.getUser() != null) {
            this.customerName = order.getUser().getFirstName() + " " + order.getUser().getLastName();
            this.customerEmail = order.getUser().getEmail();
        }

        if (order.getOrderItems() != null) {
            this.orderItems = order.getOrderItems().stream()
                    .map(OrderItemResponse::new)
                    .collect(Collectors.toList());
        }
    }

    // ✅ ALL GETTERS AND SETTERS (existing + new ones)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    // ✅ NEW: Subtotal getter/setter
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getShippingCost() { return shippingCost; }
    public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

    // ✅ NEW: Special instructions getter/setter
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    // ✅ NEW: Tracking number getter/setter
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public LocalDate getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDate estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ✅ NEW: Formatted dates getters/setters
    public String getCreatedAtFormatted() { return createdAtFormatted; }
    public void setCreatedAtFormatted(String createdAtFormatted) { this.createdAtFormatted = createdAtFormatted; }

    public String getEstimatedDeliveryFormatted() { return estimatedDeliveryFormatted; }
    public void setEstimatedDeliveryFormatted(String estimatedDeliveryFormatted) { this.estimatedDeliveryFormatted = estimatedDeliveryFormatted; }

    // ✅ NEW: Customer information getters/setters
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public List<OrderItemResponse> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemResponse> orderItems) { this.orderItems = orderItems; }
}
