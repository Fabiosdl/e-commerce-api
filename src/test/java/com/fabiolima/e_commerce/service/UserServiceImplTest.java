package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.entities.User;
import com.fabiolima.e_commerce.entities.enums.UserStatus;
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
import java.util.UUID;

import static com.fabiolima.e_commerce.entities.enums.UserStatus.ACTIVE;
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
        user.setId(UUID.randomUUID());
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

        User user1 = User.builder().id(UUID.randomUUID()).userStatus(ACTIVE).build();
        User user2 = User.builder().id(UUID.randomUUID()).userStatus(ACTIVE).build();
        User user3 = User.builder().id(UUID.randomUUID()).userStatus(UserStatus.INACTIVE).build();

        List<User> userList = List.of(user1, user2, user3);

        Pageable pageable = PageRequest.of(pgNum, pgSize);
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        //WHEN
        Page<User> result = userService.findAllUsers(pgNum,pgSize);

        //THEN
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals(user1.getId(), result.getContent().get(0).getId());
        assertEquals(user3.getId(), result.getContent().get(2).getId());

        verify(userRepository,times(1)).findAll(pageable);
    }


    @Test
    void findAllUsersWithStatus_ShouldReturnListOfInactiveUsers() {
        //GIVEN
        int pgNum = 0;
        int pgSize = 2;

        User user1 = User.builder().id(UUID.randomUUID()).userStatus(ACTIVE).build();
        User user2 = User.builder().id(UUID.randomUUID()).userStatus(ACTIVE).build();
        User user3 = User.builder().id(UUID.randomUUID()).userStatus(UserStatus.INACTIVE).build();

        List<User> userList = List.of(user1, user2, user3);

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
        user.setId(UUID.randomUUID());

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        //WHEN
        User result = userService.findUserByUserId(user.getId());

        //THEN
        assertEquals(user, result);
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    void findUserByUserID_ShouldThrowNotFoundException_WhenUserDoesNotExist() {

        //GIVEN
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //WHEN
        Executable executable = () -> userService.findUserByUserId(userId);

        //THEN
        assertThrows(NotFoundException.class, executable);

        //to verify that the repository was called with the correct id
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void updateUserByUserId_ShouldReturnUpdatedUser() {

        //GIVEN
        User user = new User();
        user.setId(UUID.randomUUID());

        User updatedUser = new User();
        updatedUser.setId(UUID.randomUUID());
        updatedUser.setName("Serrano Suarez");

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);

        //WHEN

        User actual = userService.updateUserByUserId(user.getId(), updatedUser);

        //THEN
        assertEquals(updatedUser, actual);
        verify(userRepository,times(1)).save(updatedUser);

    }

    @Test
    void patchUpdateUserByUserId_ShouldReturnPatchedUser() {
        //GIVEN
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Jamón Suarez");

        HashMap<String,Object> map = new HashMap<>();
        String username = "Serrano Suarez";
        map.put("name", username);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        //WHEN
        String expected = username;
        User actual = userService.patchUpdateUserByUserId(user.getId(), map);

        //THEN
        assertEquals(expected, actual.getName());
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void patchUpdateUserByUserId_ShouldReturnException_WhenUpdateFieldNotFound() {
        //GIVEN
        User user = new User();
        user.setId(UUID.randomUUID());

        HashMap<String, Object> map = new HashMap<>();
        map.put("test", "TEST");

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        //I won't mock the userRepository.save() because it's in the return statement and
        //the exception will be launched before reach it.

        //WHEN
        Executable executable = () -> userService.patchUpdateUserByUserId(user.getId(), map);

        //THEN
        assertThrows(ForbiddenException.class, executable);
    }

    @Test
    void deactivateUserByUserId_ShouldSaveUserWithInactiveStatus() {
        //GIVEN
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserStatus(ACTIVE);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        //WHEN
        User actual = userService.deactivateUserByUserId(user.getId());

        //THEN
        assertEquals(UserStatus.INACTIVE, actual.getUserStatus());

    }

    @Test
    void deactivateUserByUserId_ShouldThrowForbiddenException_WhenUserIsInactive() {
        //GIVEN
        User user = new User();
        user.setId(user.getId());
        user.setUserStatus(UserStatus.INACTIVE);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        //WHEN
        Executable executable = () -> userService.deactivateUserByUserId(user.getId());

        //THEN
        assertThrows(ForbiddenException.class, executable);

    }

    @Test
    void deleteUserById_ShouldDeleteUser(){
        //GIVEN
        User user = new User();
        user.setId(UUID.randomUUID());

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(user.getId());

        //WHEN
        userService.deleteUserById(user.getId());

        //THEN
        verify(userRepository,times(1)).deleteById(user.getId());
    }
}