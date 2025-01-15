package com.fabiolima.online_shop.service_implementation;

import com.fabiolima.online_shop.exceptions.BadRequestException;
import com.fabiolima.online_shop.exceptions.ForbiddenException;
import com.fabiolima.online_shop.exceptions.NotFoundException;
import com.fabiolima.online_shop.exceptions.UniqueEmailException;
import com.fabiolima.online_shop.model.Role;
import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.model.enums.UserRole;
import com.fabiolima.online_shop.model.enums.UserStatus;
import com.fabiolima.online_shop.repository.RoleRepository;
import com.fabiolima.online_shop.repository.UserRepository;
import com.fabiolima.online_shop.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.fabiolima.online_shop.model.enums.UserStatus.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public User saveUser(User theUser) {
        if(userRepository.existsByEmail(theUser.getEmail()))
            throw new UniqueEmailException("Email address already exist. Please use a new email.");
        return userRepository.save(theUser);
    }

    @Override
    public User addRoleToUser(Long userId, String roleName) {
        //fetch user
        User user = findUserByUserId(userId);

        //transform role name from string to enum
        UserRole name = UserRole.fromString(roleName);

        //fetch the role
        Role role = roleRepository.findByName(name);

        if (role == null) {
            throw new NotFoundException("Role not found");
        }

        //add role to user
        user.addRoleToUser(role);

        //save user
        return userRepository.save(user);
    }

    @Override
    public Page<User> findAllUsers(int pgNum, int pgSize) {
        Pageable pageable = PageRequest.of(pgNum,pgSize);
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> findAllUsersWithStatus(int pgNum, int pgSize, String status) {

        //validate and transform string into enum
        UserStatus theStatus = UserStatus.fromString(status);

        Pageable pageable = PageRequest.of(pgNum, pgSize);

        return userRepository.findAllByUserStatus(theStatus, pageable);
    }

    @Override
    public User findUserByUserId(Long userId){
        Optional<User> result = userRepository.findById(userId);
        if (result.isEmpty()) throw new NotFoundException("User not found");
        return result.get();
    }

    @Override
    @Transactional
    public User updateUserByUserId(Long userId, User updatedUser) {

        // first check if the user id in the parameter matches the user id in the body.
        if(!Objects.equals(updatedUser.getId(), userId))
            throw new BadRequestException("User id in the parameter does not " +
                    "match the user id in the body");

        // If the request is correct, find user. check if it exists; throw an error if it doesn't
        findUserByUserId(userId);

        // save the updated user data
        return userRepository.save(updatedUser);
    }

    @Override
    public User patchUpdateUserByUserId(Long userId, Map<String, Object> updates) {

        // Check if user exists; throw an error if it doesn't
        User theUser = findUserByUserId(userId);

        updates.forEach((field, value) -> {
            switch (field){
                case "name":
                    theUser.setName((String) value);
                    break;
                case "email":
                    theUser.setEmail((String) value);
                    break;
                case "password":
                    theUser.setPassword((String) value);
                    break;
                case "address":
                    theUser.setAddress((String) value);
                    break;
                default:
                    throw new ForbiddenException("Field not found or not allowed to update");

            }
        });
        return saveUser(theUser);
    }

    @Override
    public User deactivateUserByUserId(Long userId) {
        User theUser = findUserByUserId(userId);
        if(theUser.getUserStatus().equals(INACTIVE))
            throw new ForbiddenException("Cannot complete operation. User status is already INACTIVE");
        theUser.setUserStatus(INACTIVE);
        return saveUser(theUser);
    }

    @Override
    public User deleteUserById(Long userId) {
        User theUser = findUserByUserId(userId);
        userRepository.deleteById(userId);
        return theUser;
    }
}