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

        // Check if there's enough stock
        if (!isStockAvailable(productId, quantity)) {
            throw new InsufficientStockException("Not enough stock available");
        }

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
    public BasketItem updateBasketItem(Long basketItemId, int newQuantity) {
        BasketItem basketItem = getItemById(basketItemId);
        basketItem.setQuantity(newQuantity);
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
    @Transactional
    public void clearBasket(Long basketId) {
        Basket theBasket = basketService.findBasketById(basketId);
        theBasket.getBasketItems().clear();
    }

    @Override
    @Transactional
    public BasketItem incrementItemQuantity(Long basketItemId, int amount) {
        BasketItem basketItem = getItemById(basketItemId);
        basketItem.incrementQuantity(amount);
        return basketItem;
    }

    @Override
    @Transactional
    public BasketItem decrementItemQuantity(Long basketItemId, int amount) {
        BasketItem basketItem = getItemById(basketItemId);
        basketItem.decrementQuantity(amount);
        return basketItem;
    }

    @Override
    public boolean isStockAvailable(Long productId, int quantity) {
        Product theProduct = productService.findProductById(productId);
        return (theProduct.getStock() > 0);
    }

    @Override
    public boolean validateItemQuantity(Long basketItemId) {
        BasketItem basketItem = getItemById(basketItemId);
        return (basketItem.getQuantity() > 0
                && basketItem.getQuantity() <= basketItem.getProduct().getStock());
    }

    @Override
    public double calculateItemTotalPrice(Long basketItemId) {
        BasketItem basketItem = getItemById(basketItemId);
        return (basketItem.getQuantity() * basketItem.getProduct().getProductPrice());
    }

    @Override
    public int getTotalQuantity(Long basketId) {
        int totalQuant = 0;
        Basket theBasket = basketService.findBasketById(basketId);
        for(BasketItem item : theBasket.getBasketItems()){
            totalQuant += item.getQuantity();
        }
        return totalQuant;
    }

    @Override
    public double calculateTotalPrice(Long basketId) {

        Basket theBasket = basketService.findBasketById(basketId);
        return theBasket.getBasketItems().stream()
                .mapToDouble(basketItem -> basketItem.getQuantity() * basketItem.getProduct().getProductPrice())
                .sum();
    }
}