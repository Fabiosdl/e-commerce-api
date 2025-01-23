package com.fabiolima.e_commerce.controller.admin;

import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public class ManageUserDetailsController {

    private final UserService userService;

    public ManageUserDetailsController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createNewUser(@RequestBody @Valid User theUser){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(theUser));
    }

    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(@RequestParam(defaultValue = "0") int pgNum,
                                                  @RequestParam(defaultValue = "50") int pgSize){
        return ResponseEntity.ok(userService.findAllUsers(pgNum, pgSize));
    }

    @GetMapping("/status")
    public ResponseEntity<Page<User>> getAllActiveUsers(@RequestParam(defaultValue = "0") int pgNum,
                                                        @RequestParam(defaultValue = "50") int pgSize,
                                                        @RequestParam("status") String status){

        return ResponseEntity.ok(userService.findAllUsersWithStatus(pgNum,pgSize,status));
    }

    @PostMapping("/{userId}/roles/{roleName}")
    public ResponseEntity<User> addRoleToUser(
            @PathVariable Long userId,
            @PathVariable String roleName
    ) {
        User updatedUser = userService.addRoleToUser(userId, roleName);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<User> deleteUserByUserId(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.deactivateUserByUserId(userId));
    }
}
