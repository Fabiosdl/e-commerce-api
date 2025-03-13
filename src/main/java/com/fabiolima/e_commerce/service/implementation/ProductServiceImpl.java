package com.fabiolima.e_commerce.service.implementation;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.InsufficientStockException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.BasketItem;
import com.fabiolima.e_commerce.model.Product;
import com.fabiolima.e_commerce.model.Order;
import com.fabiolima.e_commerce.repository.ProductRepository;
import com.fabiolima.e_commerce.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    @Override
    public Product saveProduct(Product theProduct) {
        return productRepository.save(theProduct);
    }

    @Override
    public Page<Product> findAllProducts(int pgNum, int pgSize) {

        Pageable pageable = PageRequest.of(pgNum, pgSize);
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> findProductsByCategory(int pgNum, int pgSize, String category) {

        Pageable pageable = PageRequest.of(pgNum, pgSize);
        return productRepository.findAllByCategory(category,pageable);
    }

    @Override
    public Product findProductById(Long productId) {
        //check if userId and basketId are valid
        if(productId == null)
            throw new IllegalArgumentException ("Product id cannot be null");
        if(productId <= 0L)
            throw new IllegalArgumentException ("Product id must be greater than 0");
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
                case "productPrice" : theProduct.setProductPrice(new BigDecimal(value.toString())); break;
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
    //give back the item quantity to product stock if order is cancelled
    public List<Product> incrementStocksWhenOrderIsCancelled(Order order) {

        if(order == null)
            throw new IllegalArgumentException("Order cannot be null");

        List<Product> updatedProductStock = new ArrayList<>();

        // retrieve the basket that originated the order
        Basket theBasket = order.getBasket();

        // iterate through the list of items in basket and return its quantities to stock
        for (BasketItem item : theBasket.getBasketItems()) {
            // retrieve quantity of an item in basket
            int quantity = item.getQuantity();
            // retrieve the product used in item
            Product product = item.getProduct();
            // retrieve current quantity in stock
            int currentStock = product.getStock();
            // update stock quantity
            product.setStock(currentStock + quantity);
            // save product with updated stock quantity
            saveProduct(product);
            updatedProductStock.add(product);
        }
        log.info("{} items were sent back to stock", updatedProductStock.size());
        return updatedProductStock;
    }

    @Override
    public void updateProductStock(Product product, int delta){

        /** Example of how the method works
         * initial product stock = 10 units
         * Firstly user puts 4 quantity of an item in basket
         * current product stock = 10 - 4 .: current stock = 6 units
         * now user wants 7 units INSTEAD of 4 units
         * delta = new item quantity - current item quantity
         * delta = 7 - 4 .: delta = 3
         * updated product stock = current stock - delta
         * updated stock = 6 - 3 = 3 units.
         *
         * It can be proved by getting the initial stock = 10, minus items in basket = 7
         * which results in a updated stock of 3 units
         */
        if(product == null)
            throw new IllegalArgumentException("Product cannot be null");

        int currentStock = product.getStock();
        int updatedStock = currentStock - delta;

        if(updatedStock < 0)
            throw new InsufficientStockException(String.format("Not enough stock available for product '%s'. Available: %d, Requested: %d",
                    product.getProductName(), product.getStock(), delta));

        product.setStock(updatedStock);
        productRepository.save(product);
    }
}
