package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.Product;
import com.fabiolima.online_shop.model.TheOrder;

import java.util.List;
import java.util.Map;

public interface ProductService {
    Product saveProduct(Product theProduct);
    List<Product> findAllProducts();
    Product findProductById(Long productId);
    Product patchUpdateProductById(Long productId, Map<String,Object> updates);
    Product deleteProductById(Long productId);
    void updateQuantInStock(TheOrder order);

}
