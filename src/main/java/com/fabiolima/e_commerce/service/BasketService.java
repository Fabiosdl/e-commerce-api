package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.BasketItem;
import com.fabiolima.e_commerce.model.User;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.UUID;

public interface BasketService {

    Basket createBasketAndAddToUser(User user);
    Page<Basket> getUserBaskets(int pgNum, int pgSize, UUID userId);
    Basket updateBasketWhenItemsAreAddedOrModified(Basket basket);
    Basket deactivateBasketById(UUID userId, UUID basketId);
    Basket clearBasket(UUID basketId);
    void deleteExpiredBasketAndAddNewOne();
    Basket checkoutBasket(UUID userId, UUID basketId);
    Basket findBasketById(UUID basketId);
    int getTotalQuantity(UUID basketId);
    BigDecimal calculateTotalPrice(UUID basketId);
    BasketItem removeItemFromBasket(Basket basket, BasketItem item);
    Basket returnNewestActiveBasket(User user);
}
