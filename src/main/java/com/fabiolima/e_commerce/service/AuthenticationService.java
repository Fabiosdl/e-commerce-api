package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.dto.JwtAuthorizationResponse;
import com.fabiolima.e_commerce.dto.LoginRequest;
import com.fabiolima.e_commerce.dto.RegistrationRequest;
import com.fabiolima.e_commerce.entities.User;

public interface AuthenticationService {

    JwtAuthorizationResponse authenticateUser(LoginRequest input);
    User registerUser(RegistrationRequest input);
}