package com.fabiolima.e_commerce.security;

import com.fabiolima.e_commerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity

public class SecurityConfig {

    private final UserRepository userRepository;
    private final CsrfTokenAuthenticationFilter csrfTokenAuthenticationFilter;

    @Autowired
    public SecurityConfig(UserRepository userRepository, CsrfTokenAuthenticationFilter csrfTokenAuthenticationFilter) {
        this.userRepository = userRepository;
        this.csrfTokenAuthenticationFilter = csrfTokenAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);//handle csrf token via cookies (For API based authentication) instead of storing it as default request attribute

        http
                .cors(Customizer.withDefaults())
                .csrf((csrf) -> csrf
                        .csrfTokenRepository(tokenRepository)
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers("/api/auth/register/**","/api/auth/login")
                )
                //.addFilterAfter(csrfTokenAuthenticationFilter, CsrfTokenAuthenticationFilter.class) // Log CSRF token after it is created
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)// if using JWT change to STATELESS
                )
                .authorizeHttpRequests( authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/swagger-ui/**","/v3/api-docs/**").permitAll()
                                .requestMatchers("/user/**", "/basket/**").hasRole("CUSTOMER")
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/product").permitAll()
                                .anyRequest().authenticated()
                );
        SecurityFilterChain chain = http.build();
        log.info("Configured security filter chain: {}",chain);
        return chain;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetails, BCryptPasswordEncoder passwordEncoder){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetails);
        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            // Custom service to load user by email (e-commerce logic)
            com.fabiolima.e_commerce.model.User theUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return new org.springframework.security.core.userdetails.User(
                    theUser.getEmail(),
                    theUser.getPassword(),
                    theUser.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName().toString()))
                        .toList());

        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:5173")); // Frontend URL
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        corsConfiguration.setAllowCredentials(true);  // Allows cookies/credentials
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "x-xsrf-token"));
        corsConfiguration.setExposedHeaders(List.of("Authorization")); // Allow frontend to read Authorization header

        // Ensure the CORS configuration is applied for all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

}
