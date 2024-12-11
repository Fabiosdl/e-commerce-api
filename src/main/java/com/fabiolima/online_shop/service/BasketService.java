package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.Basket;

import java.util.List;
public interface BasketService {

    Basket saveBasketAndAddToUser(Long userId, Basket theBasket);
    List<Basket> getUserBaskets(Long userId);
    Basket getUserBasketById(Long userId, Long basketId);
    Basket checkOutBasket(Long userId, Long basketId);
    Basket deleteBasketById(Long userId, Long basketId);

    Basket findBasketById(Long basketId);
}
