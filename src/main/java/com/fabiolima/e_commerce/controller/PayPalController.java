package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.service_implementation.PaypalService;
import com.paypal.orders.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PayPalController {

    private final PaypalService paypalService;

    @Autowired
    public PayPalController(PaypalService paypalService){
        this.paypalService = paypalService;
    }

    @PostMapping("/create")
    public String createOrder(@RequestBody com.fabiolima.e_commerce.model.Order entityOrder){
        return paypalService.createOrder(entityOrder);
    }

    /**
     * Once the user approves the payment, PayPal returns an orderId.
     * Use this to execute the payment:
     */

    @PostMapping("/capture")
    public ResponseEntity<Order> captureOrder(@RequestParam String orderId) {
        return ResponseEntity.ok(paypalService.captureOrder(orderId));
    }
}
