package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.dto.LoginRequest;
import com.fabiolima.e_commerce.dto.RegistrationRequest;
import com.fabiolima.e_commerce.entities.User;

public interface AuthenticationService {

    User authenticateUser(LoginRequest input);
    User registerUser(RegistrationRequest input);
}