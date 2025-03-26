package com.fabiolima.e_commerce.repository;

import com.fabiolima.e_commerce.entities.Order;
import com.fabiolima.e_commerce.entities.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    //@Query("SELECT o FROM TheOrder o WHERE o.id = :orderId AND o.user.id = :userId")
    Optional<Order> findOrderByIdAndUserId(@Param("orderId") UUID orderId, @Param("userId") UUID userId);
    // Don't need the query because JPA recognizes the method name and know what to do

    Page<Order> findAllByUserId(UUID userId, Pageable pageable);

    Page<Order> findByOrderStatusAndUserId(OrderStatus orderStatus, UUID userId, Pageable pageable);

    Optional<Order>  findByPaypalOrderId(String paypalOrderId);

    Boolean existsByIdAndUserId(UUID orderId, UUID userId);

}