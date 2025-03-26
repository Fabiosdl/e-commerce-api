package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.InsufficientStockException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.entities.Basket;
import com.fabiolima.e_commerce.entities.BasketItem;
import com.fabiolima.e_commerce.entities.Product;
import com.fabiolima.e_commerce.entities.Order;
import com.fabiolima.e_commerce.repository.ProductRepository;
import com.fabiolima.e_commerce.service.implementation.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
        product1.setId(UUID.randomUUID());
        product1.setProductName("Product One");

        Product product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setProductName("Product Two");

        int pgNum = 0;
        int pgSize = 2;
        List<Product> productList = List.of(product1, product2);

        Pageable pageable = PageRequest.of(pgNum, pgSize);
        Page<Product> productPage = new PageImpl<>(productList, pageable, productList.size());
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        //when
        Page<Product> actual = productService.findAllProducts(pgNum, pgSize);

        //then
        assertNotNull(actual);
        assertEquals(product1.getId(), actual.getContent().get(0).getId());
        assertEquals(product2.getId(), actual.getContent().get(1).getId());

        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void findProductById_ShouldReturnProduct_WhenUserExist() {
        //given
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setProductName("Product One");

        when(productRepository.findById(any())).thenReturn(Optional.of(product));

        //when
        Product actual = productService.findProductById(product.getId());

        //then
        assertEquals(product, actual);
        verify(productRepository, times(1)).findById(product.getId());
    }

    @Test
    void findProductById_ShouldReturnNotFoundException_WhenUserDoesNotExist() {
        //given
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        //when
        Executable exe = () -> productService.findProductById(UUID.randomUUID());

        //then
        assertThrows(NotFoundException.class, exe);

    }

    @Test
    void findProductsByCategory(){
        // Given
        Product product1 = Product.builder().id(UUID.randomUUID()).category("Games").build();
        Product product2 = Product.builder().id(UUID.randomUUID()).category("Computers").build();
        Product product3 = Product.builder().id(UUID.randomUUID()).category("Games").build();

        int pgNum = 0;
        int pgSize = 2;

        List<Product> productList = List.of(product1,product2,product3);
        List<Product> categoryList = productList.stream()
                .filter(product -> product.getCategory().equalsIgnoreCase("Games"))
                .toList();
        Pageable pageable = PageRequest.of(pgNum,pgSize);
        Page<Product> productPage = new PageImpl<>(categoryList, pageable, categoryList.size());

        // mock productRepository
        when(productRepository.findAllByCategory("Games", pageable))
                .thenReturn(productPage);

        // When

        Page<Product> actualPage = productService.findProductsByCategory(pgNum, pgSize, "Games");

        // Then
        assertNotNull(actualPage);
        assertThat(actualPage)
                .allSatisfy(product -> assertEquals("Games",product.getCategory())
                );
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
        product.setId(UUID.randomUUID());
        product.setProductName("Product One");
        product.setProductDescription("Test");
        product.setProductPrice(new BigDecimal("10.00"));
        product.setStock(20);
        product.setCategory("Electronics");

        Object parsedValue = switch (field) {
            case "productPrice" -> new BigDecimal(newValue);
            case "stock" -> Integer.parseInt(newValue);
            default -> newValue;
        };

        HashMap<String, Object> map = new HashMap<>();
        map.put(field, parsedValue);

        when(productRepository.findById(any())).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        //when
        Product actual = productService.patchUpdateProductById(product.getId(), map);

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
        product.setId(UUID.randomUUID());
        product.setProductName("Product One");
        product.setProductDescription("Testing Exception");

        HashMap<String, Object> map = new HashMap<>();
        map.put("productName", "Product Two");
        map.put("test", "Testing Exception");

        when(productRepository.findById(any())).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        //when
        Executable exe = () -> productService.patchUpdateProductById(product.getId(), map);

        //then
        assertThrows(ForbiddenException.class, exe);

    }

    @Test
    void deleteProductById_ShouldDeleteProduct() {
        //given
        Product product = new Product();
        product.setId(UUID.randomUUID());

        when(productRepository.findById(any())).thenReturn(Optional.of(product));
        doNothing().when(productRepository).deleteById(product.getId());

        //when
        productService.deleteProductById(product.getId());

        //then
        verify(productRepository, times(1)).deleteById(product.getId());
    }

    @Test
    void incrementStocksWhenOrderIsCancelled_ShouldReturnProductWithIncrementedStock() {
        //given

        //setting two products with a stock of 10
        Product product1 = new Product();
        product1.setId(UUID.randomUUID());
        product1.setStock(10);

        Product product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setStock(8);

        //create a list of items and insert in basket and then insert basket into order
        //as the method asks for the order
        BasketItem item1 = new BasketItem();
        item1.setId(UUID.randomUUID());
        item1.setProduct(product1);
        item1.setQuantity(5);

        BasketItem item2 = new BasketItem();
        item2.setId(UUID.randomUUID());
        item2.setProduct(product2);
        item2.setQuantity(6);

        Basket basket = new Basket();
        basket.setId(UUID.randomUUID());
        basket.addBasketItemToBasket(item1);
        basket.addBasketItemToBasket(item2);

        Order order = new Order();
        order.setId(UUID.randomUUID());
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

    @ParameterizedTest
    @CsvSource({"4,3","4,4"})
    void updateProductStock_ShouldReturnUpdatedStock_WhenDeltaSmallerOrEqualCurrentStock(String stock, String delta){
        // Given
        int currentStock = Integer.parseInt(stock);
        int deltaStock = Integer.parseInt(delta);
        int expectedStock = currentStock - deltaStock;
        Product product = Product.builder().id(UUID.randomUUID())
                .stock(currentStock).build();

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.updateProductStock(product, deltaStock);

        // Then

        assertEquals(expectedStock, product.getStock(), "Stock should be updated correctly");
        verify(productRepository).save(product);
    }

    @Test
    void updateProductStock_ShouldThrowInsufficientStockException_WhenDeltaGreaterCurrentStock(){
        // Given
        int currentStock = 3;
        int deltaStock = 4;

        Product product = Product.builder().id(UUID.randomUUID())
                .stock(currentStock).build();

        // When
        Executable executable = () -> productService.updateProductStock(product, deltaStock);

        // Then

        assertThrows( InsufficientStockException.class, executable,
                String.format("Not enough stock available for product '%s'. Available: %d, Requested: %d",
                product.getProductName(), product.getStock(), deltaStock));

    }
}