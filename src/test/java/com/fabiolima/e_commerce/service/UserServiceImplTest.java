package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.model.enums.UserStatus;
import com.fabiolima.e_commerce.repository.UserRepository;
import com.fabiolima.e_commerce.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest

class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService;

    @MockitoBean
    private UserRepository userRepository;

    /**
     * The set up above is the same as:
     *
     *     private UserRepository userRepository;
     *     private UserServiceImpl userService;
     *
     *     @BeforeEach
     *     void setUp() {
     *         userRepository = mock(UserRepository.class);
     *         userService = new UserServiceImpl(userRepository);
     *     }
     */

    @Test
    void saveUser_ShouldReturnSavedUser() {
        //GIVEN
        User user = new User();
        user.setId(1L);
        user.setName("Joe Doe");

        when(userRepository.save(user)).thenReturn(user);

        //WHEN
        User savedUser = userService.saveUser(user);

        //THEN
        assertEquals(user, savedUser);
        verify(userRepository, times(1)).save(user);
    }

//    @Test
//    void findAllUsers_ShouldReturnListOfUsers() {
//
//        //GIVEN
//        List<User> users = List.of(new User(), new User());
//        when(userRepository.findAll()).thenReturn(users);
//
//        //WHEN
//        List<User> result = userService.findAllUsers();
//
//        //THEN
//        assertEquals(users,result);
//        verify(userRepository,times(1)).findAll();
//    }

//    @Test
//    void findAllUsersWithStatus_ShouldReturnListOfActiveUsers() {
//        //GIVEN
//        User activeUser1 = new User();
//        activeUser1.setId(1L);
//        activeUser1.setUserStatus(UserStatus.ACTIVE);
//
//        User activeUser2 = new User();
//        activeUser2.setId(2L);
//        activeUser2.setUserStatus(UserStatus.ACTIVE);
//
//        User inactiveUser = new User();
//        inactiveUser.setId(3L);
//        inactiveUser.setUserStatus(UserStatus.INACTIVE);
//
//        List<User> users = List.of(activeUser1,activeUser2,inactiveUser);
//        List<User> expected = List.of(activeUser1,activeUser2);
//
//        when(userRepository.findAll()).thenReturn(users);
//
//        //WHEN
//        List<User> actual = userService.findAllUsersWithStatus(UserStatus.ACTIVE);
//
//        //THEN
//        assertEquals(expected,actual);
//        verify(userRepository,times(1)).findAll();
//    }
//
//    @Test
//    void findAllUsersWithStatus_ShouldReturnListOfInactiveUsers() {
//        //GIVEN
//        User inactiveUser1 = new User();
//        inactiveUser1.setId(1L);
//        inactiveUser1.setUserStatus(UserStatus.INACTIVE);
//
//        User inactiveUser2 = new User();
//        inactiveUser2.setId(2L);
//        inactiveUser2.setUserStatus(UserStatus.INACTIVE);
//
//        User activeUser = new User();
//        activeUser.setId(3L);
//        activeUser.setUserStatus(UserStatus.ACTIVE);
//
//        List<User> users = List.of(inactiveUser1,inactiveUser2,activeUser);
//        List<User> expected = List.of(inactiveUser1, inactiveUser2);
//
//        when(userRepository.findAll()).thenReturn(users);
//
//        //WHEN
//        List<User> actual = userService.findAllUsersWithStatus(UserStatus.INACTIVE);
//
//        //THEN
//        assertEquals(expected,actual);
//        verify(userRepository,times(1)).findAll();
//    }

    @Test
    void findUserByUserId_ShouldReturnUser_WhenUserExists() {

        //GIVEN
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        //WHEN
        User result = userService.findUserByUserId(1L);

        //THEN
        assertEquals(user, result);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void findUserByUserID_ShouldThrowNotFoundException_WhenUserDoesNotExist() {

        //GIVEN
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        //WHEN
        Executable executable = () -> userService.findUserByUserId(2L);

        //THEN
        assertThrows(NotFoundException.class, executable);

        //to verify that the repository was called with the correct id
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void updateUserByUserId_ShouldReturnUpdatedUser() {

        //GIVEN
        User user = new User();
        user.setId(1L);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Serrano Suarez");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);

        //WHEN

        User actual = userService.updateUserByUserId(1L, updatedUser);

        //THEN
        assertEquals(updatedUser, actual);
        verify(userRepository,times(1)).save(updatedUser);

    }

    @Test
    void patchUpdateUserByUserId_ShouldReturnPatchedUser() {
        //GIVEN
        User user = new User();
        user.setId(1L);
        user.setName("Jamón Suarez");

        HashMap<String,Object> map = new HashMap<>();
        String username = "Serrano Suarez";
        map.put("name", username);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        //WHEN
        String expected = username;
        User actual = userService.patchUpdateUserByUserId(1L, map);

        //THEN
        assertEquals(expected, actual.getName());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void patchUpdateUserByUserId_ShouldReturnException_WhenUpdateFieldNotFound() {
        //GIVEN
        User user = new User();
        user.setId(1L);

        HashMap<String, Object> map = new HashMap<>();
        map.put("test", "TEST");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        //I won't mock the userRepository.save() because it's in the return statement and
        //the exception will be launched before reach it.

        //WHEN
        Executable executable = () -> userService.patchUpdateUserByUserId(1L, map);

        //THEN
        assertThrows(ForbiddenException.class, executable);
    }

    @Test
    void deactivateUserByUserId_ShouldSaveUserWithInactiveStatus() {
        //GIVEN
        User user = new User();
        user.setId(1L);
        user.setUserStatus(UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        //WHEN
        User actual = userService.deactivateUserByUserId(1L);

        //THEN
        assertEquals(UserStatus.INACTIVE, actual.getUserStatus());

    }

    @Test
    void deactivateUserByUserId_ShouldThrowForbiddenException_WhenUserIsInactive() {
        //GIVEN
        User user = new User();
        user.setId(1L);
        user.setUserStatus(UserStatus.INACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        //WHEN
        Executable executable = () -> userService.deactivateUserByUserId(1L);

        //THEN
        assertThrows(ForbiddenException.class, executable);

    }

    @Test
    void deleteUserById_ShouldDeleteUser(){
        //GIVEN
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);

        //WHEN
        userService.deleteUserById(1L);

        //THEN
        verify(userRepository,times(1)).deleteById(1L);
    }
}