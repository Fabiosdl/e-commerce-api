package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.model.enums.UserStatus;

import java.util.List;
import java.util.Map;

public interface UserService {

    User saveUser(User theUser);
    List<User> findAllUsers();
    List<User> findAllUsersWithStatus(UserStatus theStatus);
    User findUserByUserId(Long userId);
    User updateUserByUserId(Long userId, User updatedUser);
    User patchUpdateUserByUserId(Long userId, Map<String, Object> updates);
    User deactivateUserByUserId(Long userId); //delete user means to change data in column status to DISABLED
}
