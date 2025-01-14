package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.Product;
import com.fabiolima.online_shop.model.TheOrder;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ProductService {
    Product saveProduct(Product theProduct);
    Page<Product> findAllProducts(int pgNum, int pgSize);
    Page<Product> findProductsByCategory(int pgNum, int pgSize, String category);
    Product findProductById(Long productId);
    Product patchUpdateProductById(Long productId, Map<String,Object> updates);
    Product deleteProductById(Long productId);
    List<Product> incrementStocksWhenOrderIsCancelled(TheOrder order);
    void updateProductStock(Product product, int delta);

}
