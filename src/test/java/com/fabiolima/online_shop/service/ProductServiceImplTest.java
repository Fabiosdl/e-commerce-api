package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.exceptions.ForbiddenException;
import com.fabiolima.online_shop.exceptions.NotFoundException;
import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.BasketItem;
import com.fabiolima.online_shop.model.Product;
import com.fabiolima.online_shop.model.TheOrder;
import com.fabiolima.online_shop.repository.ProductRepository;
import com.fabiolima.online_shop.service_implementation.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductServiceImplTest {

    @MockitoBean
    private ProductRepository productRepository;
    @Autowired
    private ProductServiceImpl productService;

    @Test
    void saveProduct_ShouldReturnSavedProduct() {
        //given
        Product product = new Product();
        when(productRepository.save(product)).thenReturn(product);

        //when
        Product actual = productService.saveProduct(product);

        //then
        assertEquals(product, actual);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void findAllProducts_ShouldFindListOfProducts() {
        //given
        Product product1 = new Product();
        product1.setId(1L);
        product1.setProductName("Product One");

        Product product2 = new Product();
        product2.setId(1L);
        product2.setProductName("Product Two");

        List<Product> productList = List.of(product1, product2);

        when(productRepository.findAll()).thenReturn(productList);

        //when
        List<Product> actual = productService.findAllProducts();

        //then
        assertEquals(productList, actual);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void findProductById_ShouldReturnProduct_WhenUserExist() {
        //given
        Product product = new Product();
        product.setId(1L);
        product.setProductName("Product One");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        //when
        Product actual = productService.findProductById(1L);

        //then
        assertEquals(product, actual);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void findProductById_ShouldReturnNotFoundException_WhenUserDoesNotExist() {
        //given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        //when
        Executable exe = () -> productService.findProductById(1L);

        //then
        assertThrows(NotFoundException.class, exe);
        verify(productRepository, times(1)).findById(1L);

    }

    @ParameterizedTest
    @CsvSource({"productName, Product Two",
            "productDescription, This is a test",
            "productPrice, 15.00",
            "stock, 10",
            "category, miscellaneous"
    })
    void patchUpdateProductById_ShouldReturnUpdatedProduct(String field, String newValue) {
        //given
        Product product = new Product();
        product.setId(1L);
        product.setProductName("Product One");
        product.setProductDescription("Test");
        product.setProductPrice(10.00);
        product.setStock(20);
        product.setCategory("Electronics");

        Object parsedValue = switch (field) {
            case "productPrice" -> Double.parseDouble(newValue);
            case "stock" -> Integer.parseInt(newValue);
            default -> newValue;
        };

        HashMap<String, Object> map = new HashMap<>();
        map.put(field, parsedValue);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        //when
        Product actual = productService.patchUpdateProductById(1L, map);

        //then
        switch (field) {
            case "productName" -> assertEquals(parsedValue, actual.getProductName());
            case "productDescription" -> assertEquals(parsedValue, actual.getProductDescription());
            case "productPrice" -> assertEquals(parsedValue, actual.getProductPrice());
            case "stock" -> assertEquals(parsedValue, actual.getStock());
            case "category" -> assertEquals(parsedValue, actual.getCategory());
            default -> fail("Unexpected field " + field);
        }
    }

    @Test
    void patchUpdateProductById_ShouldReturnForbiddenException_WhenProductFieldDoesNotExist() {
        //given
        Product product = new Product();
        product.setId(1L);
        product.setProductName("Product One");
        product.setProductDescription("Testing Exception");

        HashMap<String, Object> map = new HashMap<>();
        map.put("productName", "Product Two");
        map.put("test", "Testing Exception");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        //when
        Executable exe = () -> productService.patchUpdateProductById(1L, map);

        //then
        assertThrows(ForbiddenException.class, exe);

    }

    @Test
    void deleteProductById_ShouldDeleteProduct() {
        //given
        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).deleteById(1L);

        //when
        productService.deleteProductById(1L);

        //then
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void incrementStocksWhenOrderIsCancelled_ShouldReturnProductWithIncrementedStock() {
        //given

        //setting two products with a stock of 10
        Product product1 = new Product();
        product1.setId(1L);
        product1.setStock(10);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setStock(8);

        //create a list of items and insert in basket and then insert basket into order
        //as the method asks for the order
        BasketItem item1 = new BasketItem();
        item1.setId(1L);
        item1.setProduct(product1);
        item1.setQuantity(5);

        BasketItem item2 = new BasketItem();
        item2.setId(2L);
        item2.setProduct(product2);
        item2.setQuantity(6);

        Basket basket = new Basket();
        basket.setId(1L);
        basket.addBasketItemToBasket(item1);
        basket.addBasketItemToBasket(item2);

        TheOrder order = new TheOrder();
        order.setId(1L);
        order.setBasket(basket);
        basket.setOrder(order);

        List<Product> expectedProducts = List.of(product1, product2);

        //mocking the save method used in updateQuantInStock method
        when(productRepository.save(product1)).thenReturn(product1);
        when(productRepository.save(product2)).thenReturn(product2);

        // WHEN
        List<Product> updatedProducts = productService.incrementStocksWhenOrderIsCancelled(order);
        Product updatedProduct1 = updatedProducts.get(0);
        Product updatedProduct2 = updatedProducts.get(1);

        //THEN
        assertEquals(2, updatedProducts.size());
        assertTrue(expectedProducts.containsAll(updatedProducts));
        assertEquals(15, updatedProduct1.getStock(), "Product stock should be updated to 15 (10 of stock + 5 of returned item");
        assertEquals(14, updatedProduct2.getStock(), "Product stock should be updated to 14 (8 of stock + 6 of returned item");
        verify(productRepository, times(1)).save(product1);
        verify(productRepository, times(1)).save(product2);
    }
}