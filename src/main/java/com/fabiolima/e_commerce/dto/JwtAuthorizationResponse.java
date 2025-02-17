package com.fabiolima.e_commerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class JwtAuthorizationResponse {
    private String token;
    private long expiresIn;
    private Long userId;
    private String role;
}