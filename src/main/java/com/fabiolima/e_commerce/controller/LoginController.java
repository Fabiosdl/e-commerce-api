package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Used for login. It uses user email as username")
    @PostMapping
    public ResponseEntity<String> loginUser(@RequestParam String email,
                                            @RequestParam String password) {
        //spring security uses header content-type = application/x-www-form-urlencoded, which requires the use of @RequestParam
        //in case of jwt, that uses header content-type = application/json, should use @RequestBody
        try {
            // Authenticate the user using the AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            return ResponseEntity.ok("User logged in successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials.");
        }
    }
}