package com.fabiolima.e_commerce.repository;

import com.fabiolima.e_commerce.model.Order;
import com.fabiolima.e_commerce.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    //@Query("SELECT o FROM TheOrder o WHERE o.id = :orderId AND o.user.id = :userId")
    Optional<Order> findOrderByIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);
    // Don't need the query because JPA recognizes the method name and know what to do

    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    Page<Order> findByOrderStatusAndUserId(OrderStatus orderStatus, Long userId, Pageable pageable);

    Optional<Order>  findByPaypalOrderId(String paypalOrderId);

}