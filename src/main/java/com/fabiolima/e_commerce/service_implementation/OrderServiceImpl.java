package com.fabiolima.e_commerce.service_implementation;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.InvalidQuantityException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.*;
import com.fabiolima.e_commerce.model.enums.BasketStatus;
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
    public TheOrder convertBasketToOrder(Long userId, Long basketId) {
        //1-Basket Validation

        // check if the basket belongs to user
        Basket theBasket = basketService.validateAndFetchBasket(userId, basketId);

        //check if the basket is empty
        if(theBasket.getBasketItems().isEmpty())
            throw new InvalidQuantityException("The current basket is empty.");

        // check if basket status is ACTIVE and set it to Checked out
        if(!theBasket.getBasketStatus().equals(BasketStatus.ACTIVE))
            throw new ForbiddenException("Can only check out an ACTIVE basket.");

        //2-Create the Order add to user and persist it to databases

        TheOrder order = createOrderAndAddToUser(theBasket);

        //3- Set basket status as CHECKED_OUT
        theBasket.setBasketStatus(BasketStatus.CHECKED_OUT);

        //4- Create a new basket to the user
        basketService.createBasketAndAddToUser(theBasket.getUser());

        return order;
    }

    @Override
    @Transactional
    public TheOrder createOrderAndAddToUser(Basket basket) {

        // create new order based on the basket
        TheOrder order = new TheOrder();
        order.setUser(basket.getUser());
        order.setBasket(basket);
        order.setTotalPrice(basketService.calculateTotalPrice(basket.getId()));

        //transform basket items into order items and store it in TheOrder
        double totalPrice = 0.0;
        for(BasketItem bi : basket.getBasketItems()){
            order.addOrderItemToOrder(OrderItem.builder()
                            .productId(bi.getProduct().getId())
                            .productName(bi.getProduct().getProductName())
                            .quantity(bi.getQuantity())
                            .price(bi.getProduct().getProductPrice())
                    .build());
            totalPrice += bi.getProduct().getProductPrice() * bi.getQuantity();
        }
        //truncate totalPrice to 2 digits after decimal
        double truncatedPrice = Math.floor(totalPrice * 100)/100;
        // retrieve total cost
        order.setTotalPrice(Math.floor(truncatedPrice));
        return orderRepository.save(order);
    }

    @Override
    public Page<TheOrder> getUserOrders(int pgNum, int pgSize, Long userId) {

        User theUser = userService.findUserByUserId(userId);
        Pageable pageable = PageRequest.of(pgNum, pgSize);

        return orderRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public Page<TheOrder> getUserOrdersByStatus(int pgNum, int pgSize, Long userId, String status) {
        //Check if orderStatus is a valid Enum
        if (!OrderStatus.isValid(status))
            throw new IllegalArgumentException(String.format("Invalid order status %s", status));
        //transform string into enum
        OrderStatus orderStatus = OrderStatus.fromString(status);

        Pageable pageable = PageRequest.of(pgNum,pgSize);

        return orderRepository.findByOrderStatusAndUserId(orderStatus, userId, pageable);
    }

    @Override
    public TheOrder getUserOrderById(Long userId, Long orderId) {

        // validate and fetch the order
        return validateAndFetchOrder(userId,orderId);
    }

    @Override
    @Transactional
    public TheOrder updateStatusOrder(Long userId, Long orderId, String orderStatus) {

        //Check if orderStatus is a valid Enum
        if (!OrderStatus.isValid(orderStatus))
            throw new IllegalArgumentException(String.format("Invalid order status %s", orderStatus));

        //validate and fetch order
        TheOrder theOrder = validateAndFetchOrder(userId, orderId);

        // set new status to order depending on the current Status
        String currentStatus = theOrder.getOrderStatus().toString();
        switch (currentStatus){
            case "PENDING" : if(orderStatus.equalsIgnoreCase("PAID"))
                                theOrder.setOrderStatus(OrderStatus.fromString(orderStatus));
                            else if(orderStatus.equalsIgnoreCase("CANCELLED"))
                                throw new ForbiddenException("Please, use the method cancelOrder to cancel an order.");
                            else
                                throw new ForbiddenException("Current Status PENDING can only be updated to PAID. To update to Cancel use method cancelOrder.");
                            break;
            case "PAID" :   if(orderStatus.equalsIgnoreCase("COMPLETED"))
                                theOrder.setOrderStatus(OrderStatus.fromString(orderStatus));
                            else
                                throw new ForbiddenException("Current Status PAID can only be updated to COMPLETED.");
                            break;
            default: throw new ForbiddenException("Current Status " + currentStatus + " cannot be updated.");
        }

        //persist new order
        return orderRepository.save(theOrder);
    }

    @Override
    public TheOrder cancelOrder(Long userId, Long orderId) {
        //retrieve order
        TheOrder theOrder = validateAndFetchOrder(userId, orderId);
        //retrieve current status of order
        OrderStatus currentStatus = theOrder.getOrderStatus();

        //check if its possible to cancel order
        if (Objects.requireNonNull(currentStatus) == OrderStatus.PENDING) {
            theOrder.setOrderStatus(OrderStatus.CANCELLED);
        } else {
            throw new ForbiddenException("Only orders with status PENDING can be cancelled");
        }
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
    @Override
    public TheOrder findOrderById(Long orderId){

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