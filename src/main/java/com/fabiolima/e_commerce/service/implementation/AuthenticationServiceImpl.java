package com.fabiolima.e_commerce.service.implementation;

import com.fabiolima.e_commerce.dto.JwtAuthorizationResponse;
import com.fabiolima.e_commerce.dto.LoginRequest;
import com.fabiolima.e_commerce.dto.RegistrationRequest;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.exceptions.UniqueEmailException;
import com.fabiolima.e_commerce.entities.Basket;
import com.fabiolima.e_commerce.entities.Role;
import com.fabiolima.e_commerce.entities.User;
import com.fabiolima.e_commerce.entities.enums.UserRole;
import com.fabiolima.e_commerce.repository.RoleRepository;
import com.fabiolima.e_commerce.repository.UserRepository;
import com.fabiolima.e_commerce.security.JwtService;
import com.fabiolima.e_commerce.service.AuthenticationService;
import com.fabiolima.e_commerce.service.BasketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BasketService basketService;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtService jwtService,
                                     UserRepository userRepository,
                                     BasketService basketService,
                                     RoleRepository roleRepository,
                                     BCryptPasswordEncoder passwordEncoder) {

        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.basketService = basketService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public JwtAuthorizationResponse authenticateUser(LoginRequest input) {

        String username = input.getUsername();
        String password = input.getPassword();

        // Authenticate the user with Authentication Manager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username,password));

        // Retrieve authenticated user email
        String email = authentication.getName(); //API uses email for credential

        // Load the user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Generate token
        String jwtToken = jwtService.generateToken(userDetails);

        // Retrieve authenticated user and Add basket to they
        Optional<User> result = userRepository.findByEmail(email);
        if(result.isEmpty())
            throw new NotFoundException("User not found");
        User authenticatedUser = result.get();
        Basket basket = basketService.createBasketAndAddToUser(authenticatedUser);
        log.info("Basket id {} has been created for user id {}", basket.getId(),authenticatedUser.getId());

        // Build and return the response with token and expiring date
        JwtAuthorizationResponse jwtAuthorizationResponse = new JwtAuthorizationResponse();
        jwtAuthorizationResponse.setToken(jwtToken);
        jwtAuthorizationResponse.setExpiresIn(jwtService.getExpirationTime());
        jwtAuthorizationResponse.setUserId(authenticatedUser.getId());
        jwtAuthorizationResponse.setRole(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(("ROLE_CUSTOMER")::equals) // Extract the role string
                .findFirst()
                .orElse(null));

        return jwtAuthorizationResponse;
    }

    @Override
    public User registerUser(RegistrationRequest input) {

        //check if user already exists
        log.info("Does email exist? {}", userRepository.existsByEmail(input.getEmail()) );
        if(userRepository.existsByEmail(input.getEmail()))
            throw new UniqueEmailException("Email address already exist. Please use a new email.");

        //create and save new user
        String encryptedPassword = passwordEncoder.encode(input.getPassword());

        User user = new User();
        user.setName(input.getName());
        user.setEmail(input.getEmail());
        user.setPassword(encryptedPassword);

        Optional<Role> role = roleRepository.findByName(UserRole.ROLE_CUSTOMER);

        if(role.isEmpty()) {
            log.info("Role could not be found");
            throw new NotFoundException("Role not found");
        }
        user.addRoleToUser(role.get());

        return userRepository.save(user);
    }
}