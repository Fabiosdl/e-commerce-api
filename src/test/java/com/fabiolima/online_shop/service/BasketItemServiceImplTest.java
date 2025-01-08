package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.exceptions.InsufficientStockException;
import com.fabiolima.online_shop.exceptions.InvalidIdException;
import com.fabiolima.online_shop.exceptions.InvalidQuantityException;
import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.BasketItem;
import com.fabiolima.online_shop.model.Product;
import com.fabiolima.online_shop.repository.BasketItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.Csv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class BasketItemServiceImplTest {

    @MockitoBean
    private BasketItemRepository basketItemRepository;
    @MockitoBean
    private BasketService basketService;
    @MockitoBean
    private ProductService productService;

    @Autowired
    private BasketItemServiceImpl basketItemService;


    @Test
    void addItemToBasket_ShouldReturnBasketWithAddedItem_WhenItemIsNotInBasketAndStockGreaterThanQuant() {
        //Given
        Long basketId = 1L;
        Long productId = 1L;
        int quantity = 7;

        //create basket, product and item to mock addItemToBasketMethod
        Basket basket = new Basket();
        basket.setId(basketId);

        Product product = Product.builder()
                .id(productId)
                .stock(10)
                .build();

        //mocking the dependency injection methods
        when(basketService.findBasketById(basketId)).thenReturn(basket);
        when(productService.findProductById(productId)).thenReturn(product);

        //create a new item
        BasketItem expectedItem = BasketItem.builder()
                        .product(product)
                        .quantity(quantity)
                        .build();

        /**
         * doAnswer().when() and when().thenAnswer() are used when you want to modify or perform custom actions with the arguments or the returned object before returning it.
         *
         * doAnswer() is used preferably when mocking void methods (methods that don't return anything), as it allows you to simulate side effects like modifying objects or invoking other methods.
         * thenAnswer() is more commonly used for methods that return values, allowing you to manipulate or customize the return value based on the method's arguments or any other conditions.
         *
         * when().thenReturn is used when you simply want to return a predefined object without any custom manipulation or logic. It’s a straightforward way to mock the return value of a method without involving side effects or custom logic.
         */

        // Directly modify the real basket in the test
        doAnswer(invocation -> {
            Basket basketArg = invocation.getArgument(0);
            basketArg.addBasketItemToBasket(expectedItem);
            return null;
        }).when(basketService).updateBasketWhenItemsAreAddedOrModified(any(Basket.class));

        //When
        BasketItem actualItem = basketItemService.addItemToBasket(basketId,productId, quantity);

        //Then
        assertNotNull(actualItem);
        assertEquals(productId,actualItem.getProduct().getId());
        assertEquals(quantity,actualItem.getQuantity());
        assertTrue(basket.getBasketItems().contains(actualItem));
        assertEquals(expectedItem.getProduct(), actualItem.getProduct());
        assertEquals(expectedItem.getQuantity(), actualItem.getQuantity());

        verify(basketService, times(1)).findBasketById(basketId);
        verify(productService, times(1)).findProductById(productId);
        verify(basketService, times(1)).updateBasketWhenItemsAreAddedOrModified(basket);
    }

    @Test
    void addItemToBasket_ShouldIncrementItemQuantityInBasket_WhenBasketAlreadyHasItem(){

        //Given
        Long basketId = 1L;
        Long productId = 1L;
        int quantity = 6;

        //create basket, product and item to mock addItemToBasketMethod
        Basket basket = new Basket();
        basket.setId(basketId);

        Product product = Product.builder()
                .id(productId)
                .stock(10)
                .build();

        BasketItem existingItem = new BasketItem();
        existingItem.setProduct(product);
        existingItem.setQuantity(3);//existing quantity
        //basket already contains the item
        basket.addBasketItemToBasket(existingItem);

        //mocking services call
        when(basketService.findBasketById(anyLong())).thenReturn(basket);
        when(productService.findProductById(anyLong())).thenReturn(product);
        when(basketItemRepository.save(any(BasketItem.class))).thenReturn(existingItem);

        //When
        BasketItem actualItem = basketItemService.addItemToBasket(basketId,productId,quantity);

        //Then
        assertNotNull(actualItem);
        assertEquals(product,actualItem.getProduct());
        //expected quantity 3 + 6 (original quantity + new quantity)
        assertEquals(9, actualItem.getQuantity());

        verify(basketItemRepository, times(1)).save(existingItem);
        verify(basketService, never()).updateBasketWhenItemsAreAddedOrModified(basket); // No need to update basket

    }

    @Test
    void addItemToBasket_ShouldReturnInsufficientStockException_WhenStockIsLessThanQuantity(){

        //given
        Product product = new Product();
        product.setStock(5);

        int quantity = 6;

        when(basketService.findBasketById(anyLong())).thenReturn(new Basket());
        when(productService.findProductById(anyLong())).thenReturn(product);

        //when
        Executable executable = () -> basketItemService.ensureStockAvailable(product,quantity);

        //then
        assertThrows(InsufficientStockException.class, executable);

    }

    @ParameterizedTest
    @CsvSource({"-5", "0"})
    void addItemToBasket_ShouldReturnIllegalArgumentException_WhenQuantityIsZeroOrNegative(int quantity){
        //given
        Product product = new Product();
        product.setId(1L);

        when(basketService.findBasketById(anyLong())).thenReturn(new Basket());
        when(productService.findProductById(anyLong())).thenReturn(product);

        //when
        Executable executable = () -> basketItemService.ensureStockAvailable(product,quantity);

        //then
        assertThrows(InvalidQuantityException.class, executable);
    }

    @Test
    void getItemsByBasket_ShouldReturnListOfItems_WhenUserExists() {
        //Given
        BasketItem item1 = new BasketItem();
        BasketItem item2 = new BasketItem();
        BasketItem item3 = new BasketItem();

        Basket basket = new Basket();
        basket.setId(1L);

        basket.addBasketItemToBasket(item1);
        basket.addBasketItemToBasket(item2);
        basket.addBasketItemToBasket(item3);

        when(basketService.findBasketById(anyLong())).thenReturn(basket);

        //When
        List<BasketItem> actualListOfItems = basketItemService.getItemsByBasket(1L);

        //Then
        assertNotNull(actualListOfItems);
        assertTrue(actualListOfItems.containsAll(basket.getBasketItems()));
        assertEquals(basket.getBasketItems(),actualListOfItems);

        verify(basketService,times(1)).findBasketById(1L);

    }

    @Test
    void getItemById_ShouldReturnItem_WhenItemIdIsValidAndItemExists() {
        //Given
        Long itemId = 1L;

        BasketItem expectedItem = new BasketItem();
        expectedItem.setId(itemId);

        when(basketItemRepository.findById(anyLong())).thenReturn(Optional.of(expectedItem));

        //When
        BasketItem actualItem = basketItemService.getItemById(itemId);

        //Then
        assertNotNull(actualItem);
        assertEquals(expectedItem,actualItem);

        verify(basketItemRepository,times(1)).findById(itemId);
    }

    /**
     * Testing the method validateId. This method is used in every method, but it will only be tested here
     */
    @ParameterizedTest
    @CsvSource({"-5","0"})
    void getItemById_ShouldThrowException_WhenItemIdIsSmallerThan1(String id) {
        //Given
        Long itemId = Long.parseLong(id);

        //When
        Executable executable = () -> basketItemService.validateId(itemId);

        //Then
        assertThrows(InvalidIdException.class, executable);
        verify(basketItemRepository,never()).findById(itemId);
    }

    @Test
    void getItemById_ShouldThrowException_WhenItemIdIsNull() {
        //Given
        Long itemId = null;

        //When
        Executable executable = () -> basketItemService.validateId(itemId);

        //Then
        assertThrows(InvalidIdException.class, executable);
        verify(basketItemRepository,never()).findById(1L);
    }

    @Test
    void updateBasketItem_ShouldReturnItemWithNewQuantity_WhenQuantityIsSmallerThanStock() {
        //Given
        Long basketId = 1L;
        Long basketItemId = 2L;
        int newQuantity = 3;

        BasketItem item = new BasketItem();
        item.setId(basketItemId);
        item.setQuantity(newQuantity);

        Product product = new Product();
        product.setId(1L);
        product.setStock(100);

        item.setProduct(product);

        //mocking the called methods
        when(basketItemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(basketItemRepository.save(item)).thenReturn(item);

        //When
        BasketItem actualItem = basketItemService.updateBasketItem(basketId, basketItemId, newQuantity);

        //Then
        assertNotNull(actualItem);
        assertEquals(item.getQuantity(), actualItem.getQuantity());
        assertEquals(item,actualItem);

        verify(basketItemRepository, times(1)).findById(basketItemId);
        verify(basketItemRepository,times(1)).save(item);

    }

    @Test
    void updateBasketItem_ShouldReturnInsufficientStockException_WhenStockIsLessThanQuantity(){

        //Given

        Product product = new Product();
        product.setStock(5);

        int quantity = 6;

        //mocking the called methods
        when(basketItemRepository.findById(anyLong())).thenReturn(Optional.of(new BasketItem()));

        //when
        Executable executable = () -> basketItemService.ensureStockAvailable(product,quantity);

        //then
        assertThrows(InsufficientStockException.class, executable);

    }

    @Test
    void updateBasketItem_ShouldRemoveItemFromBasket_WhenNewQuantityIsZero(){
        //Given
        Long basketId = 1L;
        Long basketItemId = 2L;
        int newQuantity = 0;

        Product product = new Product();
        product.setId(1L);
        product.setStock(100);

        BasketItem item = new BasketItem();
        item.setId(basketItemId);
        item.setQuantity(5);//this is the initial quantity.
        item.setProduct(product);

        Basket basket = new Basket();
        basket.setId(basketId);
        basket.addBasketItemToBasket(item);

        //mocking methods in updateBasketItem
        when(basketItemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(basketService.findBasketById(anyLong())).thenReturn(basket);

        //When
        BasketItem actualItem = basketItemService.updateBasketItem(basketId, basketItemId, newQuantity);

        //Then
        assertFalse(basket.getBasketItems().contains(actualItem));
        assertEquals(0,actualItem.getQuantity());
        assertEquals(item,actualItem);//Removed item should be returned by the removeItemFromBasket

        verify(basketItemRepository, times(1)).findById(basketItemId);
        verify(basketService, times(1)).findBasketById(basketId);
        verify(basketItemRepository,never()).save(item);

    }

    @Test
    void incrementItemQuantity() {
    }

    @Test
    void decrementItemQuantity() {
    }

    @Test
    void removeItemFromBasket() {
    }

    @Test
    void ensureStockAvailable() {
    }

    @Test
    void calculateItemTotalPrice() {
    }
}