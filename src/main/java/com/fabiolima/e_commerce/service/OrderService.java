package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.Order;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Order returnNewestPendingOrder(Long userId);
    Order createOrderAndAddToUser(Long userId, Basket basket);
    Page<Order> getUserOrders(int pgNum, int pgSize, Long userId);
    Page<Order> getUserOrdersByStatus(int pgNum, int pgSize, Long userId, String orderStatus);
    Order updateOrderStatus(Long orderId, String orderStatus);
    Order cancelOrder(Long orderId);
    Order findOrderById(Long orderId);
}
