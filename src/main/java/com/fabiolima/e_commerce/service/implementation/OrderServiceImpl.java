package com.fabiolima.e_commerce.service.implementation;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.*;
import com.fabiolima.e_commerce.model.enums.OrderStatus;
import com.fabiolima.e_commerce.repository.OrderRepository;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.OrderService;
import com.fabiolima.e_commerce.service.ProductService;
import com.fabiolima.e_commerce.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public OrderServiceImpl (OrderRepository orderRepository,
                             UserService userService, ProductService productService){
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
    }

    @Override
    @Transactional
    public Order createOrderAndAddToUser(Long userId, Basket basket) {

        //1 - Retrieve the User
        User user = userService.findUserByUserId(userId);

        //2 - Convert basket to order
        Order order = convertBasketToOrder(basket);

        //3 - Set Pending status to order
        order.setOrderStatus(OrderStatus.PENDING);

        //4 - Link order to user
        user.addOrderToUser(order);

        //5 - persist new order attached to user
        return orderRepository.save(order);
    }

    //helper method for createOrderAndAddToUser
    @Transactional
    public Order convertBasketToOrder(Basket basket) {
        // initialize a new order
        Order order = basket.getOrder();

        // check if the Basket already has an order.
        // If it does, clear the order and insert the new items
        if( order != null) {
            // clear the items in order if it does exist
            order = basket.getOrder();
            order.getItems().clear();
        } else {
            // create new order based on the basket
            order = new Order();
            order.setUser(basket.getUser());
            order.setBasket(basket);
        }

        //transfer data from the basket to order
        transferBasketDataToOrder(basket, order);

        log.info("items in order after transfer: {}", order.getItems());

        return orderRepository.save(order);
    }

    // Helper method for convertBasketToOrder
    private Order transferBasketDataToOrder(Basket basket, Order order){
        //transform basket items into order items and store it in TheOrder
        BigDecimal totalPrice = BigDecimal.ZERO;
        for(BasketItem bi : basket.getBasketItems()){
            order.addOrderItemToOrder(OrderItem.builder()
                    .productId(bi.getProduct().getId())
                    .productName(bi.getProduct().getProductName())
                    .quantity(bi.getQuantity())
                    .price(bi.getProduct().getProductPrice())
                    .build());
            BigDecimal itemTotal = bi.getProduct().getProductPrice()
                    .multiply(new BigDecimal(bi.getQuantity()));  // Multiply by quantity (int)
            totalPrice = totalPrice.add(itemTotal);  // Accumulate total price
        }
        // retrieve total cost
        order.setTotalPrice(totalPrice);
        return order;
    }

    @Override
    public Order returnNewestPendingOrder(Long userId) {
        //1- Fetch user
        User user = userService.findUserByUserId(userId);
        //2- Fetch the newest order with status pending
        Optional<Order> newestOrder = user.getOrders()
                .stream()
                .filter(order -> "PENDING".equalsIgnoreCase(order.getOrderStatus().name()))
                .max(Comparator.comparing(Order::getCreatedAt));

        if(newestOrder.isEmpty())
            throw new NotFoundException("Order with status PENDING not found");

        return newestOrder.get();
    }

    @Override
    public Page<Order> getUserOrders(int pgNum, int pgSize, Long userId) {

        User theUser = userService.findUserByUserId(userId);
        Pageable pageable = PageRequest.of(pgNum, pgSize);

        return orderRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public Page<Order> getUserOrdersByStatus(int pgNum, int pgSize, Long userId, String status) {
        //Check if orderStatus is a valid Enum
        if (!OrderStatus.isValid(status))
            throw new IllegalArgumentException(String.format("Invalid order status %s", status));
        //transform string into enum
        OrderStatus orderStatus = OrderStatus.fromString(status);

        Pageable pageable = PageRequest.of(pgNum,pgSize);

        return orderRepository.findByOrderStatusAndUserId(orderStatus, userId, pageable);
    }

    @Override
    public Order findOrderById(Long orderId){

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("Order with Id %d not found.", orderId)));
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, String orderStatus) {

        //Check if orderStatus is a valid Enum
        if (!OrderStatus.isValid(orderStatus))
            throw new IllegalArgumentException(String.format("Invalid order status %s", orderStatus));

        //Fetch order
        Order order = findOrderById(orderId);

        // set new status to order depending on the current Status
        String currentStatus = order.getOrderStatus().toString();
        switch (currentStatus){
            case "PENDING" : if(orderStatus.equalsIgnoreCase("PAID"))
                                order.setOrderStatus(OrderStatus.fromString(orderStatus));
                            else if(orderStatus.equalsIgnoreCase("CANCELLED"))
                                throw new ForbiddenException("Please, use the method cancelOrder to cancel an order.");
                            else
                                throw new ForbiddenException("Current Status PENDING can only be updated to PAID. To update to Cancel use method cancelOrder.");
                            break;
            case "PAID" :   if(orderStatus.equalsIgnoreCase("COMPLETED"))
                                order.setOrderStatus(OrderStatus.fromString(orderStatus));
                            else
                                throw new ForbiddenException("Current Status PAID can only be updated to COMPLETED.");
                            break;
            default: throw new ForbiddenException("Current Status " + currentStatus + " cannot be updated.");
        }

        if(order.getOrderStatus().toString().equalsIgnoreCase("cancelled"))
            productService.incrementStocksWhenOrderIsCancelled(order);

        //persist new order
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long orderId) {
        log.info("cancel order being called");
        //retrieve order
        Order order = findOrderById(orderId);
        //retrieve current status of order
        OrderStatus currentStatus = order.getOrderStatus();

        //check if its possible to cancel order
        if (Objects.requireNonNull(currentStatus) == OrderStatus.PENDING) {
            order.setOrderStatus(OrderStatus.CANCELLED);
        } else {
            throw new ForbiddenException("Only orders with status PENDING can be cancelled");
        }

        log.info("Order id {} has been cancelled",orderId);
        productService.incrementStocksWhenOrderIsCancelled(order);
        return orderRepository.save(order);
    }

}