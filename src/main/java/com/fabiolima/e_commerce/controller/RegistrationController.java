package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.dto.RegistrationRequest;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {

        this.userService = userService;
    }

    @Operation(summary = "Registers a new customer")
    @PostMapping("/api/auth/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        // Use the UserService to register the user
        try {
            User user = userService.registerUser(registrationRequest);
            log.info("User registered successfully: {}", user.getEmail());
            return ResponseEntity.ok(Map.of("message", "User registered successfully."));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error registering user: " + e.getMessage()));
        }
    }
}