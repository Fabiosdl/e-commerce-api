package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.Order;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {
    Order convertBasketToOrder(Basket basket);
    Order createOrderAndAddToUser(Long userId, Basket basket);
    Page<Order> getUserOrders(int pgNum, int pgSize, Long userId);
    Page<Order> getUserOrdersByStatus(int pgNum, int pgSize, Long userId, String orderStatus);
    Order getUserOrderById(Long userId, Long orderId);
    Order updateStatusOrder(Long userId, Long orderId, String orderStatus);
    Order cancelOrder(Long userId, Long orderId);
    List<Order> getOrdersByStatus(Long userId, String status);
    Order findOrderById(Long orderId);
}
