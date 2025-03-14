package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.model.Order;
import com.fabiolima.e_commerce.service.OrderService;
import com.fabiolima.e_commerce.service.implementation.PaypalServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/user/{userId}/order/{orderId}/payments")
public class PayPalController {

    private final PaypalServiceImpl paypalServiceImpl;
    private final OrderService orderService;

    @Autowired
    public PayPalController(PaypalServiceImpl paypalServiceImpl, OrderService orderService){
        this.paypalServiceImpl = paypalServiceImpl;
        this.orderService = orderService;
    }

    @Operation(summary = "Creates a paypal order and request the user to approve it")
    @PostMapping("/create")
    public String createPayPalOrder(@PathVariable("orderId") UUID orderId){
        
        return paypalServiceImpl.createOrder(orderId);
    }

    /**
     * Once the user approves the payment, PayPal returns an orderId.
     * Use this to execute the payment:
     */

    @Operation(summary = "Captures the created order by its Id")
    @PostMapping("/capture")
    public ResponseEntity<Order> captureOrder(@RequestParam String token) {
        return ResponseEntity.ok(paypalServiceImpl.captureOrder(token));
    }
}
