package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.TheOrder;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {
    TheOrder saveOrderAndAddToUser(Long userId, TheOrder theOrder);
    Page<TheOrder> getUserOrders(int pgNum, int pgSize, Long userId);
    Page<TheOrder> getUserOrdersByStatus(int pgNum, int pgSize, Long userId, String orderStatus);
    TheOrder getUserOrderById(Long userId, Long orderId);
    TheOrder updateStatusOrder(Long userId, Long orderId, String orderStatus);
    TheOrder cancelOrder(Long userId, Long orderId);
    List<TheOrder> getOrdersByStatus(Long userId, String status);
    TheOrder findOrderById(Long orderId);
}
