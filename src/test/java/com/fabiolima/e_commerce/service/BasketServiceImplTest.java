package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.*;
import com.fabiolima.e_commerce.model.enums.BasketStatus;
import com.fabiolima.e_commerce.repository.BasketRepository;
import com.fabiolima.e_commerce.service_implementation.BasketServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class BasketServiceImplTest {

    @MockitoBean
    private BasketRepository basketRepository;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ProductService productService;

    @Autowired
    private BasketServiceImpl basketService;

    @Test
    @Transactional
    void saveBasketAndAddToUser_ShouldReturnBasketAddedToUser_WhenUserExists() {
        // given

        // Create a new User
        User user = new User();

        //Create new baskets
        Basket basket1 = new Basket();
        Basket basket2 = new Basket();
        Basket expectedBasket = new Basket();

        // Mocking the behavior of userService
        when(userService.findUserByUserId(anyLong())).thenReturn(user);

        // Simulate saving the User along with its associated Basket
        when(userService.saveUser(any(User.class))).thenAnswer(invocation -> {
            User capturedUser = invocation.getArgument(0);
            // adding a baskets to the list of baskets. The correct one should be the last one.
            capturedUser.addBasketToUser(basket1);
            capturedUser.addBasketToUser(basket2);
            capturedUser.addBasketToUser(expectedBasket);
            return capturedUser;  // Return the user with the updated Basket
        });

        // when
        Basket actualBasket = basketService.createBasketAndAddToUser(user);

        // then
        assertAll(
                () -> assertNotNull(actualBasket),
                () -> assertEquals(expectedBasket, actualBasket),  // Ensure the returned Basket is the same one added to the User
                () -> assertTrue(user.getBaskets().contains(expectedBasket))  // Ensure the Basket was added to the User's baskets
        );

        // Verify that userService.saveUser() was called exactly once
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).saveUser(userCaptor.capture());
    }
