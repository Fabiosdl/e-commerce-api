package com.fabiolima.e_commerce.service.implementation;

import com.fabiolima.e_commerce.dto.LoginRequest;
import com.fabiolima.e_commerce.dto.RegistrationRequest;
import com.fabiolima.e_commerce.exceptions.InvalidIdException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.exceptions.UniqueEmailException;
import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.Role;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.model.enums.UserRole;
import com.fabiolima.e_commerce.repository.RoleRepository;
import com.fabiolima.e_commerce.repository.UserRepository;
import com.fabiolima.e_commerce.service.AuthenticationService;
import com.fabiolima.e_commerce.service.BasketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final BasketService basketService;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,
                                     UserRepository userRepository,
                                     BasketService basketService,
                                     RoleRepository roleRepository,
                                     BCryptPasswordEncoder passwordEncoder) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.basketService = basketService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User authenticateUser(LoginRequest input) {

        String username = input.getUsername();
        String password = input.getPassword();

        //01- Authenticate the user with Authentication Manager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username,password));

        //02- SecurityContextHolder allows the application to know the user is authenticated.
        // It's being used in JwtAuthenticationFilter

        // Retrieve user and add a basket to it if it hasn't one
        Optional<User> result = userRepository.findByEmail(username);
        if(result.isEmpty())
            throw new NotFoundException(String.format("Cannot find user with email %s", username));

        User entityUser = result.get();
        Basket basket = basketService.createBasketAndAddToUser(entityUser);

        log.info("Basket id {} has been created for user id {}", basket.getId(),entityUser.getId());

        return entityUser;
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
