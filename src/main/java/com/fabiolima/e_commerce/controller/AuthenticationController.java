package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.dto.LoginRequest;
import com.fabiolima.e_commerce.dto.RegistrationRequest;
import com.fabiolima.e_commerce.entities.Basket;
import com.fabiolima.e_commerce.entities.User;
import com.fabiolima.e_commerce.dto.JwtAuthorizationResponse;
import com.fabiolima.e_commerce.security.JwtService;
import com.fabiolima.e_commerce.service.AuthenticationService;
import com.fabiolima.e_commerce.service.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    /**
     * Create a service class to store the logic for sign in and sign up. Only call the service in the controller.
     **/

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Used for login. It uses user email as username")
    @PostMapping("/login")
    public ResponseEntity<JwtAuthorizationResponse> loginUser(@Valid @RequestBody LoginRequest loginInput) {

        // Authenticate and retrieve the user
        JwtAuthorizationResponse jwtAuthorizationResponse = authenticationService.authenticateUser(loginInput);
        log.info("User id {}  has been authenticated", jwtAuthorizationResponse.getUserId());

        return ResponseEntity.ok(jwtAuthorizationResponse);
    }

    @Operation(summary = "Registers a new customer")
    @PostMapping("/signup")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegistrationRequest registrationInput) {

        User registeredUser = authenticationService.registerUser(registrationInput);
        log.info("User registered successfully: {}", registeredUser.getEmail());

        return ResponseEntity.ok(registeredUser);
    }
}