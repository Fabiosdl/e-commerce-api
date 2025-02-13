package com.fabiolima.e_commerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class JwtAuthorizationResponse {
    private String token;
    private long expiresIn;
}
