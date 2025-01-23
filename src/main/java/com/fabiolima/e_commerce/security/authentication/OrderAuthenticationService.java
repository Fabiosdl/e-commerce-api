package com.fabiolima.e_commerce.security.authentication;

import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderAuthenticationService {

    private final UserRepository userRepository;

    public OrderAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isOwner(Long urlOrderId, Authentication authentication ){

        String email = authentication.getName();

        Optional<User> optional = userRepository.findByEmail(email);
        if(optional.isEmpty())
            throw new NotFoundException(String.format("User with email %s not found.",email));
        User authenticatedUser = optional.get();

        return authenticatedUser.getOrders().stream().anyMatch(order -> order.getId().equals(urlOrderId));
    }

}
