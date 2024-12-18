package com.fabiolima.online_shop.paymentGateways;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaypalController {

    private final PaypalService paypalService;

//    @GetMapping("/")
//    public String home(){
//
//    }
}
