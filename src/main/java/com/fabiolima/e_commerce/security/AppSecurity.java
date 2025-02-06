package com.fabiolima.e_commerce.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class AppSecurity {

    //query to spring boot find the user/role customized table
    @Bean /// @Bean meaning it will be registered as a Spring bean and available for dependency injection.
    public UserDetailsManager userDetailsManager(DataSource dataSource){ //replace this method

        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);

        // Custom query to fetch user credentials & check if the user is active
        jdbcUserDetailsManager.setUsersByUsernameQuery(
                "SELECT email, password, status = 'ACTIVE' FROM user WHERE email = ?"
        );

        // Custom query to fetch user roles (supports multiple roles)
        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(
                "SELECT u.email, r.role FROM user u " +
                        "JOIN user_roles ur ON u.id = ur.user_id " +
                        "JOIN roles r ON ur.role_id = r.id " +
                        "WHERE u.email = ?"
        );

        return jdbcUserDetailsManager;
    }

    //AuthenticationManager bean
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        return authenticationManagerBuilder.build();
    }

    //bcrypt bean definition
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //authenticationProvider bean definition
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService); //set the custom user details service
        auth.setPasswordEncoder(passwordEncoder()); //set the password encoder - bcrypt
        return auth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationSuccessHandler customAuthenticationSuccessHandler) throws Exception {

        http
                .cors(cors -> cors.configure(http)) // Enable CORS at the security level
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // CSRF token handling
                        .ignoringRequestMatchers("/register/**","/login/**","/authenticate") // Disable CSRF for the /register endpoint
                )
                .authorizeHttpRequests( authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/swagger-ui/**","/v3/api-docs/**").permitAll()
                                .requestMatchers("/user/**").hasRole("CUSTOMER")
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/register/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(form ->
                        form
                                .loginPage("/login") //specify custom login page
                                .loginProcessingUrl("/authenticate") // This is where React app will send the credentials
                                .successHandler(customAuthenticationSuccessHandler)
                                .permitAll()
                )
                .logout(logout -> logout.permitAll()
                )
                // Enable session management and configure it to create a session cookie
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Default is 'IF_REQUIRED'
                        .invalidSessionUrl("/login") // Redirect if session is invalid
                        .maximumSessions(1) // Limit to 1 session per user (optional)
                        .expiredUrl("/login") // Redirect to login if session expired
                )
        ;

        return http.build();
    }
}