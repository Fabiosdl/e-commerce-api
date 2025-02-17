package com.fabiolima.e_commerce.security.authentication;

import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class UserAuthenticationService {

    private final UserRepository userRepository;

    public UserAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isOwner(Long urlId, Authentication authentication){
        String email = authentication.getName();

        Optional<User> optional = userRepository.findByEmail(email);
        if(optional.isEmpty())
            throw new NotFoundException(String.format("User with email %s not found.",email));

        User authenticatedUser = optional.get();

        Long authenticatedUserId = authenticatedUser.getId();

        log.info("Is user id from the url the same as the authenticated user ? {}", urlId.equals(authenticatedUser.getId()));

        return authenticatedUserId.equals(urlId);
    }
}
