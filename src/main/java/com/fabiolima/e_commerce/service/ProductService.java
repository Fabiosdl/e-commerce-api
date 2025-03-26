package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.entities.Product;
import com.fabiolima.e_commerce.entities.Order;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProductService {
    Product saveProduct(Product theProduct);
    Page<Product> findAllProducts(int pgNum, int pgSize);
    Page<Product> findProductsByCategory(int pgNum, int pgSize, String category);
    Product findProductById(UUID productId);
    Product patchUpdateProductById(UUID productId, Map<String,Object> updates);
    Product deleteProductById(UUID productId);
    List<Product> incrementStocksWhenOrderIsCancelled(Order order);
    void updateProductStock(Product product, int delta);
}