package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.dto.RegistrationRequest;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody RegistrationRequest registrationRequest) {
        // Use the UserService to register the user
        try {
            User user = userService.registerUser(registrationRequest);
            return ResponseEntity.ok("User registered successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error registering user: " + e.getMessage());
        }
    }
}
