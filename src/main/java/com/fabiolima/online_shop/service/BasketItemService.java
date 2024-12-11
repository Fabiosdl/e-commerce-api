package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.BasketItem;

import java.util.List;

public interface BasketItemService {

    BasketItem addItemToBasket(Long basketId, Long productId, int quantity);
    List<BasketItem> getItemsByBasket(Long basketId);
    BasketItem getItemById(Long basketItemId);
    BasketItem updateBasketItem(Long basketItemId, int newQuantity);
    BasketItem removeItem(Long basketItemId);
    void clearBasket(Long basketId);

    BasketItem incrementItemQuantity(Long basketItemId, int amount);
    BasketItem decrementItemQuantity(Long basketItemId, int amount);

    boolean isStockAvailable(Long productId, int quantity);
    boolean validateItemQuantity(Long basketItemId);

    double calculateItemTotalPrice(Long basketItemId);

    int getTotalQuantity(Long basketId);
    double calculateTotalPrice(Long basketId);

}