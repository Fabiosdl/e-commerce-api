package com.fabiolima.e_commerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class JwtAuthorizationResponse {
    private String token;
    private long expiresIn;
    private UUID userId;
    private String role;
}