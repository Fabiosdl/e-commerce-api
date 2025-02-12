package com.fabiolima.e_commerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class BasicAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String incomingCredentials = request.getHeader("Authorization");

        if (incomingCredentials != null){
            log.info("🔑 Credentials Received From Client");
        } else {
            log.warn("⚠️ No Credentials Received From Client");
        }
        filterChain.doFilter(request, response);
    }
}
