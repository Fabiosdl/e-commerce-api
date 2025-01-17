package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController (UserService userService){
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
    //implement pagination

    @GetMapping("/status")
    public ResponseEntity<Page<User>> getAllActiveUsers(@RequestParam(defaultValue = "0") int pgNum,
                                                        @RequestParam(defaultValue = "50") int pgSize,
                                                        @RequestParam("status") String status){

        return ResponseEntity.ok(userService.findAllUsersWithStatus(pgNum,pgSize,status));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserByUserId(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.findUserByUserId(userId));
    }

    @PostMapping("/{userId}/roles/{roleName}")
    public ResponseEntity<User> addRoleToUser(
            @PathVariable Long userId,
            @PathVariable String roleName
    ) {
        User updatedUser = userService.addRoleToUser(userId, roleName);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<User> updateUserByUserId(@RequestBody Map<String, Object> updates,
                                   @PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.patchUpdateUserByUserId(userId,updates));
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<User> deactivateUserByUserId(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.deactivateUserByUserId(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<User> deleteUserByUserId(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.deactivateUserByUserId(userId));
    }

    //implement spring security
    //access control
    //
}