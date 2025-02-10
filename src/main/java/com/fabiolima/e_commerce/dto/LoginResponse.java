package com.fabiolima.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO to send information about user and the basket created after login to the front end in a json format
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginResponse {

    private String message;
    private Long userId;
    private String role;
    private Long basketId;

}