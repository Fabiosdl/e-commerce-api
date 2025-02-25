package com.fabiolima.e_commerce.security.authentication;

import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.repository.OrderRepository;
import com.fabiolima.e_commerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class OrderAuthenticationService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public OrderAuthenticationService(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public boolean isOwner(Long urlOrderId, Authentication authentication ){

        String email = authentication.getName();

        Optional<User> optional = userRepository.findByEmail(email);
        if(optional.isEmpty())
            throw new NotFoundException(String.format("User with email %s not found.",email));
        User authenticatedUser = optional.get();

        boolean orderBelongsToUser = orderRepository.existsByIdAndUserId(urlOrderId, authenticatedUser.getId());

        log.info("Does the order ID {} belong to the authenticated user {}? {}",
                urlOrderId, email, orderBelongsToUser);

        return orderBelongsToUser;
    }

}
