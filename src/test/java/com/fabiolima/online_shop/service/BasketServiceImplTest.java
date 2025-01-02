package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.repository.BasketRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class BasketServiceImplTest {

    @MockitoBean
    private BasketRepository basketRepository;
    @MockitoBean
    private UserService userService;

    @Autowired
    private BasketServiceImpl basketService;

    @Test
    @Transactional
    void saveBasketAndAddToUser() {
        // given
        User user = new User();  // Create a new User
        Basket basket = new Basket();  // Create a new Basket
        basket.setId(1L);

        // Mocking the behavior of userService
        when(userService.findUserByUserId(anyLong())).thenReturn(user);

        // Simulate saving the User along with its associated Basket
        when(userService.saveUser(any(User.class))).thenAnswer(invocation -> {
            User capturedUser = invocation.getArgument(0);
            // Manually ensuring that the basket is related to the user and saved
            capturedUser.addBasketToUser(basket);
            return capturedUser;  // Return the user with the updated Basket
        });

        // when
        Basket actual = basketService.saveBasketAndAddToUser(1L);

        // then
        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(basket, actual),  // Ensure the returned Basket is the same one added to the User
                () -> assertTrue(user.getBaskets().contains(basket))  // Ensure the Basket was added to the User's baskets
        );

        // Verify that userService.saveUser() was called exactly once
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).saveUser(userCaptor.capture());
    }

    @Test
    void getUserBaskets_ShouldReturnUsersListOfBasket_WhenUserExist() {
        //given
        User user = new User();
        user.addBasketToUser(new Basket());
        user.addBasketToUser(new Basket());

        when(userService.findUserByUserId(anyLong())).thenReturn(user);
        List<Basket> expected = user.getBaskets();

        //when
        List<Basket> actual = basketService.getUserBaskets(1L);

        //then
        assertEquals(expected,actual);
        verify(userService,times(1)).findUserByUserId(anyLong());
    }

    @Test
    void getUserBasketById_ShouldReturnUsersBasket() {
        //given
        Basket basket = new Basket();

        when(basketRepository.findBasketByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(basket));

        //when
        Basket actual = basketService.getUserBasketById(1L,1L);

        //then
        assertEquals(basket,actual);
        verify(basketRepository,times(1)).findBasketByIdAndUserId(anyLong(),anyLong());
    }

    @Test
    void checkOutBasket() {
    }

    @Test
    void deleteBasketById() {
    }

    @Test
    void findBasketById() {
    }

    @Test
    void clearBasket() {
    }

    @Test
    void getTotalQuantity() {
    }

    @Test
    void calculateTotalPrice() {
    }
}