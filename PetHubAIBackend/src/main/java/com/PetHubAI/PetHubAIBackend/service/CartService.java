package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.dto.product.CartItemRequest;
import com.PetHubAI.PetHubAIBackend.dto.product.CartItemResponse;
import com.PetHubAI.PetHubAIBackend.entity.CartItem;
import com.PetHubAI.PetHubAIBackend.entity.Product;
import com.PetHubAI.PetHubAIBackend.entity.User;
import com.PetHubAI.PetHubAIBackend.exception.ProductNotFoundException;
import com.PetHubAI.PetHubAIBackend.repository.CartItemRepository;
import com.PetHubAI.PetHubAIBackend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<CartItemResponse> getCartItems(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUserWithProductDetails(user);
        return cartItems.stream()
                .map(CartItemResponse::new)
                .collect(Collectors.toList());
    }

    public CartItemResponse addToCart(CartItemRequest request, User user) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (!product.getIsActive()) {
            throw new RuntimeException("Product is not available");
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
            }

            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
            return new CartItemResponse(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());

            CartItem savedItem = cartItemRepository.save(cartItem);
            return new CartItemResponse(savedItem);
        }
    }

    public CartItemResponse updateCartItem(Long cartItemId, Integer quantity, User user) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        if (cartItem.getProduct().getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + cartItem.getProduct().getStockQuantity());
        }

        cartItem.setQuantity(quantity);
        CartItem savedItem = cartItemRepository.save(cartItem);
        return new CartItemResponse(savedItem);
    }

    public void removeFromCart(Long cartItemId, User user) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        cartItemRepository.delete(cartItem);
    }

    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    public int getCartItemCount(User user) {
        return cartItemRepository.countByUser(user);
    }

    public Double getCartTotal(User user) {
        Double total = cartItemRepository.calculateCartTotal(user);
        return total != null ? total : 0.0;
    }

    public CartSummary getCartSummary(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUserWithProductDetails(user);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        int itemCount = 0;

        for (CartItem item : cartItems) {
            BigDecimal itemPrice = item.getProduct().getPrice();
            BigDecimal itemDiscount = item.getProduct().getDiscountPercentage() != null ?
                    item.getProduct().getDiscountPercentage() : BigDecimal.ZERO;

// Calculate discount amount
            BigDecimal discountAmount = itemPrice.multiply(itemDiscount)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

// Final price after discount
            BigDecimal finalPrice = itemPrice.subtract(discountAmount);

// Update subtotal: finalPrice * quantity
            subtotal = subtotal.add(finalPrice.multiply(BigDecimal.valueOf(item.getQuantity())));

// Update total discount: discountAmount * quantity
            discount = discount.add(discountAmount.multiply(BigDecimal.valueOf(item.getQuantity())));

// Update item count
            itemCount += item.getQuantity();

        }

        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(500)) > 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(50);

        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(shipping).add(tax);


        return new CartSummary(itemCount, subtotal, discount, shipping, tax, total);
    }

    public static class CartSummary {
        private int itemCount;
        private BigDecimal subtotal;
        private BigDecimal discount;
        private BigDecimal shipping;
        private BigDecimal tax;
        private BigDecimal total;

        public CartSummary(int itemCount, BigDecimal subtotal, BigDecimal discount, BigDecimal shipping, BigDecimal tax, BigDecimal total) {
            this.itemCount = itemCount;
            this.subtotal = subtotal;
            this.discount = discount;
            this.shipping = shipping;
            this.tax = tax;
            this.total = total;
        }

        // Getters
        public int getItemCount() { return itemCount; }
        public BigDecimal getSubtotal() { return subtotal; }
        public BigDecimal getDiscount() { return discount; }
        public BigDecimal getShipping() { return shipping; }
        public BigDecimal getTax() { return tax; }
        public BigDecimal getTotal() { return total; }
    }
}
