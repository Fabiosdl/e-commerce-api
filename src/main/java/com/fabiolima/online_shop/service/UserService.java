package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.model.enums.UserStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface UserService {

    User saveUser(User theUser);
    Page<User> findAllUsers(int pgNum, int pgSize);
    Page<User> findAllUsersWithStatus(int pgNum, int pgSize, String status);
    User findUserByUserId(Long userId);
    User updateUserByUserId(Long userId, User updatedUser);
    User patchUpdateUserByUserId(Long userId, Map<String, Object> updates);
    User deactivateUserByUserId(Long userId); //delete user means to change data in column status to DISABLED
    User deleteUserById(Long userId); //method to be used in integration test only
}
