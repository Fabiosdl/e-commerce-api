package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.TheOrder;
import com.fabiolima.online_shop.model.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    TheOrder saveOrderAndAddToUser(Long userId, TheOrder theOrder);
    List<TheOrder> getUserOrders(Long userId);
    TheOrder getUserOrderById(Long userId, Long orderId);
    TheOrder updateStatusOrder(Long userId, Long orderId, String orderStatus);
    TheOrder cancelOrder(Long userId, Long orderId);
    List<TheOrder> getOrdersByStatus(Long userId, String status);
    TheOrder findOrderById(Long orderId);
}
