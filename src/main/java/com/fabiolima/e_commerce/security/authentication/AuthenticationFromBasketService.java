package com.fabiolima.e_commerce.security.authentication;

import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.model.enums.BasketStatus;
import com.fabiolima.e_commerce.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFromBasketService {

    private final UserRepository userRepository;

    public AuthenticationFromBasketService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isOwner(Long basketUrlId, Authentication authentication){

        String email = authentication.getName();
        User authenticatedUser = userRepository.findByEmail(email);
        return authenticatedUser.getBaskets().stream()
                .anyMatch(b -> b.getId().equals(basketUrlId) && b.getBasketStatus() == BasketStatus.ACTIVE);
    }
}