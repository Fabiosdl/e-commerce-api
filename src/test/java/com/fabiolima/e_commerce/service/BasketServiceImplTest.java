package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.InvalidQuantityException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.entities.*;
import com.fabiolima.e_commerce.entities.enums.BasketStatus;
import com.fabiolima.e_commerce.repository.BasketRepository;
import com.fabiolima.e_commerce.service.implementation.BasketServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

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
    void createBasketAndAddToUser_ShouldReturnNewBasketAddedToUser_WhenUserHasINACTIVEAndCHECKED_OUTBaskets() {
        // given

        // Create a new User
        User user = new User();

        //Create new baskets
        Basket basket1 = new Basket();
        basket1.setBasketStatus(BasketStatus.INACTIVE);
        Basket basket2 = new Basket();
        basket2.setBasketStatus(BasketStatus.CHECKED_OUT);

        // Mocking the behavior of basketRepository
        when(basketRepository.findActiveBasketByUserId(any(), eq(BasketStatus.ACTIVE)))
                .thenReturn(Optional.empty());

        // Simulate saving the User along with its associated Basket
        when(basketRepository.save(any(Basket.class))).thenAnswer(invocation -> {
            // adding a new active basket to the user.
            Basket argumentBasket = invocation.getArgument(0);
            argumentBasket.setId(UUID.randomUUID());//simulate DB creating an order
            return argumentBasket;
        });

        // when
        Basket actualBasket = basketService.createBasketAndAddToUser(user);

        // then

        assertAll(
                () -> assertNotNull(actualBasket),
                () -> assertEquals(BasketStatus.ACTIVE, actualBasket.getBasketStatus()), // Ensure new basket is ACTIVE
                () -> assertTrue(user.getBaskets().contains(actualBasket)) // Ensure the Basket is in the User's list
        );
    }

    @Test
    @Transactional
    void createBasketAndAddToUser_ShouldReturnUserExistingBasket_WhenBasketIsACTIVE() {
        // GIVEN

        // Create a new User
        User user = new User();
        user.setId(UUID.randomUUID());

        //Create active basket to insert in user
        Basket basket = new Basket();
        basket.setId(UUID.randomUUID());
        basket.setBasketStatus(BasketStatus.ACTIVE);

        user.addBasketToUser(basket);

        when(basketRepository.findActiveBasketByUserId(any(), eq(BasketStatus.ACTIVE)))
                .thenReturn(Optional.of(basket));

        //WHEN
        Basket actualBasket = basketService.createBasketAndAddToUser(user);

        //THEN
        assertNotNull(actualBasket);
        assertEquals(basket, actualBasket); //confirming that actualBasket already exists with user
        assertTrue(user.getBaskets().contains(actualBasket));  // Ensure the Basket was added to the User's baskets

    }

    @Test
    void getUserBaskets_ShouldReturnPaginatedBaskets_WhenUserHasBaskets() {
        // given
        UUID userId = UUID.randomUUID();
        int pageNumber = 0;
        int pageSize = 10;

        // Create a list of Basket objects to check if the method is returning in descending order
        Basket basket1 = new Basket();
        basket1.setCreatedAt(LocalDateTime.now().minusDays(1));
        Basket basket2 = new Basket();
        basket2.setCreatedAt(LocalDateTime.now());
        Basket basket3 = new Basket();
        basket3.setCreatedAt(LocalDateTime.now().minusDays(2));

        // Mock Pageable to return baskets sorted by createdAt in descending order
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Create a Page object containing the baskets
        Page<Basket> page = new PageImpl<>(List.of(basket1, basket2, basket3), pageable, 3);

        // Mock the repository
        when(basketRepository.findAllByUserId(userId, pageable))
                .thenReturn(page);

        // when
        Page<Basket> result = basketService.getUserBaskets(pageNumber, pageSize, userId);

        // then
        assertNotNull(result);  // Ensure the result is not null
        assertEquals(3, result.getTotalElements());  // Ensure the total number of elements is correct
        assertEquals(1, result.getTotalPages());  // Ensure the total number of pages is correct
        assertEquals(3, result.getContent().size());
        assertEquals(basket1, result.getContent().getFirst());
        assertEquals(basket2, result.getContent().get(1));
        assertEquals(basket3, result.getContent().get(2));
    }

    @Test
    void deactivateBasket_ShouldReturnBasketStatusInactive_WhenBasketStatusIsActive(){
        //Given
        UUID basketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        Basket basket = new Basket();
        basket.setId(basketId);
        basket.setBasketStatus(BasketStatus.ACTIVE);
        user.addBasketToUser(basket);

        when(basketRepository.findById(any()))
                .thenReturn(Optional.of(basket));
        when(basketRepository.save(any(Basket.class)))
                .thenAnswer(invocation -> invocation.<Basket>getArgument(0));

        //When

        Basket actualBasket = basketService.deactivateBasketById(userId,basketId);

        //Then
        assertNotNull(actualBasket);
        assertEquals(BasketStatus.INACTIVE, actualBasket.getBasketStatus());

    }

    @ParameterizedTest
    @CsvSource({"INACTIVE", "CHECKED_OUT"})
    void deactivateBasket_ShouldThrowForbiddenException_WhenBasketStatusIsNOTActive(String input){
        //Given
        BasketStatus status = null;
        if(input.equalsIgnoreCase("INACTIVE")){
            status = BasketStatus.INACTIVE;
        } else{
            status = BasketStatus.CHECKED_OUT;
        }

        UUID basketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Basket basket = new Basket();
        basket.setId(basketId);
        basket.setBasketStatus(status);

        when(basketRepository.findById(any()))
                .thenReturn(Optional.of(basket));

        //When

        Executable executable = () -> basketService.deactivateBasketById(userId, basketId);

        //Then
        assertThrows(ForbiddenException.class, executable, "Only an ACTIVE basket can be deactivated.");

    }

    @Test
    void clearBasket_ShouldReturnBasketWithNoItemsAndRestockProduct_WhenBasketIsActive() {

        // Given
        UUID basketId = UUID.randomUUID();
        Basket basket = new Basket();
        basket.setId(basketId);
        basket.setBasketStatus(BasketStatus.ACTIVE);

        Product product = new Product();
        product.setStock(10);

        BasketItem item = new BasketItem();
        item.setProduct(product);
        item.setQuantity(2);
        basket.addBasketItemToBasket(item);

        when(basketRepository.findById(any())).thenReturn(Optional.of(basket));
        doAnswer(invocation -> {
            int stock = product.getStock();
            product.setStock(stock + item.getQuantity());
            return null;
        }).when(productService).updateProductStock(any(Product.class), anyInt()); // Mock product service

        // When
        basketService.clearBasket(basket.getId());  // Call the method

        // Then
        assertEquals(0, basket.getBasketItems().size());  // Basket should have no items after clearing
        assertEquals(12, product.getStock());  // Stock should be updated (10 + 2)

        verify(productService, times(1)).updateProductStock(product, -2);
    }

    @ParameterizedTest
    @CsvSource({"INACTIVE", "CHECKED_OUT"})
    void clearBasket_ShouldThrowForbiddenException_WhenBasketStatusIsNOTActive(String input){
        //Given
        BasketStatus status = null;
        if(input.equalsIgnoreCase("INACTIVE")){
            status = BasketStatus.INACTIVE;
        } else{
            status = BasketStatus.CHECKED_OUT;
        }

        UUID basketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Basket basket = new Basket();
        basket.setId(basketId);
        basket.setBasketStatus(status);

        when(basketRepository.findById(any()))
                .thenReturn(Optional.of(basket));

        //When

        Executable executable = () -> basketService.clearBasket(basketId);

        //Then
        assertThrows(ForbiddenException.class, executable, "Can only clear an ACTIVE basket");

    }

    @Test
    void checkOutBasket_ShouldReturnBasketStatusCheckedOut_WhenBasketStatusIsActive() {

        // Given
        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();

        BasketItem item = new BasketItem();
        item.setId(UUID.randomUUID());

        Basket basket = new Basket();
        basket.setId(basketId);
        basket.setBasketStatus(BasketStatus.ACTIVE);
        basket.addBasketItemToBasket(item);

        User user = new User();
        user.setId(userId);
        user.addBasketToUser(basket);

        when(basketRepository.findById(basketId))
                .thenReturn(Optional.of(basket));

        when(basketRepository.findActiveBasketByUserId(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(basketRepository.save(any(Basket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Basket actual = basketService.checkoutBasket(userId, basketId);

        // Then
        assertEquals(BasketStatus.CHECKED_OUT, actual.getBasketStatus());
        verify(basketRepository, times(1)).findById(basketId);
        verify(basketRepository, times(1)).findActiveBasketByUserId(userId, BasketStatus.ACTIVE);
        verify(basketRepository, times(2)).save(any(Basket.class)); // One for checked-out, one for new basket
    }

    @Test
    void checkOutBasket_ShouldThrowInvalidQuantityException_WhenBasketIsEmpty() {
        //given
        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();

        Basket basket = new Basket();
        basket.setId(basketId);
        basket.setBasketStatus(BasketStatus.ACTIVE);

        when(basketRepository.findById(any())).thenReturn(Optional.of(basket));

        //when
        Executable executable = () -> basketService.checkoutBasket(userId,basketId);

        //then
        assertThrows(InvalidQuantityException.class, executable, "The current basket is empty.");
        verify(basketRepository,times(1)).findById(basketId);
    }

    @ParameterizedTest
    @CsvSource({"inactive", "checked_out"})
    void checkOutBasket_ShouldThrowForbiddenException_WhenBasketStatusIsCheckedOut(String input) {
        //given
        BasketStatus status = null;
        if(input.equalsIgnoreCase("INACTIVE"))
            status = BasketStatus.INACTIVE;
        else status = BasketStatus.CHECKED_OUT;

        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();

        BasketItem item = new BasketItem();
        item.setId(UUID.randomUUID());

        Basket basket = new Basket();
        basket.setId(basketId);
        basket.setBasketStatus(status);
        basket.addBasketItemToBasket(item);

        when(basketRepository.findById(any())).thenReturn(Optional.of(basket));

        //when
        Executable executable = () -> basketService.checkoutBasket(userId,basketId);

        //then
        assertThrows(ForbiddenException.class,executable, "Can only check out an ACTIVE basket.");
        verify(basketRepository,times(1)).findById(basketId);
    }

    @Test
    void findBasketById_ShouldReturnBasketWithTheSameId_WhenIdDoesExist() {
        // given
        UUID basketId = UUID.randomUUID();
        Basket expectedBasket = new Basket();
        expectedBasket.setId(basketId);
        when(basketRepository.findById(any())).thenReturn(Optional.of(expectedBasket));

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
        UUID basketId = UUID.randomUUID();
        when(basketRepository.findById(any())).thenReturn(Optional.empty());

        // when
        Executable executable = () -> basketService.findBasketById(basketId);

        //then
        assertThrows(NotFoundException.class, executable);
        verify(basketRepository, times(1)).findById(basketId);
    }

    @Test
    void getTotalQuantity_ShouldReturnTotalItemQuantityInBasket() {
        //given
        UUID basketId = UUID.randomUUID();

        BasketItem item1 = new BasketItem();
        item1.setId(UUID.randomUUID());
        item1.setQuantity(2);

        BasketItem item2 = new BasketItem();
        item2.setId(UUID.randomUUID());
        item2.setQuantity(3);

        Basket basket = new Basket();
        basket.addBasketItemToBasket(item1);
        basket.addBasketItemToBasket(item2);

        when(basketRepository.findById(any())).thenReturn(Optional.of(basket));

        //when
        Integer result = basketService.getTotalQuantity(basketId);

        //then
        assertEquals(5,result);
        verify(basketRepository,times(1)).findById(basketId);
    }


    @Test
    void calculateTotalPrice_ShouldReturnTheTotalPriceOfBasket() {
        //given
        Basket basket = new Basket();
        basket.setId(UUID.randomUUID());

        Product product1 = new Product();
        product1.setProductPrice(new BigDecimal("15"));
        Product product2 = new Product();
        product2.setProductPrice(new BigDecimal("17.34"));

        BasketItem item1 = new BasketItem();
        item1.setProduct(product1);
        item1.setQuantity(3);
        BasketItem item2 = new BasketItem();
        item2.setProduct(product2);
        item2.setQuantity(2);

        basket.addBasketItemToBasket(item1);
        basket.addBasketItemToBasket(item2);

        when(basketRepository.findById(any())).thenReturn(Optional.of(basket));

        BigDecimal expected = new BigDecimal("15.00").multiply(new BigDecimal("3"))
                .add(new BigDecimal("17.34").multiply(new BigDecimal("2")));

        //when
        BigDecimal actual = basketService.calculateTotalPrice(basket.getId());

        //then
        assertEquals(expected, actual);
        verify(basketRepository,times(1)).findById(basket.getId());
    }

    @Test
    void removeItemFromBasket_ShouldReturnBasketWithoutItem_WhenBasketHasTheItem(){

        //given
        UUID basketId = UUID.randomUUID();
        UUID item1Id = UUID.randomUUID();
        UUID item2Id = UUID.randomUUID();

        Product product = new Product();
        product.setStock(10);

        BasketItem item1 = new BasketItem();
        item1.setId(item1Id);
        item1.setQuantity(2);

        BasketItem item2 = new BasketItem();
        item2.setId(item2Id);
        item2.setQuantity(3);

        Basket basket = new Basket();
        basket.setId(basketId);
        basket.addBasketItemToBasket(item1);
        basket.addBasketItemToBasket(item2);

        doAnswer(invocation -> {
            int stock = product.getStock();
            product.setStock(stock + item1.getQuantity());
            return null;
        }).when(productService).updateProductStock(any(Product.class), anyInt()); // Mock product service


        //when
        BasketItem removedItem = basketService.removeItemFromBasket(basket, item1);

        //then
        assertFalse(basket.getBasketItems().contains(removedItem));

    }
}