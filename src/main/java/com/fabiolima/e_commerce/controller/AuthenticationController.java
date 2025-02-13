package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.dto.LoginRequest;
import com.fabiolima.e_commerce.dto.RegistrationRequest;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.dto.JwtAuthorizationResponse;
import com.fabiolima.e_commerce.security.JwtService;
import com.fabiolima.e_commerce.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, JwtService jwtService, UserDetailsService userDetailsService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Operation(summary = "Used for login. It uses user email as username")
    @PostMapping("/login")
    public ResponseEntity<JwtAuthorizationResponse> loginUser(@Valid @RequestBody LoginRequest loginInput) {

        //01 - Authenticate and retrieve the user
        User authenticatedUser = authenticationService.authenticateUser(loginInput);

        //02 - load the user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticatedUser.getEmail());

        //03 - generate token
        String jwtToken = jwtService.generateToken(userDetails);

        //04 - build and return the response with token and expiring date
        JwtAuthorizationResponse jwtAuthorizationResponse = new JwtAuthorizationResponse();
                jwtAuthorizationResponse.setToken(jwtToken);
                jwtAuthorizationResponse.setExpiresIn(jwtService.getExpirationTime());

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