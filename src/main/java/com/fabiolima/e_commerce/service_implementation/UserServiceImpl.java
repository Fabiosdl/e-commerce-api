package com.fabiolima.e_commerce.service_implementation;

import com.fabiolima.e_commerce.dto.RegistrationRequest;
import com.fabiolima.e_commerce.exceptions.*;
import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.Role;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.model.enums.BasketStatus;
import com.fabiolima.e_commerce.model.enums.UserRole;
import com.fabiolima.e_commerce.model.enums.UserStatus;
import com.fabiolima.e_commerce.repository.RoleRepository;
import com.fabiolima.e_commerce.repository.UserRepository;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.fabiolima.e_commerce.model.enums.UserStatus.*;

@Service
@Slf4j //logging framework slf4j vs log4j vs log4j2 vs logback logging level
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BasketService basketService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder, BasketService basketService){ //
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.basketService = basketService;
    }

    //generate a log file from my code
    @Override
    public User saveUser(User theUser) {
        log.info("user has been successfully created");
        return userRepository.save(theUser);

    }

    @Override
    public User registerUser(RegistrationRequest registrationRequest) {

        //check if user already exists
        if(userRepository.existsByEmail(registrationRequest.getEmail()))
            throw new UniqueEmailException("Email address already exist. Please use a new email.");

        //create and save new user

        String encryptedPassword = passwordEncoder.encode(registrationRequest.getPassword());

        User user = new User();
        user.setName(registrationRequest.getName());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(encryptedPassword);

        Optional<Role> role = roleRepository.findByName(UserRole.ROLE_CUSTOMER);

        if(role.isEmpty()) {
            log.info("Role could not be found");
            throw new NotFoundException("Role not found");
        }
        user.addRoleToUser(role.get());

        return userRepository.save(user);
    }

    @Override
    public User addRoleToUser(Long userId, String roleName) {
        //fetch user
        User user = findUserByUserId(userId);

        //transform role name from string to enum
        UserRole name = UserRole.fromString(roleName);

        //fetch the role
        Optional<Role> role = roleRepository.findByName(name);

        if (role.isEmpty()) {
            throw new NotFoundException("Role not found");
        }

        //add role to user
        user.addRoleToUser(role.get());

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
        //1- Retrieve the user
        User theUser = findUserByUserId(userId);

        //2-Check if the user is active
        if(theUser.getUserStatus().equals(INACTIVE))
            throw new ForbiddenException("Cannot complete operation. User status is already INACTIVE");

        //3-Deactivate active basket if any
        Optional<Basket> basket = theUser.getBaskets().stream()
                .filter(basket1 -> basket1.getBasketStatus().equals(BasketStatus.ACTIVE))
                .findFirst();
        basket.ifPresent(value -> basketService.deactivateBasketById(theUser.getId(), value.getId()));

        //4-Deactivate User
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