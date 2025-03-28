package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.entities.Basket;
import com.fabiolima.e_commerce.entities.Order;
import com.fabiolima.e_commerce.entities.User;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.OrderService;
import com.fabiolima.e_commerce.service.ProductService;
import com.fabiolima.e_commerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user/{userId}/order")
@PreAuthorize("@userAuthenticationService.isOwner(#userId, authentication)")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final BasketService basketService;
    private final UserService userService;

    @Autowired
    public OrderController (OrderService orderService,
                            ProductService productService, BasketService basketService, UserService userService){
        this.orderService = orderService;
        this.productService = productService;
        this.basketService = basketService;
        this.userService = userService;
    }

    @Operation(summary = "Creates an Order from Basket")
    @PostMapping("/create-order")
    public ResponseEntity<Order> createOrderFromBasket(@PathVariable("userId") UUID userId){
        User user = userService.findUserByUserId(userId);
        Basket basket = basketService.returnNewestActiveBasket(user);

        return ResponseEntity.ok(orderService.createOrderAndAddToUser(userId,basket));
    }

    @Operation(summary = "Retrieve newest created Order - Useful to fetch the created order when checking basket out")
    @GetMapping("/newest-created-order")
    public ResponseEntity<Order> getNewestPendingBasket(@PathVariable("userId") UUID userId){
        return  ResponseEntity.ok(orderService.returnNewestPendingOrder(userId));
    }

    @Operation(summary = "It retrieves all users orders")
    @GetMapping
    public ResponseEntity<Page<Order>> getAllUsersOrders(@RequestParam(defaultValue = "0") int pgNum,
                                                         @RequestParam(defaultValue = "25") int pgSize,
                                                         @PathVariable ("userId") UUID userId){
        Page<Order> orders = orderService.getUserOrders(pgNum, pgSize, userId);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "It retrieves all users orders depending on its status")
    @GetMapping("/status")
    public ResponseEntity<Page<Order>> getUsersOrdersByOrderStatus(@RequestParam(defaultValue = "0") int pgNum,
                                                                   @RequestParam(defaultValue = "25") int pgSize,
                                                                   @PathVariable("userId") UUID userId,
                                                                   @RequestParam("status") String status){

        Page<Order> orders = orderService.getUserOrdersByStatus(pgNum, pgSize, userId, status);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Retrieve users order by its id")
    @GetMapping("/{orderId}")
    @PreAuthorize("@orderAuthenticationService.isOwner(#orderId,authentication)")
    public ResponseEntity<Order> getUsersOrderByOrderId(@PathVariable ("userId") UUID userId,
                                                        @PathVariable ("orderId") UUID orderId){
        return ResponseEntity.ok(orderService.findOrderById(orderId));
    }

    @Operation(summary = "Update order status")
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("@orderAuthenticationService.isOwner(#orderId,authentication)")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable ("orderId") UUID orderId,
                                                   @RequestParam ("status") String status){
        Order order = orderService.updateOrderStatus(orderId,status);

        return ResponseEntity.ok(order);
    }

    @Operation(summary = "To cancel an order if its current status is pending")
    @DeleteMapping("/{orderId}/cancel")
    @PreAuthorize("@orderAuthenticationService.isOwner(#orderId,authentication)")
    public ResponseEntity<Order> cancelOrder(@PathVariable ("orderId") UUID orderId){

        Order order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }
}