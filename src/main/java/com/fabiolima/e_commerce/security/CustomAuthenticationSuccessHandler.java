package com.fabiolima.e_commerce.security;

import java.io.IOException;
import java.util.Optional;

import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.repository.BasketRepository;
import com.fabiolima.e_commerce.repository.UserRepository;
import com.fabiolima.e_commerce.service.BasketService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * This class is to direct the url after tle login is successful
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final BasketService basketService;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository, BasketService basketService) {
        this.userRepository = userRepository;
        this.basketService = basketService;
    }

    @Override
    @Transactional // This ensures that when I retrieve the User, the baskets collection is initialized and no lazy loading is required.
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        System.out.println("In customAuthenticationSuccessHandler");

        String userName = authentication.getName();

        System.out.println("userName=" + userName);

        Optional<User> optional = userRepository.findByEmail(userName);
        if(optional.isEmpty())
            throw new NotFoundException(String.format("User with email %s not found.",userName));
        User theUser = optional.get();
        Long userId = theUser.getId();

        // now place in the session
        HttpSession session = request.getSession();
        session.setAttribute("user", theUser);

        // check what is the role of the user
        String redirectURL = "";

        if (theUser.getRoles().stream().anyMatch(role -> role.getName().toString().equalsIgnoreCase("ROLE_CUSTOMER"))) {

            redirectURL = "/user/"+userId;
            //creates a basket for the user
            //method that requires the Transactional wrap
            basketService.createBasketAndAddToUser(theUser);

        } else if (theUser.getRoles().stream().anyMatch(role -> role.getName().toString().equalsIgnoreCase("ROLE_ADMIN"))) {
            redirectURL = "/admin/dashboard";
        }

        // Default redirect if no roles match
        if (redirectURL.isEmpty()) {
            redirectURL = "/";
        }
        response.sendRedirect(request.getContextPath() + redirectURL);
    }
}