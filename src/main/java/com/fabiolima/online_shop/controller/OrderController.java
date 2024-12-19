package com.fabiolima.online_shop.controller;

import com.fabiolima.online_shop.model.TheOrder;
import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.service.OrderService;
import com.fabiolima.online_shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/{userId}/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

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
        return ResponseEntity.ok(orderService.updateStatusOrder(userId,orderId,status));
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