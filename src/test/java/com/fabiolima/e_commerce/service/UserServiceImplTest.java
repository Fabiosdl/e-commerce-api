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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.fabiolima.e_commerce.model.enums.UserStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest

class UserServiceImplTest {

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;


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

    @Test
    void findAllUsers_ShouldReturnListOfUsers() {

        //GIVEN
        int pgNum = 0;
        int pgSize = 2;

        List<User> userList = List.of(
                User.builder().id(1L).userStatus(ACTIVE).build(),
                User.builder().id(2L).userStatus(ACTIVE).build(),
                User.builder().id(3L).userStatus(UserStatus.INACTIVE).build()
        );

        Pageable pageable = PageRequest.of(pgNum, pgSize);
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        //WHEN
        Page<User> result = userService.findAllUsers(pgNum,pgSize);

        //THEN
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(3L, result.getContent().get(2).getId());

        verify(userRepository,times(1)).findAll(pageable);
    }


    @Test
    void findAllUsersWithStatus_ShouldReturnListOfInactiveUsers() {
        //GIVEN
        int pgNum = 0;
        int pgSize = 2;

        List<User> userList = List.of(
                User.builder().id(1L).userStatus(ACTIVE).build(),
                User.builder().id(2L).userStatus(ACTIVE).build(),
                User.builder().id(3L).userStatus(UserStatus.INACTIVE).build()
        );

        List<User> statusList = userList.stream()
                .filter(user -> user.getUserStatus().equals(ACTIVE))
                .toList();

        Pageable pageable = PageRequest.of(pgNum, pgSize);
        Page<User> userPage = new PageImpl<>(statusList, pageable, statusList.size());
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        //WHEN
        Page<User> result = userService.findAllUsers(pgNum,pgSize);

        //THEN
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertThat(result)
                .allSatisfy(user -> user.getUserStatus().equals(ACTIVE));

        verify(userRepository,times(1)).findAll(pageable);
    }

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
        user.setUserStatus(ACTIVE);

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