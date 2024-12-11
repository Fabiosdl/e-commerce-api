package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.exceptions.NotFoundException;
import com.fabiolima.online_shop.model.TheOrder;
import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.model.enums.OrderStatus;
import com.fabiolima.online_shop.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;


    @Override
    @Transactional
    public TheOrder saveOrderAndAddToUser(Long userId, TheOrder theOrder) {
        // fetch the user
        User theUser = userService.findUserByUserId(userId);

        // use helper method to add order to user
        theUser.addOrderToUser(theOrder);

        // persist user with its new order. The order will also be persisted due to the bidirectional helper method
        userService.saveUser(theUser);

        return theOrder;
    }

    @Override
    public List<TheOrder> getUserOrders(Long userId) {

        User theUser = userService.findUserByUserId(userId);
        return theUser.getOrders();
    }

    @Override
    public TheOrder getUserOrderById(Long userId, Long orderId) {
        // validate and fetch the order
        return validateAndFetchOrder(userId,orderId);
    }

    @Override
    public TheOrder updateStatusOrder(Long userId, Long orderId, String orderStatus) {

        //Check if orderStatus is a valid Enum
        if (!OrderStatus.isValid(orderStatus))
            throw new IllegalArgumentException(String.format("Invalid order status %s", orderStatus));

        //validate and fetch order
        TheOrder theOrder = validateAndFetchOrder(userId, orderId);

        // set new status to order
        theOrder.setOrderStatus(OrderStatus.fromString(orderStatus));

        //persist new order
        return orderRepository.save(theOrder);
    }

    @Override
    public TheOrder cancelOrder(Long userId, Long orderId) {
        TheOrder theOrder = validateAndFetchOrder(userId, orderId);
        theOrder.setOrderStatus(OrderStatus.CANCELLED);
        return orderRepository.save(theOrder);
    }

    @Override
    public List<TheOrder> getOrdersByStatus(Long userId, String status) {

        //Check if orderStatus is a valid Enum
        if (!OrderStatus.isValid(status))
            throw new IllegalArgumentException(String.format("Invalid order status %s", status));

        OrderStatus orderStatus = OrderStatus.fromString(status);

        //fetch user
        User theUser = userService.findUserByUserId(userId);

        // get the full List of orders
        List<TheOrder> orderList = theUser.getOrders();

        // add only the orders with designated status
        List<TheOrder> selectedOrder = new ArrayList<>();
        for(TheOrder o : orderList){
            if(o.getOrderStatus().equals(orderStatus)){
                selectedOrder.add(o);
            }
        }

        return selectedOrder;
    }

    private TheOrder findOrderById(Long orderId){

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("Order with Id %d not found.", orderId)));
    }

    private TheOrder validateAndFetchOrder(Long userId, Long orderId){

        return orderRepository.findOrderByIdAndUserId(orderId,userId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Order with Id %d does not belong to User with Id %d",orderId,userId
                )));

    }
}