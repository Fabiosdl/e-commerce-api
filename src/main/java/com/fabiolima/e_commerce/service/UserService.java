package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.dto.RegistrationRequest;
import com.fabiolima.e_commerce.model.User;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.UUID;

public interface UserService {

    User saveUser(User theUser);
    User addRoleToUser(UUID userId, String roleName);
    Page<User> findAllUsers(int pgNum, int pgSize);
    Page<User> findAllUsersWithStatus(int pgNum, int pgSize, String status);
    User findUserByUserId(UUID userId);
    User updateUserByUserId(UUID userId, User updatedUser);
    User patchUpdateUserByUserId(UUID userId, Map<String, Object> updates);
    User deactivateUserByUserId(UUID userId); //delete user means to change data in column status to DISABLED
    User deleteUserById(UUID userId); //method to be used in integration test only
}