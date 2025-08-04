package com.PetHubAI.PetHubAIBackend.service;

import com.PetHubAI.PetHubAIBackend.dto.product.OrderRequest;
import com.PetHubAI.PetHubAIBackend.dto.product.OrderResponse;
import com.PetHubAI.PetHubAIBackend.entity.*;
import com.PetHubAI.PetHubAIBackend.repository.CartItemRepository;
import com.PetHubAI.PetHubAIBackend.repository.OrderRepository;
import com.PetHubAI.PetHubAIBackend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentService paymentService;

    // ✅ NEW: Create COD Order
    public OrderResponse createCODOrder(OrderRequest request, User user) {
        System.out.println("🔍 Creating COD order for user: " + user.getEmail());

        try {
            // 1. Get cart items
            List<CartItem> cartItems = cartItemRepository.findByUserWithProductDetails(user);
            if (cartItems.isEmpty()) {
                throw new RuntimeException("Cart is empty. Cannot place order.");
            }

            // 2. Validate stock availability
            for (CartItem item : cartItems) {
                if (item.getProduct().getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for product: " + item.getProduct().getName());
                }
            }

            // 3. Create order
            Order order = new Order();
            order.setUser(user);
            order.setShippingAddress(request.getShippingAddress());
            order.setBillingAddress(request.getBillingAddress());
            order.setSpecialInstructions(request.getSpecialInstructions());
            order.setPaymentMethod("COD");
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
            order.setStatus(Order.OrderStatus.PENDING);

            // 4. Calculate totals
            OrderCalculation calculation = calculateOrderTotals(cartItems);
            order.setSubtotal(calculation.getSubtotal());
            order.setTotalAmount(calculation.getSubtotal());
            order.setDiscountAmount(calculation.getDiscount());
            order.setTaxAmount(calculation.getTax());
            order.setShippingCost(calculation.getShipping());
            order.setFinalAmount(calculation.getTotal());

            // 5. Create order items
            List<OrderItem> orderItems = createOrderItems(order, cartItems);
            order.setOrderItems(orderItems);

            // 6. Save order
            Order savedOrder = orderRepository.save(order);
            System.out.println("✅ COD Order created with ID: " + savedOrder.getId());

            // 7. Update product inventory
            updateProductInventory(cartItems);

            // 8. Clear user's cart
            cartItemRepository.deleteByUser(user);
            System.out.println("✅ Cart cleared for user: " + user.getEmail());

            return new OrderResponse(savedOrder);

        } catch (Exception e) {
            System.err.println("❌ Failed to create COD order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    // ✅ ENHANCED: Existing createOrder method (for Razorpay)
    public OrderResponse createOrder(CreateOrderRequest request, User user) {
        // Get cart items
        List<CartItem> cartItems = cartItemRepository.findByUserWithProductDetails(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Validate stock availability
        for (CartItem item : cartItems) {
            if (item.getProduct().getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + item.getProduct().getName());
            }
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());
        order.setBillingAddress(request.getBillingAddress());
        order.setPaymentMethod("RAZORPAY");

        // Calculate totals using same logic as COD
        OrderCalculation calculation = calculateOrderTotals(cartItems);
        order.setSubtotal(calculation.getSubtotal());
        order.setTotalAmount(calculation.getSubtotal());
        order.setDiscountAmount(calculation.getDiscount());
        order.setTaxAmount(calculation.getTax());
        order.setShippingCost(calculation.getShipping());
        order.setFinalAmount(calculation.getTotal());

        // Create order items
        List<OrderItem> orderItems = createOrderItems(order, cartItems);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Create Razorpay order
        String razorpayOrderId = paymentService.createRazorpayOrder(savedOrder);
        savedOrder.setRazorpayOrderId(razorpayOrderId);
        orderRepository.save(savedOrder);

        return new OrderResponse(savedOrder);
    }

    // ✅ NEW: Enhanced order calculation method
    private OrderCalculation calculateOrderTotals(List<CartItem> cartItems) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            BigDecimal originalPrice = product.getPrice();
            BigDecimal discountPercent = product.getDiscountPercentage() != null ?
                    product.getDiscountPercentage() : BigDecimal.ZERO;

            // Calculate item discount
            BigDecimal itemDiscount = originalPrice
                    .multiply(discountPercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            BigDecimal finalPrice = originalPrice.subtract(itemDiscount);
            BigDecimal itemTotal = finalPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            subtotal = subtotal.add(itemTotal);
            totalDiscount = totalDiscount.add(
                    itemDiscount.multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        // Calculate tax (18% GST)
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.18))
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate shipping (free for orders above ₹500)
        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(500)) >= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(50);

        // Calculate total
        BigDecimal total = subtotal.add(tax).add(shipping);

        return new OrderCalculation(subtotal, totalDiscount, tax, shipping, total);
    }

    // ✅ NEW: Create order items helper method
    private List<OrderItem> createOrderItems(Order order, List<CartItem> cartItems) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            BigDecimal originalPrice = product.getPrice();
            BigDecimal discountPercent = product.getDiscountPercentage() != null ?
                    product.getDiscountPercentage() : BigDecimal.ZERO;

            // Calculate final price after discount
            BigDecimal itemDiscount = originalPrice
                    .multiply(discountPercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal finalPrice = originalPrice.subtract(itemDiscount);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(finalPrice);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setItemTotal(finalPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            // Set primary image URL
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                orderItem.setPrimaryImageUrl(product.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                        .findFirst()
                        .map(ProductImage::getImageUrl)
                        .orElse(product.getImages().get(0).getImageUrl()));
            }

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    // ✅ NEW: Update product inventory helper method
    private void updateProductInventory(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            int newStock = product.getStockQuantity() - item.getQuantity();

            if (newStock < 0) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(newStock);
            productRepository.save(product);
        }
    }

    // ✅ ENHANCED: Get order by ID (with proper loading)
    public OrderResponse getOrderById(Long orderId, User user) {
        Order order = orderRepository.findByIdAndUserWithItems(orderId, user)
                .orElseThrow(() -> new RuntimeException("Order not found or access denied"));

        return new OrderResponse(order);
    }

    public Page<OrderResponse> getUserOrders(User user, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return orders.map(OrderResponse::new);
    }

    public OrderResponse confirmPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        // Verify payment with Razorpay
        boolean paymentVerified = paymentService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);

        if (!paymentVerified) {
            throw new RuntimeException("Payment verification failed");
        }

        Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Update order status
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setRazorpayPaymentId(razorpayPaymentId);

        // Update stock quantities
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        // Clear user cart
        cartItemRepository.deleteByUser(order.getUser());

        Order savedOrder = orderRepository.save(order);
        return new OrderResponse(savedOrder);
    }

    public void cancelOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order cannot be cancelled at this stage");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        // If payment was made, initiate refund
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            paymentService.initiateRefund(order.getRazorpayPaymentId(), order.getFinalAmount());
            order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
            orderRepository.save(order);
        }
    }

    // Admin methods
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        return orders.map(OrderResponse::new);
    }

    public void updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);

        if (status == Order.OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            // ✅ For COD orders, mark payment as completed when delivered
            if ("COD".equals(order.getPaymentMethod())) {
                order.setPaymentStatus(Order.PaymentStatus.PAID);
            }
        }

        orderRepository.save(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    // ✅ NEW: Helper class for order calculations
    private static class OrderCalculation {
        private final BigDecimal subtotal;
        private final BigDecimal discount;
        private final BigDecimal tax;
        private final BigDecimal shipping;
        private final BigDecimal total;

        public OrderCalculation(BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal shipping, BigDecimal total) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.tax = tax;
            this.shipping = shipping;
            this.total = total;
        }

        public BigDecimal getSubtotal() { return subtotal; }
        public BigDecimal getDiscount() { return discount; }
        public BigDecimal getTax() { return tax; }
        public BigDecimal getShipping() { return shipping; }
        public BigDecimal getTotal() { return total; }
    }

    public static class CreateOrderRequest {
        private String shippingAddress;
        private String billingAddress;

        // Getters and Setters
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

        public String getBillingAddress() { return billingAddress; }
        public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    }
}
