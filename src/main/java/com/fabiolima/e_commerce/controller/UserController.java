package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.repository.UserRepository;
import com.fabiolima.e_commerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserController (UserService userService, UserRepository userRepository){
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<User> createNewUser(@RequestBody @Valid User theUser){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(theUser));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<User> getUserDashboard(Authentication authentication) {
        String email = authentication.getName();
        User authenticatedUser = userRepository.findByEmail(email);
        return ResponseEntity.ok(authenticatedUser); // Return user details to populate the dashboard
    }

    @GetMapping("/{userId}")
    @PreAuthorize("@userAuthenticationService.isOwner(#userId, authentication)")
    public ResponseEntity<User> getUserByUserId(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.findUserByUserId(userId));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("@userAuthenticationService.isOwner(#userId, authentication)")
    public ResponseEntity<User> updateUserByUserId(@RequestBody Map<String, Object> updates,
                                   @PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.patchUpdateUserByUserId(userId,updates));
    }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("@userAuthenticationService.isOwner(#userId, authentication)")
    public ResponseEntity<User> deactivateUserByUserId(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.deactivateUserByUserId(userId));
    }
}