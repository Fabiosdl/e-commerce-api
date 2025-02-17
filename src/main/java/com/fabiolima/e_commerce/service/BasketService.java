package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.BasketItem;
import com.fabiolima.e_commerce.model.User;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface BasketService {

    Basket createBasketAndAddToUser(User user);
    Page<Basket> getUserBaskets(int pgNum, int pgSize, Long userId);
    Basket getUserBasketById(Long userId, Long basketId);
    Basket updateBasketWhenItemsAreAddedOrModified(Basket basket);
    Basket deactivateBasketById(Long userId, Long basketId);
    Basket clearBasket(Long basketId);
    void deleteExpiredBasketAndAddNewOne();
    Basket checkoutBasket(Long userId, Long basketId);
    Basket findBasketById(Long basketId);
    int getTotalQuantity(Long basketId);
    BigDecimal calculateTotalPrice(Long basketId);
    BasketItem removeItemFromBasket(Basket basket, BasketItem item);
    Basket validateAndFetchBasket(Long userId, Long basketId);
    Basket returnNewestActiveBasket(User user);
}
