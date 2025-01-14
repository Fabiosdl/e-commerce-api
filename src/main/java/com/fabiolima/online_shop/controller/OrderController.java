package com.fabiolima.online_shop.controller;

import com.fabiolima.online_shop.model.TheOrder;
import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.model.enums.OrderStatus;
import com.fabiolima.online_shop.service.OrderService;
import com.fabiolima.online_shop.service.ProductService;
import com.fabiolima.online_shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/{userId}/order")
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
    public ResponseEntity<TheOrder> createNewOrder(@PathVariable ("userId") Long userId,
                                                   @RequestBody TheOrder order){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.saveOrderAndAddToUser(userId, order));
    }

    @GetMapping
    public ResponseEntity<Page<TheOrder>> getAllUsersOrders(@RequestParam(defaultValue = "0") int pgNum,
                                                            @RequestParam(defaultValue = "25") int pgSize,
                                                            @PathVariable ("userId") Long userId){
        Page<TheOrder> orders = orderService.getUserOrders(pgNum, pgSize, userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status")
    public ResponseEntity<Page<TheOrder>> getUsersOrdersByOrderStatus(@RequestParam(defaultValue = "0") int pgNum,
                                                                        @RequestParam(defaultValue = "25") int pgSize,
                                                                        @PathVariable("userId") Long userId,
                                                                        @RequestParam("status") String status){

        Page<TheOrder> orders = orderService.getUserOrdersByStatus(pgNum, pgSize, userId, status);
        return ResponseEntity.ok(orders);
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
         * Stock quantity will be replaced if order status is cancelled
         */
        if(status.equalsIgnoreCase("cancelled"))
            productService.incrementStocksWhenOrderIsCancelled(order);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<TheOrder> cancelOrder(@PathVariable ("userId") Long userId,
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