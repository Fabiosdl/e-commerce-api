package com.fabiolima.e_commerce.security.authentication;

import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserAuthenticationService {

    private final UserRepository userRepository;

    public UserAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isOwner(Long urlId, Authentication authentication){
        String email = authentication.getName();
        User authenticatedUser = userRepository.findByEmail(email);
        Long authenticatedUserId = authenticatedUser.getId();

        return authenticatedUserId.equals(urlId);
    }
}
