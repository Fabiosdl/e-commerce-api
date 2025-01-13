package com.fabiolima.online_shop.controller;

import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.model.enums.UserStatus;
import com.fabiolima.online_shop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    //@GetMapping
    //public ResponseEntity<List<User>> getAllUsers(){
        //return ResponseEntity.ok(userService.findAllUsers());
    //}
    //implement pagination

    @GetMapping("/active")
    public ResponseEntity<List<User>> getAllActiveUsers(){
        return ResponseEntity.ok(userService.findAllUsersWithStatus(UserStatus.ACTIVE));
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<User>> getAllInactiveUsers(){
        return ResponseEntity.ok(userService.findAllUsersWithStatus(UserStatus.INACTIVE));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(userService.findUserByUserId(userId));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<User> updateUserByUserId(@RequestBody Map<String, Object> updates,
                                   @PathVariable Long userId){
        return ResponseEntity.ok(userService.patchUpdateUserByUserId(userId,updates));
    }
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<User> deactivateUserByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(userService.deactivateUserByUserId(userId));
    }
    @DeleteMapping("/{userId}")
    public ResponseEntity<User> deleteUserByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(userService.deactivateUserByUserId(userId));
    }

    //implement spring security
    //access control
    //
}