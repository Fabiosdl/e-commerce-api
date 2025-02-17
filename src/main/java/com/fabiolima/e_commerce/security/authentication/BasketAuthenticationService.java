package com.fabiolima.e_commerce.security.authentication;

import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.repository.BasketRepository;
import com.fabiolima.e_commerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class BasketAuthenticationService {

    private final UserRepository userRepository;
    private final BasketRepository basketRepository;

    public BasketAuthenticationService(UserRepository userRepository, BasketRepository basketRepository) {
        this.userRepository = userRepository;
        this.basketRepository = basketRepository;
    }

    public boolean isOwner(Long basketUrlId, Authentication authentication){

        //retrieve user from basket id
        Optional<User> expectedOptionalUser = userRepository.findByBaskets_Id(basketUrlId);
        if(expectedOptionalUser.isEmpty())
            throw new NotFoundException(String.format("User do not contain basket with id %d", basketUrlId));
        User expectedUser = expectedOptionalUser.get();

        //retrieve user from authentication
        String email = authentication.getName();

        Optional<User> optional = userRepository.findByEmail(email);
        if(optional.isEmpty())
            throw new NotFoundException(String.format("User with email %s not found.",email));
        User authenticatedUser = optional.get();

        log.info("Is basket id from authenticated user ? {}", expectedUser.getId().equals(authenticatedUser.getId()));

        //return if they are the same or not
        return expectedUser.getId().equals(authenticatedUser.getId());
    }
}