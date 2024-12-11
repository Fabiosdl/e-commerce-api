package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.exceptions.ForbiddenException;
import com.fabiolima.online_shop.exceptions.NotFoundException;
import com.fabiolima.online_shop.model.Product;
import com.fabiolima.online_shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product saveProduct(Product theProduct) {
        return productRepository.save(theProduct);
    }

    @Override
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(String.format("Product with Id %d not found",productId)));
    }

    @Override
    public Product patchUpdateProductById(Long productId, Map<String, Object> updates) {

        // check if the product exists
        Product theProduct = findProductById(productId);

        updates.forEach((field,value) -> {

            switch(field){

                case "productName" : theProduct.setProductName((String) value); break;
                case "productDescription" : theProduct.setProductDescription((String) value); break;
                case "productPrice" : theProduct.setProductPrice((double) value); break;
                case "stock" : theProduct.setStock((Integer) value); break;
                case "category" : theProduct.setCategory((String) value); break;
                default: throw new ForbiddenException("Field not found or not allowed to update");
            }

        });
        return saveProduct(theProduct);
    }

    @Override
    public Product deleteProductById(Long productId) {
        Product reference = findProductById(productId);
        productRepository.deleteById(productId);
        return reference;
    }
}
