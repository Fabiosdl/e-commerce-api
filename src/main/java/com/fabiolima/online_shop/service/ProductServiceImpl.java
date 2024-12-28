package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.exceptions.ForbiddenException;
import com.fabiolima.online_shop.exceptions.NotFoundException;
import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.BasketItem;
import com.fabiolima.online_shop.model.Product;
import com.fabiolima.online_shop.model.TheOrder;
import com.fabiolima.online_shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
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

    @Override
    @Transactional
    public void updateQuantInStock(TheOrder order) {
        // retrieve the basket that originated the order
        Basket theBasket = order.getBasket();

        // iterate through the list of items in basket and return its quantities to stock
        for (BasketItem item : theBasket.getBasketItems()) {
            // retrieve quantity in basket
            int quantity = item.getQuantity();
            // retrieve the product used in item
            Product product = item.getProduct();
            // retrieve current quantity in stock
            int currentStock = product.getStock();
            // update stock quantity
            if (currentStock < quantity) {
                throw new IllegalStateException("Not enough stock for product: " + product.getProductName());
            }
            product.setStock(currentStock - quantity);
            // save product with updated stock quantity
            saveProduct(product);
        }
    }
}
