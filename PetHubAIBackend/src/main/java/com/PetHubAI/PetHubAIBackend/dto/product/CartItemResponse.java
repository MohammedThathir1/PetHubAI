package com.PetHubAI.PetHubAIBackend.dto.product;


import com.PetHubAI.PetHubAIBackend.entity.CartItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private BigDecimal finalPrice;
    private String primaryImageUrl;
    private Integer quantity;
    private BigDecimal itemTotal;
    private Boolean inStock;
    private Integer availableStock;
    private LocalDateTime addedAt;

    public CartItemResponse() {}

    public CartItemResponse(CartItem cartItem) {
        this.id = cartItem.getId();
        this.productId = cartItem.getProduct().getId();
        this.productName = cartItem.getProduct().getName();
        this.productPrice = cartItem.getProduct().getPrice();
        this.quantity = cartItem.getQuantity();
        this.addedAt = cartItem.getAddedAt();
        this.inStock = cartItem.getProduct().getStockQuantity() > 0;
        this.availableStock = cartItem.getProduct().getStockQuantity();

        // Calculate final price with discount
        BigDecimal discount = cartItem.getProduct().getDiscountPercentage();
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            this.finalPrice = this.productPrice.subtract(
                    this.productPrice.multiply(discount).divide(BigDecimal.valueOf(100)));
        } else {
            this.finalPrice = this.productPrice;
        }

        this.itemTotal = this.finalPrice.multiply(BigDecimal.valueOf(this.quantity));
                //this.finalPrice * this.quantity;

        // Get primary image
        if (cartItem.getProduct().getImages() != null && !cartItem.getProduct().getImages().isEmpty()) {
            this.primaryImageUrl = cartItem.getProduct().getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .findFirst()
                    .map(img -> img.getImageUrl())
                    .orElse(cartItem.getProduct().getImages().get(0).getImageUrl());
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }

    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }

    public String getPrimaryImageUrl() { return primaryImageUrl; }
    public void setPrimaryImageUrl(String primaryImageUrl) { this.primaryImageUrl = primaryImageUrl; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getItemTotal() { return itemTotal; }
    public void setItemTotal(BigDecimal itemTotal) { this.itemTotal = itemTotal; }

    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }

    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
