package com.fabiolima.online_shop.controller;

import com.fabiolima.online_shop.model.TheOrder;
import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.service.OrderService;
import com.fabiolima.online_shop.service.ProductService;
import com.fabiolima.online_shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/{userId}/order")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public OrderController (OrderService orderService,
                            UserService userService,
                            ProductService productService){
        this.orderService = orderService;
        this.userService = userService;
        this.productService = productService;
    }

    @PostMapping //the front end will pass all the payload of an order, including basketId, address and total price
    public ResponseEntity<TheOrder> createNewOrder(@PathVariable ("userId") Long userId,
                                                   @RequestBody TheOrder order){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.saveOrderAndAddToUser(userId, order));
    }

    @GetMapping
    public ResponseEntity<List<TheOrder>> getAllUsersOrders(@PathVariable ("userId") Long userId){
        User user = userService.findUserByUserId(userId);
        return ResponseEntity.ok(user.getOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<TheOrder> getUsersOrderByOrderId(@PathVariable ("userId") Long userId,
                                                           @PathVariable ("orderId") Long orderId){
        return ResponseEntity.ok(orderService.getUserOrderById(userId,orderId));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<TheOrder> updateOrderStatus(@PathVariable ("userId") Long userId,
                                                      @PathVariable ("orderId") Long orderId,
                                                      @RequestParam ("status") String status){
        TheOrder order = orderService.updateStatusOrder(userId,orderId,status);

        /**
         * As it's a small online shop, the stock quantity will be decremented only after payment
         */
        if(status.equalsIgnoreCase("paid"))
            productService.updateQuantInStock(order);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<TheOrder> cancelOrder(@PathVariable ("userId") Long userId,
                                                @PathVariable ("orderId") Long orderId){
        return ResponseEntity.ok(orderService.cancelOrder(userId,orderId));
    }

    // Get orders filtered by status
    @GetMapping("/status")
    public ResponseEntity<List<TheOrder>> getOrdersByStatus(
            @PathVariable("userId") Long userId, @RequestParam("status") String status) {
        List<TheOrder> ordersByStatus = orderService.getOrdersByStatus(userId, status);
        return ResponseEntity.ok(ordersByStatus);
    }
}