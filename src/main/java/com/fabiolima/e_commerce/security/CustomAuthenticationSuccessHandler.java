package com.fabiolima.e_commerce.security;

import java.io.IOException;

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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        System.out.println("In customAuthenticationSuccessHandler");

        String userName = authentication.getName();

        System.out.println("userName=" + userName);

        User theUser = userRepository.findByEmail(userName);

        // now place in the session
        HttpSession session = request.getSession();
        session.setAttribute("user", theUser);

        // check what is the role of the user
        String redirectURL = "";

        if (theUser.getRoles().stream().anyMatch(role -> role.getName().toString().equalsIgnoreCase("ROLE_CUSTOMER"))) {
            Long userId = theUser.getId();
            redirectURL = "/user/"+userId;
            //creates a basket for the user
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