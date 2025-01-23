package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.dto.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final AuthenticationManager authenticationManager;

    public LoginController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate the user using the AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            // Here, you can generate a JWT token if needed
            return ResponseEntity.ok("User logged in successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials.");
        }
    }
}
