package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.exceptions.InsufficientStockException;
import com.fabiolima.e_commerce.exceptions.InvalidQuantityException;
import com.fabiolima.e_commerce.entities.Basket;
import com.fabiolima.e_commerce.entities.BasketItem;
import com.fabiolima.e_commerce.entities.Product;
import com.fabiolima.e_commerce.entities.enums.BasketStatus;
import com.fabiolima.e_commerce.repository.BasketItemRepository;
import com.fabiolima.e_commerce.repository.BasketRepository;
import com.fabiolima.e_commerce.service.implementation.BasketItemServiceImpl;
import com.fabiolima.e_commerce.service.implementation.BasketServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class BasketItemServiceImplTest {

    @MockitoBean
    private BasketItemRepository basketItemRepository;
    @MockitoBean
    private BasketServiceImpl basketService;
    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private BasketRepository basketRepository;

    @Autowired
    private BasketItemServiceImpl basketItemService;


    @Test
    void addItemToBasket_ShouldReturnBasketWithAddedItemAndUpdateStock_WhenItemIsNotInBasketAndStockGreaterThanQuant() {
        //Given
        UUID basketId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int quantity = 7;
        int initialStock = 10;

        //create basket, product and item to mock addItemToBasketMethod
        Basket basket = new Basket();
        basket.setId(basketId);
        basket.setBasketStatus(BasketStatus.ACTIVE);

        Product product = Product.builder()
                .id(productId)
                .stock(initialStock)
                .build();

        //create a new item
        BasketItem expectedItem = BasketItem.builder()
                .product(product)
                .quantity(quantity)
                .build();

        //mocking the dependency injection methods
        when(basketService.findBasketById(basketId)).thenReturn(basket);
        when(productService.findProductById(productId)).thenReturn(product);
        doAnswer(invocation -> { //I used doAnswer as I don't need to return the product, but to modify it
            product.setStock(initialStock - quantity);
            return null;
        }).when(productService).updateProductStock(any(Product.class), anyInt());
        when(basketService.updateBasketWhenItemsAreAddedOrModified(any(Basket.class))).thenReturn(basket);

        /**
         * doAnswer().when() and when().thenAnswer() are used when you want to modify or perform custom actions with the arguments or the returned object before returning it.
         *
         * doAnswer() is used preferably when mocking methods that don't return anything, as it allows you to simulate side effects like modifying objects or invoking other methods.
         * thenAnswer() is more commonly used for methods that return values, allowing you to manipulate or customize the return value based on the method's arguments or any other conditions.
         *
         * when().thenReturn is used when you simply want to return a predefined object without any custom manipulation or logic. It’s a straightforward way to mock the return value of a method without involving side effects or custom logic.
         */

        //When
        BasketItem actualItem = basketItemService.addItemToBasket(basketId,productId, quantity);

        //Then
        assertNotNull(actualItem);
        assertEquals(productId,actualItem.getProduct().getId());
        assertEquals(quantity,actualItem.getQuantity());
        assertEquals(initialStock - quantity, product.getStock());
        assertTrue(basket.getBasketItems().contains(actualItem));
        assertEquals(expectedItem.getProduct(), actualItem.getProduct());
        assertEquals(expectedItem.getQuantity(), actualItem.getQuantity());

        verify(basketService, times(1)).findBasketById(basketId);
        verify(productService, times(1)).findProductById(productId);
        verify(productService, times(1)).updateProductStock(product,quantity);
        verify(basketService, times(1)).updateBasketWhenItemsAreAddedOrModified(basket);
    }

    @Test
    void addItemToBasket_ShouldIncrementItemQuantityInBasketAndUpdateStock_WhenBasketAlreadyHasItem(){

        //Given
        UUID basketId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int quantity = 6;
        int initialStock = 10;

        //create basket, product and item to mock addItemToBasketMethod
        Basket basket = new Basket();
        basket.setId(basketId);
        basket.setBasketStatus(BasketStatus.ACTIVE);

        Product product = Product.builder()
                .id(productId)
                .stock(initialStock)
                .build();

        BasketItem existingItem = new BasketItem();
        existingItem.setProduct(product);
        existingItem.setQuantity(3);//existing quantity
        //basket already contains the item
        basket.addBasketItemToBasket(existingItem);

        //mocking services call
        when(basketService.findBasketById(any())).thenReturn(basket);
        when(productService.findProductById(any())).thenReturn(product);
        doAnswer(invocation -> {
            product.setStock(initialStock - quantity);
            return null;
        }).when(productService).updateProductStock(any(Product.class),anyInt());

        when(basketItemRepository.save(any(BasketItem.class))).thenReturn(existingItem);

        //When
        BasketItem actualItem = basketItemService.addItemToBasket(basketId,productId,quantity);

        //Then
        assertNotNull(actualItem);
        assertEquals(product, actualItem.getProduct());
        //expected quantity 3 + 6 (original quantity + new quantity)
        assertEquals(9, actualItem.getQuantity());
        //expected stock -> 10 - 6 (initial stock - new quantity)
        assertEquals(4, actualItem.getProduct().getStock());

        verify(basketService, times(1)).findBasketById(basketId);
        verify(productService, times(1)).findProductById(productId);
        verify(productService, times(1)).updateProductStock(product,quantity);
        verify(basketItemRepository, times(1)).save(existingItem);
        verify(basketService, never()).updateBasketWhenItemsAreAddedOrModified(basket); // this method is not reached when the item already exists.
    }

    @Test
    void addItemToBasket_ShouldReturnInsufficientStockException_WhenStockIsLessThanQuantity(){

        //given
        Basket basket = Basket.builder()
                .id(UUID.randomUUID())
                .basketStatus(BasketStatus.ACTIVE).build();

        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setStock(5);

        int quantity = 6;

        when(basketService.findBasketById(any())).thenReturn(basket);
        when(productService.findProductById(any())).thenReturn(product);

        //when
        Executable executable = () -> basketItemService.addItemToBasket(basket.getId(),productId,quantity);

        //then
        assertThrows(InsufficientStockException.class, executable);

    }

    @ParameterizedTest
    @CsvSource({"-5", "0"})
    void addItemToBasket_ShouldReturnInvalidQuantityException_WhenQuantityIsZeroOrNegative(String input){
        //given
        UUID productId = UUID.randomUUID();
        int quantity = Integer.parseInt(input);

        //when
        Executable executable = () -> basketItemService.addItemToBasket(UUID.randomUUID(),productId,quantity);

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
        basket.setId(UUID.randomUUID());

        basket.addBasketItemToBasket(item1);
        basket.addBasketItemToBasket(item2);
        basket.addBasketItemToBasket(item3);

        when(basketService.findBasketById(any())).thenReturn(basket);

        //When
        List<BasketItem> actualListOfItems = basketItemService.getItemsByBasket(basket.getId());

        //Then
        assertNotNull(actualListOfItems);
        assertTrue(actualListOfItems.containsAll(basket.getBasketItems()));
        assertEquals(basket.getBasketItems(),actualListOfItems);

        verify(basketService,times(1)).findBasketById(basket.getId());

    }

    @Test
    void getItemById_ShouldReturnItem_WhenItemIdIsValidAndItemExists() {
        //Given
        UUID itemId = UUID.randomUUID();

        BasketItem expectedItem = new BasketItem();
        expectedItem.setId(itemId);

        when(basketItemRepository.findById(any())).thenReturn(Optional.of(expectedItem));

        //When
        BasketItem actualItem = basketItemService.getItemById(itemId);

        //Then
        assertNotNull(actualItem);
        assertEquals(expectedItem,actualItem);

        verify(basketItemRepository,times(1)).findById(itemId);
    }

    @Transactional
    @Test
    void updateBasketItem_ShouldReturnItemWithNewQuantity_WhenQuantityIsSmallerThanStock() {
        //Given
        UUID basketId = UUID.randomUUID();
        UUID basketItemId = UUID.randomUUID();
        int initialQuantity = 7;
        int initialStock = 100;
        int newQuantity = 3;
        int delta = newQuantity - initialQuantity;

        BasketItem item = new BasketItem();
        item.setId(basketItemId);
        item.setQuantity(initialQuantity);

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setStock(initialStock);

        item.setProduct(product);

        //build basket to update last time it was updated
        Basket basket = new Basket();
        basket.setId(basketId);

        //mocking the called methods

        when(basketService.findBasketById(any())).thenReturn(basket);
        when(basketItemRepository.findById(any())).thenReturn(Optional.of(item));
        doAnswer(invocation -> {
            product.setStock(initialStock - delta);
            return null;
        }).when(productService).updateProductStock(any(Product.class), anyInt());
        when(basketRepository.save(any(Basket.class))).thenReturn(basket);
        when(basketItemRepository.save(any(BasketItem.class))).thenReturn(item);

        //When
        BasketItem actualItem = basketItemService.updateBasketItem(basketId, basketItemId, newQuantity);

        //Then
        assertNotNull(actualItem);
        assertEquals(item.getQuantity(), actualItem.getQuantity());
        assertEquals(item,actualItem);
        assertEquals(product.getStock(), actualItem.getProduct().getStock());

        verify(basketItemRepository, times(1)).findById(basketItemId);
        verify(basketItemRepository,times(1)).save(item);
        verify(productService, times(1)).updateProductStock(product, delta);

    }

    @Test
    void updateBasketItem_ShouldReturnInsufficientStockException_WhenNewQuantityIsGreaterThanCurrentQuantityAndStockIsLessThanQuantity(){

        //Given
        UUID basketId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        int initialQuantity = 3;
        BasketItem item = new BasketItem();
        item.setQuantity(initialQuantity);

        Product product = new Product();
        product.setStock(2);
        product.setProductName("Test");

        //the tested method gets the product from the item
        //so its paramount that product is set to item
        item.setProduct(product);

        int newQuantity = 6;

        //mocking the called methods
        when(basketItemRepository.findById(any())).thenReturn(Optional.of(item));

        //when
        Executable executable = () -> basketItemService.updateBasketItem(basketId, itemId, newQuantity);

        //then
        InsufficientStockException ex = assertThrows(InsufficientStockException.class, executable);
        assertEquals("Not enough stock available for product 'Test'. Available: 2, Requested: 3",
                ex.getMessage());

    }

    @Test
    void updateBasketItem_ShouldRemoveItemFromBasketAndUpdateStock_WhenNewQuantityIsZero(){
        //Given
        UUID basketId = UUID.randomUUID();
        UUID basketItemId = UUID.randomUUID();
        int initialQuantity = 5;
        int newQuantity = 0;
        int initialStock = 100;
        int delta = newQuantity - initialQuantity;

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setStock(initialStock);

        BasketItem item = new BasketItem();
        item.setId(basketItemId);
        item.setQuantity(initialQuantity);
        item.setProduct(product);

        Basket basket = new Basket();
        basket.setId(basketId);
        basket.addBasketItemToBasket(item);

        //mocking methods in updateBasketItem
        when(basketItemRepository.findById(any())).thenReturn(Optional.of(item));
        when(basketService.findBasketById(any())).thenReturn(basket);

        // mocking updateProductStock inside the removeItemFromBasket
        when(basketService.removeItemFromBasket(any(Basket.class), any(BasketItem.class)))
                .thenAnswer(invocation -> {
                    basket.getBasketItems().remove(item);
                    // Now, simulate stock update when the item is removed from the basket
                    product.setStock(initialStock - delta);  // update stock by delta
                    return item;
                });

        //When
        BasketItem actualItem = basketItemService.updateBasketItem(basketId, basketItemId, newQuantity);

        //Then
        assertFalse(basket.getBasketItems().contains(actualItem));
        assertEquals(item,actualItem);//Removed item should be returned by the removeItemFromBasket
        assertEquals(initialStock - delta, actualItem.getProduct().getStock());//after removing item, the 5 items should go back to stock

        verify(basketItemRepository, times(1)).findById(basketItemId);
        verify(basketService, times(1)).findBasketById(basketId);
        verify(basketItemRepository,never()).save(item);

    }

    @Test
    void incrementItemQuantity_ShouldReturnItemWithQuantityIncrementedByOne_WhenTheresEnoughStockAvailable() {

        //Given
        UUID itemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int quantity = 1;
        int delta = 1; // delta = new quantity - initial quantity -> new quantity is always one unit more
        int initialStock = 10;

        Product product = new Product();
        product.setId(productId);
        product.setStock(initialStock);

        BasketItem item = new BasketItem();
        item.setId(itemId);
        item.setQuantity(quantity);
        item.setProduct(product);

        Basket basket = new Basket();
        basket.setId(UUID.randomUUID());
        basket.addBasketItemToBasket(item);

        //mocking the methods
        when(basketItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        doAnswer(invocation -> {
        product.setStock(initialStock - delta);
        return null;
        }).when(productService).updateProductStock(product, delta);

        when(basketRepository.save(any(Basket.class))).thenReturn(basket);
        when(basketItemRepository.save(any(BasketItem.class))).thenReturn(item);

        //When
        BasketItem actualItem = basketItemService.incrementItemQuantity(itemId);

        //Then
        assertNotNull(actualItem);
        assertEquals( quantity + 1, actualItem.getQuantity());
        assertEquals(initialStock - delta, actualItem.getProduct().getStock());

    }

    @Test
    void incrementItemQuantity_ShouldThrowInsufficientStockException_WhenTheresEnoughStockAvailable() {

        //Given
        UUID itemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int quantity = 10;

        Product product = new Product();
        product.setId(productId);
        product.setStock(0);

        BasketItem item = new BasketItem();
        item.setId(itemId);
        item.setQuantity(quantity);
        item.setProduct(product);

        //mocking the methods
        when(basketItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        //When
        Executable executable = () -> basketItemService.incrementItemQuantity(itemId);

        //Then
        assertThrows(InsufficientStockException.class, executable);

    }

    @Test
    void decrementItemQuantity_ShouldDecrementItemQuantityByOneAndIncrementStockByOne_WhenItemQuantityIsGreaterThanOne() {
        //Given
        UUID itemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        int initialQuantity = 8;
        int delta = -1;
        int currentStock = 10;

        Product product = new Product();
        product.setId(productId);
        product.setStock(currentStock);

        BasketItem item = new BasketItem();
        item.setId(itemId);
        item.setProduct(product);
        item.setQuantity(initialQuantity);

        Basket basket = new Basket();
        basket.setId(UUID.randomUUID());
        basket.addBasketItemToBasket(item);

        //mocking the methods
        when(basketItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        doAnswer(invocation -> {
            product.setStock(currentStock - delta);
            return null;
        }).when(productService).updateProductStock(product, delta);
        when(basketRepository.save(basket)).thenReturn(basket);
        when(basketItemRepository.save(item)).thenReturn(item);

        //When
        BasketItem actualItem = basketItemService.decrementItemQuantity(basketId,itemId);

        //Then
        assertNotNull(actualItem);
        assertEquals(initialQuantity - 1, actualItem.getQuantity());
        assertEquals(11,actualItem.getProduct().getStock());
    }

    @Test
    void decrementItemQuantity_ShouldRemoveItemFromBasketAndIncrementStockByOne_WhenItemQuantityIsOne(){
        //Given
        UUID basketId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int currentStock = 10;
        int delta = -1;

        Product product = new Product();
        product.setId(productId);
        product.setStock(currentStock);

        BasketItem basketItem = new BasketItem();
        basketItem.setId(itemId);
        basketItem.setQuantity(1);
        basketItem.setProduct(product);

        Basket basket = new Basket();
        basket.setId(basketId);
        basket.addBasketItemToBasket(basketItem);

        when(basketItemRepository.findById(any())).thenReturn(Optional.of(basketItem));
        when(basketService.findBasketById(any())).thenReturn(basket);
        when(basketService.removeItemFromBasket(any(Basket.class), any(BasketItem.class)))
                .thenAnswer(invocation -> {
                    basket.getBasketItems().remove(basketItem);
                    // Now, simulate stock update when the item is removed from the basket
                    product.setStock(currentStock - delta);  // update stock by delta
                    return basketItem;
                });

        //When
        BasketItem actualItem = basketItemService.decrementItemQuantity(basketId, itemId);

        //Then
        assertNotNull(actualItem);
        assertFalse(basket.getBasketItems().contains(actualItem));
        assertEquals(11,actualItem.getProduct().getStock());

        verify(basketItemRepository,times(1)).findById(itemId);
        verify(basketService,times(1)).findBasketById(basketId);
        verify(basketService, times(1)).removeItemFromBasket(basket, basketItem);
        verify(basketItemRepository,never()).save(basketItem);
    }

    @ParameterizedTest
    @CsvSource({"0","-5"})
    void decrementItemQuantity_ShouldThrowInvalidQuantityException_WhenItemQuantityBelow1(String input){
        //Given
        UUID basketId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        int quantity = Integer.parseInt(input);

        BasketItem item = new BasketItem();
        item.setId(itemId);
        item.setQuantity(quantity);

        when(basketItemRepository.findById(any())).thenReturn(Optional.of(item));

        //When
        Executable executable = () -> basketItemService.decrementItemQuantity(basketId,itemId);

        //Then
        assertThrows(InvalidQuantityException.class,executable);
    }

    @Test
    void removeItemFromBasket_ShouldRemoveItemFromBasketAndUpdateStock_WhenItemIsFoundInBasket() {
        //Given
        UUID basketId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Basket basket = new Basket();
        basket.setId(basketId);

        Product product = new Product();
        product.setStock(10);

        BasketItem item1 = new BasketItem();
        item1.setId(UUID.randomUUID());
        BasketItem item2 = new BasketItem();
        item2.setId(UUID.randomUUID());

        BasketItem removedItem = new BasketItem();
        removedItem.setId(itemId);
        removedItem.setQuantity(4);
        removedItem.setProduct(product);

        basket.addBasketItemToBasket(item1);
        basket.addBasketItemToBasket(item2);
        basket.addBasketItemToBasket(removedItem);

        //mock the dependencies calls
        when(basketService.findBasketById(any())).thenReturn(basket);
        when(basketItemRepository.findById(any())).thenReturn(Optional.of(removedItem));
        when(basketService.removeItemFromBasket(any(Basket.class), any(BasketItem.class)))
                .thenAnswer(invocation -> {
                    basket.getBasketItems().remove(removedItem);
                    // Now, simulate stock update when the item is removed from the basket
                    product.setStock(10 + 4);  // update stock by delta
                    return removedItem;
                });

        //When
        //method removeItemFromBasket returns the removed item for testing purposes
        BasketItem actualRemovedItem = basketItemService.removeItemFromBasket(basketId, itemId);

        //Then
        assertNotNull(actualRemovedItem);
        assertFalse(basket.getBasketItems().contains(actualRemovedItem));
        assertEquals(removedItem,actualRemovedItem);
        assertEquals((10+4),actualRemovedItem.getProduct().getStock());

        verify(basketService,times(1)).findBasketById(basketId);
        verify(basketService, times(1)).removeItemFromBasket(basket, removedItem);
    }

    @Test
    void ensureStockAvailable_ShouldThrowIllegalArgumentException_WhenProductIsNull() {
        //Given
        int quantity = 10;
        Product product = null;

        //When
        Executable executable = () -> basketItemService.ensureStockAvailable(product,quantity);

        //Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,executable);
        assertEquals("Product cannot be null",ex.getMessage());
    }

    @Test
    void ensureStockAvailable_ShouldThrowInvalidQuantityException_WhenQuantityIsNegative() {
        //Given
        int quantity = -10;
        Product product = new Product();

        //When
        Executable executable = () -> basketItemService.ensureStockAvailable(product,quantity);

        //Then
        InvalidQuantityException ex = assertThrows(InvalidQuantityException.class,executable);
        assertEquals("Quantity cannot have negative values",ex.getMessage());
    }

    @Test
    void ensureStockAvailable_ShouldThrowInsufficientStockException_WhenStockIsSmallerThanQuantity() {
        //Given
        int quantity = 10;

        Product product = new Product();
        product.setStock(6);
        product.setProductName("Test");

        //When
        Executable executable = () -> basketItemService.ensureStockAvailable(product,quantity);

        //Then
        InsufficientStockException ex = assertThrows(InsufficientStockException.class,executable);
        assertEquals("Not enough stock available for product 'Test'. Available: 6, Requested: 10",ex.getMessage());
    }

    @Test
    void calculateItemTotalPrice_ShouldReturnTotalPriceOfItem_WhenItemQuantityAndProductAreValid() {
        //Given
        int quantity = 5;
        BigDecimal price = new BigDecimal(4.5);
        UUID itemId = UUID.randomUUID();

        Product product = new Product();
        product.setProductPrice(price);

        BasketItem item = new BasketItem();
        item.setId(itemId);
        item.setQuantity(quantity);
        item.setProduct(product);

        BigDecimal expectedTotal = new BigDecimal("22.50").setScale(2, RoundingMode.HALF_UP);

        when(basketItemRepository.findById(any())).thenReturn(Optional.of(item));

        //When
        BigDecimal actualTotal = basketItemService.calculateItemTotalPrice(UUID.randomUUID());

        //Then
        assertEquals(expectedTotal, actualTotal);
    }


    @Test
    void calculateItemTotalPrice_ShouldReturnZero_WhenItemQuantityisZer0() {

        //Given
        int quantity = 0;
        BigDecimal price = new BigDecimal(4.5);
        UUID itemId = UUID.randomUUID();

        Product product = new Product();
        product.setProductPrice(price);

        BasketItem item = new BasketItem();
        item.setId(itemId);
        item.setQuantity(quantity);
        item.setProduct(product);

        BigDecimal expectedTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        when(basketItemRepository.findById(any())).thenReturn(Optional.of(item));

        //When
        BigDecimal actualTotal = basketItemService.calculateItemTotalPrice(UUID.randomUUID());

        //Then
        assertEquals(expectedTotal, actualTotal, "Total price should be zero for zero quantity.");
    }

}