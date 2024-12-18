package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.exceptions.InsufficientStockException;
import com.fabiolima.online_shop.exceptions.NotFoundException;
import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.BasketItem;
import com.fabiolima.online_shop.model.Product;
import com.fabiolima.online_shop.repository.BasketItemRepository;
import com.fabiolima.online_shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BasketItemServiceImpl implements BasketItemService{

    @Autowired
    private BasketItemRepository basketItemRepository;
    @Autowired
    private BasketService basketService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public BasketItem addItemToBasket(Long basketId, Long productId, int quantity) {
        // fetch the basket
        Basket theBasket = basketService.findBasketById(basketId);

        // fetch product
        Product theProduct = productService.findProductById(productId);

        // Check if there's enough stock. If not it will throw an InsufficientStockException
        isStockAvailable(theProduct, quantity);

        // Check if item already exists in the basket, if so update item quantity
        BasketItem existingItem = theBasket.getBasketItems().stream()
                .filter(item -> item.getProduct().getId().equals(theProduct.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.incrementQuantity(quantity);
            return basketItemRepository.save(existingItem);
        }

        // if item doesn't exist create a new basket item
        BasketItem newItem = BasketItem.builder()
                .product(theProduct)
                .quantity(quantity)
                .build();

        // add item to basket
        theBasket.addBasketItemToBasket(newItem);

        // save the Basket containing the new item
        return basketItemRepository.save(newItem);
    }

    @Override
    public List<BasketItem> getItemsByBasket(Long basketId) {
        return basketService.findBasketById(basketId).getBasketItems();
    }

    @Override
    public BasketItem getItemById(Long basketItemId) {
        return basketItemRepository.findById(basketItemId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Item with Id %d was not found.",basketItemId)
                ));
    }

    @Override
    @Transactional
    // in case the user changes the quantity of a product directly providing the quantity they want
    public BasketItem updateBasketItem(Long basketItemId, int newQuantity) {
        BasketItem basketItem = getItemById(basketItemId);
        basketItem.setQuantity(newQuantity);

        Product product = basketItem.getProduct();

        // Check if there's enough stock. If not it will throw an InsufficientStockException
        isStockAvailable(product, newQuantity);

        //check if quantity is 0. If so, remove item.
        if(basketItem.getQuantity() == 0) {
            return removeItem(basketItemId);
        }

        // check if it's negative quantity and throw an exception if its the case
        if(basketItem.getQuantity() < 0)
            throw new IllegalArgumentException("Quantity must have a positive value.");

        return basketItemRepository.save(basketItem);
    }

    @Override
    @Transactional
    //in case the quantity is provided  by pressing a button that increment the quantity
    public BasketItem incrementItemQuantity(Long basketItemId) {
        BasketItem basketItem = getItemById(basketItemId);
        basketItem.incrementQuantity(1);

        Product product = basketItem.getProduct();

        // Check if there's enough stock. If not it will throw an InsufficientStockException
        isStockAvailable(product, basketItem.getQuantity());

        return basketItemRepository.save(basketItem);
    }

    @Override
    @Transactional
    public BasketItem decrementItemQuantity(Long basketItemId) {
        BasketItem basketItem = getItemById(basketItemId);
        if(basketItem.getQuantity() <= 1)
            throw new IllegalArgumentException("Cannot decrement quantity below 1.");

        basketItem.decrementQuantity(1);

        return basketItemRepository.save(basketItem);
    }

    @Override
    @Transactional
    public BasketItem removeItem(Long basketItemId) {
        BasketItem reference = getItemById(basketItemId);
        basketItemRepository.deleteById(basketItemId);
        return reference;
    }

    @Override
    public void isStockAvailable(Product product, int quantity) {
        if(product.getStock() < quantity)
            throw new InsufficientStockException(
                    String.format("Not enough stock available for product '%s'. Available: %d, Requested: %d",
                            product.getProductName(), product.getStock(), quantity));
    }


    @Override
    public double calculateItemTotalPrice(Long basketItemId) {
        BasketItem basketItem = getItemById(basketItemId);
        return (basketItem.getQuantity() * basketItem.getProduct().getProductPrice());
    }
}