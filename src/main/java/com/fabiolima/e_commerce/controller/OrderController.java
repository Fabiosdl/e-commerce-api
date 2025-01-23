package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.model.Order;
import com.fabiolima.e_commerce.service.OrderService;
import com.fabiolima.e_commerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/{userId}/order")
@PreAuthorize("@userAuthenticationService.isOwner(userId, authentication)")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;

    @Autowired
    public OrderController (OrderService orderService,
                            ProductService productService){
        this.orderService = orderService;
        this.productService = productService;
    }

    @PostMapping //the front end will pass all the payload of an order, including basketId, address and total price
    public ResponseEntity<Order> createNewOrder(@PathVariable ("userId") Long userId,
                                                @RequestBody Order order){
        return null; //ResponseEntity.status(HttpStatus.CREATED)
                //.body(orderService.createOrderAndAddToUser(userId, order));
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getAllUsersOrders(@RequestParam(defaultValue = "0") int pgNum,
                                                         @RequestParam(defaultValue = "25") int pgSize,
                                                         @PathVariable ("userId") Long userId){
        Page<Order> orders = orderService.getUserOrders(pgNum, pgSize, userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status")
    public ResponseEntity<Page<Order>> getUsersOrdersByOrderStatus(@RequestParam(defaultValue = "0") int pgNum,
                                                                   @RequestParam(defaultValue = "25") int pgSize,
                                                                   @PathVariable("userId") Long userId,
                                                                   @RequestParam("status") String status){

        Page<Order> orders = orderService.getUserOrdersByStatus(pgNum, pgSize, userId, status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getUsersOrderByOrderId(@PathVariable ("userId") Long userId,
                                                        @PathVariable ("orderId") Long orderId){
        return ResponseEntity.ok(orderService.getUserOrderById(userId,orderId));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable ("userId") Long userId,
                                                   @PathVariable ("orderId") Long orderId,
                                                   @RequestParam ("status") String status){
        Order order = orderService.updateStatusOrder(userId,orderId,status);

        /**
         * Stock quantity will be replaced if order status is cancelled
         */
        if(status.equalsIgnoreCase("cancelled"))
            productService.incrementStocksWhenOrderIsCancelled(order);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable ("userId") Long userId,
                                             @PathVariable ("orderId") Long orderId){
        return ResponseEntity.ok(orderService.cancelOrder(userId,orderId));
    }

    // Get orders filtered by status
        /*    @GetMapping("/status")
    public ResponseEntity<List<TheOrder>> getOrdersByStatus(@PathVariable("userId") Long userId,
                                                            @RequestParam("status") String status) {
        List<TheOrder> ordersByStatus = orderService.getOrdersByStatus(userId, status);
        return ResponseEntity.ok(ordersByStatus);
    }*/
}