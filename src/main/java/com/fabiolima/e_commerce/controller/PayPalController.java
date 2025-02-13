package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.service.implementation.PaypalService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Creates a paypal order and request the user to approve it")
    @PostMapping("/create")
    public String createOrder(@RequestBody com.fabiolima.e_commerce.model.Order entityOrder){
        return paypalService.createOrder(entityOrder);
    }

    /**
     * Once the user approves the payment, PayPal returns an orderId.
     * Use this to execute the payment:
     */

    @Operation(summary = "Captures the created order by its Id")
    @PostMapping("/capture")
    public ResponseEntity<String> captureOrder(@RequestParam String orderId) {
        return ResponseEntity.ok(paypalService.captureOrder(orderId));
    }
}
