package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.Basket;

import java.util.List;
public interface BasketService {

    Basket saveBasketAndAddToUser(Long userId);
    List<Basket> getUserBaskets(Long userId);
    Basket getUserBasketById(Long userId, Long basketId);
    Basket checkOutBasket(Long userId, Long basketId);
    Basket updateBasketWhenItemsAreAddedOrModified(Basket basket);
    Basket deleteBasketById(Long userId, Long basketId);
    Basket clearBasket(Long basketId);
    void deleteExpiredBasket();
    Basket findBasketById(Long basketId);

    int getTotalQuantity(Long basketId);

    double calculateTotalPrice(Long basketId);
}
