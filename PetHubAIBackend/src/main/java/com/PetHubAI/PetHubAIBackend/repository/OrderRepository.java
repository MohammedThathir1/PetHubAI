package com.PetHubAI.PetHubAIBackend.repository;

import com.PetHubAI.PetHubAIBackend.entity.Order;
import com.PetHubAI.PetHubAIBackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by user
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Find order by Razorpay order ID
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);

    // Find orders by status
    Page<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status, Pageable pageable);

    // Find orders by payment status
    Page<Order> findByPaymentStatusOrderByCreatedAtDesc(Order.PaymentStatus paymentStatus, Pageable pageable);

    // ✅ NEW: Find COD orders
    Page<Order> findByPaymentMethodOrderByCreatedAtDesc(String paymentMethod, Pageable pageable);

    // Find orders by date range
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Get total revenue
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.paymentStatus = 'PAID'")
    Double getTotalRevenue();

    // ✅ NEW: Get COD revenue (pending payments)
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.paymentMethod = 'COD' AND o.status = 'DELIVERED'")
    Double getCODRevenue();

    // Get order count by status
    long countByStatus(Order.OrderStatus status);

    // ✅ NEW: Count COD orders
    long countByPaymentMethod(String paymentMethod);

    // Find orders with items
    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.user = :user ORDER BY o.createdAt DESC")
    List<Order> findByUserWithItems(@Param("user") User user);

    // ✅ NEW: Find order with items by ID (for order confirmation)
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    // ✅ NEW: Find user order with items by ID
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id AND o.user = :user")
    Optional<Order> findByIdAndUserWithItems(@Param("id") Long id, @Param("user") User user);

    // Admin - get all orders
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // ✅ NEW: Find recent orders for dashboard
    List<Order> findTop10ByOrderByCreatedAtDesc();

    // ✅ NEW: Find pending COD orders
    @Query("SELECT o FROM Order o WHERE o.paymentMethod = 'COD' AND o.status = 'PENDING' ORDER BY o.createdAt DESC")
    List<Order> findPendingCODOrders();
}
