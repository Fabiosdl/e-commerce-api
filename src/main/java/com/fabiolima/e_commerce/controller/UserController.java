package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.repository.UserRepository;
import com.fabiolima.e_commerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@Slf4j
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

    @Operation(summary = "Returns user's data")
    @GetMapping("/{userId}")
    @PreAuthorize("@userAuthenticationService.isOwner(#userId, authentication)")
    public ResponseEntity<User> getUserByUserId(@PathVariable("userId") Long userId){
        User user = userService.findUserByUserId(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user details")
    @PatchMapping("/{userId}")
    @PreAuthorize("@userAuthenticationService.isOwner(#userId, authentication)")
    public ResponseEntity<User> updateUserByUserId(@RequestBody Map<String, Object> updates,
                                   @PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.patchUpdateUserByUserId(userId,updates));
    }

    @Operation(summary = "Deactivate user, preserving its data")
    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("@userAuthenticationService.isOwner(#userId, authentication)")
    public ResponseEntity<User> deactivateUserByUserId(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.deactivateUserByUserId(userId));
    }

    @PostMapping("/{userId}/roles/add-role")
    public ResponseEntity<User> addRoleToUser(
            @PathVariable Long userId,
            @RequestParam String roleName
    ) {
        User updatedUser = userService.addRoleToUser(userId, roleName);
        return ResponseEntity.ok(updatedUser);
    }
}