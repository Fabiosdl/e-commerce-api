package com.fabiolima.online_shop.controller;

import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.model.enums.UserStatus;
import com.fabiolima.online_shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public User createNewUser(@RequestBody User theUser){
        return userService.saveUser(theUser);
    }

    @GetMapping
    public List<User> getAllUsers(){
        return userService.findAllUsers();
    }

    @GetMapping("/active")
    public List<User> getAllActiveUsers(){
        return userService.findAllUsersWithStatus(UserStatus.ACTIVE);
    }

    @GetMapping("/inactive")
    public List<User> getAllInactiveUsers(){
        return userService.findAllUsersWithStatus(UserStatus.INACTIVE);
    }

    @GetMapping("/{userId}")
    public User getUserByUserId(@PathVariable Long userId){
        return userService.findUserByUserId(userId);
    }

    @PatchMapping("/{userId}")
    public User updateUserByUserId(@RequestBody Map<String, Object> updates,
                                   @PathVariable Long userId){
        return userService.patchUpdateUserByUserId(userId,updates);
    }
    @PatchMapping("/{userId}/deactivate")
    public User deactivateUserByUserId(@PathVariable Long userId){
        return userService.deactivateUserByUserId(userId);
    }

}
