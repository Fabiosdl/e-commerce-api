package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.BasketItem;
import com.fabiolima.e_commerce.model.User;
import org.springframework.data.domain.Page;

public interface BasketService {

    Basket createBasketAndAddToUser(User user);
    Page<Basket> getUserBaskets(int pgNum, int pgSize, Long userId);
    Basket getUserBasketById(Long userId, Long basketId);
    Basket updateBasketWhenItemsAreAddedOrModified(Basket basket);
    Basket deactivateBasketById(Long userId, Long basketId);
    Basket clearBasket(Long basketId);
    void clearExpiredBasketAndAddNewOne();
    Basket checkoutBasket(Long userId, Long basketId);
    Basket findBasketById(Long basketId);
    int getTotalQuantity(Long basketId);
    double calculateTotalPrice(Long basketId);
    BasketItem removeItemFromBasket(Basket basket, Long itemId);
    Basket validateAndFetchBasket(Long userId, Long basketId);
}
