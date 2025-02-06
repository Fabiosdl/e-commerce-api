package com.fabiolima.e_commerce.security;

import java.io.IOException;
import java.util.Optional;

import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.repository.UserRepository;
import com.fabiolima.e_commerce.service.BasketService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * This class is to direct the url after tle login is successful. It also conveniently creates a basket after user login
 */
@Slf4j
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

        String userName = authentication.getName();
        log.info("User {} has been successfully authenticated", userName);

        // Retrieve the user by email
        Optional<User> optional = userRepository.findByEmail(userName);
        if (optional.isEmpty()) {
            throw new NotFoundException(String.format("User with email %s not found.", userName));
        }
        User theUser = optional.get();
        Long userId = theUser.getId();

        // Place user in the session
        HttpSession session = request.getSession();
        session.setAttribute("user", theUser);

        // Create basket if user is a customer
        boolean isCustomer = theUser.getRoles().stream()
                .anyMatch(role -> role.getName().toString().equalsIgnoreCase("ROLE_CUSTOMER"));

        if (isCustomer) {
            basketService.createBasketAndAddToUser(theUser);
        }

        // Return response to frontend
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String jsonResponse = String.format(
                "{\"message\": \"Login successful\", \"userId\": %d, \"role\": \"%s\"}",
                userId,
                isCustomer ? "CUSTOMER" : "ADMIN"
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

}