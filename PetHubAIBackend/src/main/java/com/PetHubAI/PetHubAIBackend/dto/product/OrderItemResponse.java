package com.PetHubAI.PetHubAIBackend.dto.product;

import com.PetHubAI.PetHubAIBackend.entity.OrderItem;

public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public Double getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(Double itemTotal) {
        this.itemTotal = itemTotal;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    private Integer quantity;
    private Double productPrice;
    private Double itemTotal;
    private String primaryImageUrl;

    public OrderItemResponse(OrderItem orderItem) {
        this.id = orderItem.getId();
        this.productId = orderItem.getProduct() != null ? orderItem.getProduct().getId() : null;
        this.productName = orderItem.getProductName();
        this.quantity = orderItem.getQuantity();
        this.productPrice = orderItem.getProductPrice() != null ? orderItem.getProductPrice().doubleValue() : null;
        this.itemTotal = orderItem.getItemTotal() != null ? orderItem.getItemTotal().doubleValue() : null;
        this.primaryImageUrl = orderItem.getPrimaryImageUrl();
    }
}
