package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.model.Basket;

import java.util.List;
import java.util.Map;

public interface BasketService {

    Basket saveBasket(Basket theBasket);
    Basket findBasketById(Long basketId);
    List<Basket> getUserBaskets(Long userId);
    Basket getUserBasketById(Long userId, Long basketId);
    Basket updateBasketStatus(Long userId, Long basketId, Basket basketStatus);
    Basket deleteBasketById(Long userId, Long basketId);
}
