package com.fabiolima.e_commerce.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class CsrfTokenAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String incomingCsrfToken = request.getHeader("X-XSRF-TOKEN");

        if (incomingCsrfToken != null) {
            log.info("📥 CSRF Token Received from Client: {}", incomingCsrfToken);
        } else {
            log.warn("⚠️ No CSRF Token Received in Request");
        }

        filterChain.doFilter(request, response);
    }
}
