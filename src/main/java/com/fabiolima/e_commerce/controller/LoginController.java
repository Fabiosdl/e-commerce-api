package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.service.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class LoginController {

    private final com.fabiolima.e_commerce.repository.UserRepository userRepository;
    private final BasketService basketService;

    @Autowired
    public LoginController( com.fabiolima.e_commerce.repository.UserRepository userRepository, BasketService basketService) {
        this.userRepository = userRepository;
        this.basketService = basketService;
    }

    @Operation(summary = "Used for login. It uses user email as username")
    @PostMapping("/api/auth/login")
    public ResponseEntity<Map<String, Object>> loginUser() {
        try {
            /// Retrieve the authenticated user from the SecurityContext.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

             /**After successful authentication, create a basket and get the user details
             passing them to the frontend*/
            com.fabiolima.e_commerce.model.User user = userRepository.findByEmail(email).orElseThrow();

            Basket basket = basketService.createBasketAndAddToUser(user);
            log.info("Basket with id: {} has been created to user id: {}",basket.getId(),user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("basketId", basket.getId());
            response.put("role", user.getRoles().stream().map(role -> role.getName().name()).findFirst());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials."));
        }
    }



}