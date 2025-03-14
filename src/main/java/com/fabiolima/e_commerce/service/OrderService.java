package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.Order;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    Order returnNewestPendingOrder(UUID userId);
    Order createOrderAndAddToUser(UUID userId, Basket basket);
    Page<Order> getUserOrders(int pgNum, int pgSize, UUID userId);
    Page<Order> getUserOrdersByStatus(int pgNum, int pgSize, UUID userId, String orderStatus);
    Order updateOrderStatus(UUID orderId, String orderStatus);
    Order cancelOrder(UUID orderId);
    Order findOrderById(UUID orderId);
}