//
//    @Test
//    void getUserBaskets_ShouldReturnUsersListOfBasket_WhenUserExist() {
//        //given
//        User user = new User();
//        user.addBasketToUser(new Basket());
//        user.addBasketToUser(new Basket());
//
//        when(userService.findUserByUserId(anyLong())).thenReturn(user);
//        List<Basket> expected = user.getBaskets();
//
//        //when
//        List<Basket> actual = basketService.getUserBaskets(1L);
//
//        //then
//        assertEquals(expected,actual);
//        verify(userService,times(1)).findUserByUserId(anyLong());
//    }

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
    void getUserBasketById_ShouldThrowNotFoundException_WhenBasketDoesNotBelongToUser() {
        //given

        when(basketRepository.findBasketByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        //when
        Executable executable = () -> basketService.getUserBasketById(1L,1L);

        //then
        assertThrows(NotFoundException.class, executable);
        verify(basketRepository,times(1)).findBasketByIdAndUserId(anyLong(),anyLong());
    }

//    @Test
//    void checkOutBasket_ShouldReturnBasketStatusCheckedOut_WhenBasketStatusIsOpenAndBasketBelongsToUser() {
//        //given
//        Basket basket = new Basket();
//        basket.setBasketStatus(BasketStatus.ACTIVE);
//
//        when(basketRepository.findBasketByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(basket));
//        when(basketRepository.save(basket)).thenReturn(basket);
//
//        //when
//        TheOrder actual = basketService.convertBasketToOrder(1L,1L);
//
//        //then
//        assertEquals(BasketStatus.CHECKED_OUT,actual.getBasket().getBasketStatus());
//        verify(basketRepository,times(1)).findBasketByIdAndUserId(1L,1L);
//        verify(basketRepository,times(1)).save(basket);
//    }

    /*@Test
    void checkOutBasket_ShouldThrowForbiddenException_WhenBasketDoesntBelongToUser() {
        //given
        Basket basket = new Basket();
        basket.setBasketStatus(BasketStatus.CHECKED_OUT);

        when(basketRepository.findBasketByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        //when
        Executable executable = () -> basketService.convertBasketToOrder(1L,1L);

        //then
        assertThrows(NotFoundException.class,executable);
        verify(basketRepository,times(1)).findBasketByIdAndUserId(1L,1L);
    }

    @Test
    void checkOutBasket_ShouldThrowForbiddenException_WhenBasketStatusIsCheckedOut() {
        //given
        Basket basket = new Basket();
        basket.setBasketStatus(BasketStatus.CHECKED_OUT);

        when(basketRepository.findBasketByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(basket));

        //when
        Executable executable = () -> basketService.convertBasketToOrder(1L,1L);

        //then
        assertThrows(ForbiddenException.class,executable);
        verify(basketRepository,times(1)).findBasketByIdAndUserId(1L,1L);
    }*/

//    @Test
//    void deleteBasketById_ShouldDeleteBasket_WhenBasketIsEmptyAndBasketStatusIsOpen() {
//        //given
//        Basket basket = new Basket();
//        basket.setBasketStatus(BasketStatus.OPEN);
//
//        Product product1 = new Product();
//        product1.setStock(5);
//        Product product2 = new Product();
//        product2.setStock(3);
//
//        BasketItem item1 = new BasketItem();
//        item1.setQuantity(2);
//        item1.setProduct(product1);
//        BasketItem item2 = new BasketItem();
//        item2.setQuantity(4);
//
//        basket.addBasketItemToBasket(item1);
//        basket.addBasketItemToBasket(item2);
//
//        //mocking dependencies in deleteBasketById
//        when(basketRepository.findBasketByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(basket));
//        //mocking dependencies in clearBasket helper method
//        when(basketRepository.findById(anyLong())).thenReturn(Optional.of(basket));
//        //mocking dependencies in removeItemFromBasket helper method
//        //when(productService.saveProduct(any(Product.class))).thenReturn(product);
//
//        doNothing().when(basketRepository).deleteById(1L);
//        //when
//        basketService.deleteBasketById(1L,1L);
//
//        //then
//        verify(basketRepository,times(1)).findBasketByIdAndUserId(1L,1L);
//        verify(basketRepository,times(1)).deleteById(1L);
//    }

    @Test
    void deleteBasketById_ShouldThrowForbiddenException_WhenBasketStatusIsCheckedOut(){
        //given
        Basket basket = new Basket();
        basket.setBasketStatus(BasketStatus.CHECKED_OUT);

        when(basketRepository.findBasketByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(basket));

        //when
        Executable executable = () -> basketService.deactivateBasketById(1L,1L);

        //then
        assertThrows(ForbiddenException.class,executable);
        verify(basketRepository,times(1)).findBasketByIdAndUserId(1L,1L);
    }

    @Test
    void findBasketById_ShouldReturnBasketWithTheSameId_WhenIdDoesExist() {
        // given
        Long basketId = 1L;
        Basket expectedBasket = new Basket();
        expectedBasket.setId(basketId);
        when(basketRepository.findById(anyLong())).thenReturn(Optional.of(expectedBasket));

        // when
        Basket actualBasket = basketService.findBasketById(basketId);

        // then
        assertNotNull(actualBasket);
        assertEquals(expectedBasket, actualBasket);
        verify(basketRepository, times(1)).findById(basketId);

    }

    @Test
    void findBasketById_ShouldThrowNotFoundException_WhenIdDoesNotExist() {
        // given
        Long basketId = 1L;
        when(basketRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        Executable executable = () -> basketService.findBasketById(basketId);

        //then
        NotFoundException e = assertThrows(NotFoundException.class, executable);
        assertEquals("Basket with Id 1 not found", e.getMessage());
        verify(basketRepository, times(1)).findById(basketId);
    }

    /*@Test
    void clearBasket() {
        //given
        Basket basket = new Basket();
        basket.addBasketItemToBasket(new BasketItem());
        basket.addBasketItemToBasket(new BasketItem());

        when(basketRepository.findById(anyLong())).thenReturn(Optional.of(basket));
        when(basketRepository.save(basket)).thenReturn(basket);

        //when
        Basket result = basketService.clearBasket(1L);

        //then
        assertTrue((result.getBasketItems()).isEmpty());
        verify(basketRepository,times(1)).findById(1L);
        verify(basketRepository,times(1)).save(basket);
    }*/

    @Test
    void getTotalQuantity_ShouldReturnTotalItemQuantityInBasket() {
        //given
        Basket basket = new Basket();
        basket.addBasketItemToBasket(new BasketItem());
        basket.addBasketItemToBasket(new BasketItem());

        when(basketRepository.findById(anyLong())).thenReturn(Optional.of(basket));

        //when
        Integer result = basketService.getTotalQuantity(1L);

        //then
        assertEquals(2,result);
        verify(basketRepository,times(1)).findById(1L);
    }


    @Test
    void calculateTotalPrice_ShouldReturnTheTotalPriceOfBasket() {
        //given
        Basket basket = new Basket();
        basket.setId(1L);

        Product product1 = new Product();
        product1.setProductPrice(15.00);
        Product product2 = new Product();
        product2.setProductPrice(17.34);

        BasketItem item1 = new BasketItem();
        item1.setProduct(product1);
        item1.setQuantity(3);
        BasketItem item2 = new BasketItem();
        item2.setProduct(product2);
        item2.setQuantity(2);

        basket.addBasketItemToBasket(item1);
        basket.addBasketItemToBasket(item2);

        when(basketRepository.findById(anyLong())).thenReturn(Optional.of(basket));

        Double expected = 15.00 * 3 + 17.34 * 2;

        //when
        Double actual = basketService.calculateTotalPrice(1L);

        //then
        assertEquals(expected, actual);
        verify(basketRepository,times(1)).findById(1L);
    }

    @Test
    void validateAndFetchBasket_ShouldReturnBasket_WhenBasketBelongsToUser() {
        //given
        Basket basket = new Basket();

        when(basketRepository.findBasketByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(basket));

        //when
        Basket actual = basketService.validateAndFetchBasket(1L,1L);

        //then
        assertEquals(basket,actual);
        verify(basketRepository,times(1)).findBasketByIdAndUserId(1L,1L);

    }

    @Test
    void validateAndFetchBasket_ShouldThrowNotFoundException_WhenBasketDoesNotBelongToUser() {
        //given

        when(basketRepository.findBasketByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        //when
        Executable executable = () -> basketService.validateAndFetchBasket(1L,1L);

        //then
        assertThrows(NotFoundException.class, executable,"Basket with Id 1 does not belong to the user with Id 1.");
        verify(basketRepository,times(1)).findBasketByIdAndUserId(1L,1L);

    }
}