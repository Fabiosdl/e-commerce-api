package com.fabiolima.e_commerce.security;

import com.fabiolima.e_commerce.repository.UserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
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
                .addFilterAfter(logCsrfTokenFilter(), CsrfFilter.class) // Log CSRF token after it is created
                .addFilterBefore(logIncomingCsrfTokenFilter(), CsrfFilter.class)
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)// if using JWT change to STATELESS
//                )
                .authorizeHttpRequests( authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/swagger-ui/**","/v3/api-docs/**").permitAll()
                                .requestMatchers("/user/**", "/basket/**").hasRole("CUSTOMER")
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/product").permitAll()
                                .anyRequest().authenticated()
                ).formLogin(form -> form.loginPage("/api/auth/login").permitAll());

        SecurityFilterChain chain = http.build();
        log.info("Configured security filter chain: {}",chain);
        return chain;
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


    /// Below are 2 methods to check if the csrf token was sent correctly and received correctly
    @Bean
    public Filter logIncomingCsrfTokenFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain filterChain)
                    throws IOException, jakarta.servlet.ServletException {

                String incomingCsrfToken = request.getHeader("X-XSRF-TOKEN");

                if (incomingCsrfToken != null) {
                    log.info("📥 CSRF Token Received from Client: {}", incomingCsrfToken);
                } else {
                    log.warn("⚠️ No CSRF Token Received in Request");
                }

                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public Filter logCsrfTokenFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain filterChain)
                    throws IOException, jakarta.servlet.ServletException {

                CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                if (csrfToken != null) {
                    log.info("🔑 CSRF Token Sent to Client: {}", csrfToken.getToken());
                } else {
                    log.warn("⚠️ No CSRF Token Found in Request");
                }

                filterChain.doFilter(request, response);
            }
        };
    }
}
