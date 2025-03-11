package com.fabiolima.e_commerce.controller.admin;

import com.fabiolima.e_commerce.model.Role;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.model.enums.UserRole;
import com.fabiolima.e_commerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/{adminId}")
public class ManageUserDetailsController {

    private final UserService userService;

    public ManageUserDetailsController(UserService userService){
        this.userService = userService;
    }

    @Operation(summary = "Used to create a new Vendor")
    @PostMapping
    public ResponseEntity<User> createNewUser(@RequestBody @Valid User theUser){
        Role role = new Role();
        role.setName(UserRole.ROLE_VENDOR);
        theUser.addRoleToUser(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(theUser));
    }

    @Operation(summary = "Retrieve all users")
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(@RequestParam(defaultValue = "0") int pgNum,
                                                  @RequestParam(defaultValue = "50") int pgSize){
        return ResponseEntity.ok(userService.findAllUsers(pgNum, pgSize));
    }

    @Operation(summary = "Retrieve all active users")
    @GetMapping("/status")
    public ResponseEntity<Page<User>> getAllActiveUsers(@RequestParam(defaultValue = "0") int pgNum,
                                                        @RequestParam(defaultValue = "50") int pgSize,
                                                        @RequestParam("status") String status){

        return ResponseEntity.ok(userService.findAllUsersWithStatus(pgNum,pgSize,status));
    }

    @Operation(summary = "Add new role to user")
    @PostMapping("/{userId}/roles/add-role")
    public ResponseEntity<User> addRoleToUser(
            @PathVariable Long userId,
            @RequestParam String roleName
    ) {
        User updatedUser = userService.addRoleToUser(userId, roleName);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Remove user account")
    @DeleteMapping("/{userId}")
    public ResponseEntity<User> deleteUserByUserId(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.deactivateUserByUserId(userId));
    }
}
