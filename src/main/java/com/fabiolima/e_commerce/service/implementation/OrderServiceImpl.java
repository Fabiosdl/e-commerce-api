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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final BasketService basketService;

    @Autowired
    public OrderServiceImpl (OrderRepository orderRepository,
                             UserService userService,
                             ProductService productService, BasketService basketService){
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.basketService = basketService;
    }

    @Override
    @Transactional
    public Order convertBasketToOrder(Basket basket) {

        // create new order based on the basket
        Order order = new Order();
        order.setUser(basket.getUser());
        order.setBasket(basket);
        order.setTotalPrice(basketService.calculateTotalPrice(basket.getId()));

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
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order createOrderAndAddToUser(Long userId, Basket basket) {

        //1 - Retrieve the User
        User user = userService.findUserByUserId(userId);

        //2 - Convert basket to order
        Order order = convertBasketToOrder(basket);

        //3 - Link order to user
        order.setUser(user);

        //4 - persist new order attached to user
        return orderRepository.save(order);
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
    public Order getUserOrderById(Long userId, Long orderId) {

        // validate and fetch the order
        return validateAndFetchOrder(userId,orderId);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long userId, Long orderId, String orderStatus) {

        //Check if orderStatus is a valid Enum
        if (!OrderStatus.isValid(orderStatus))
            throw new IllegalArgumentException(String.format("Invalid order status %s", orderStatus));

        //validate and fetch order
        Order order = validateAndFetchOrder(userId, orderId);

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

        //persist new order
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long userId, Long orderId) {
        //retrieve order
        Order order = validateAndFetchOrder(userId, orderId);
        //retrieve current status of order
        OrderStatus currentStatus = order.getOrderStatus();

        //check if its possible to cancel order
        if (Objects.requireNonNull(currentStatus) == OrderStatus.PENDING) {
            order.setOrderStatus(OrderStatus.CANCELLED);
        } else {
            throw new ForbiddenException("Only orders with status PENDING can be cancelled");
        }
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByStatus(Long userId, String status) {

        //Check if orderStatus is a valid Enum
        if (!OrderStatus.isValid(status))
            throw new IllegalArgumentException(String.format("Invalid order status %s", status));

        OrderStatus orderStatus = OrderStatus.fromString(status);

        //fetch user
        User theUser = userService.findUserByUserId(userId);

        // get the full List of orders
        List<Order> orderList = theUser.getOrders();

        // add only the orders with designated status
        List<Order> selectedOrder = new ArrayList<>();
        for(Order o : orderList){
            if(o.getOrderStatus().equals(orderStatus)){
                selectedOrder.add(o);
            }
        }

        return selectedOrder;
    }
    @Override
    public Order findOrderById(Long orderId){

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("Order with Id %d not found.", orderId)));
    }

    private Order validateAndFetchOrder(Long userId, Long orderId){

        return orderRepository.findOrderByIdAndUserId(orderId,userId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Order with Id %d does not belong to User with Id %d",orderId,userId
                )));

    }
}