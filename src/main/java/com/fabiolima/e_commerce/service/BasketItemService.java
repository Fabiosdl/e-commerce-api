package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.model.BasketItem;
import com.fabiolima.e_commerce.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BasketItemService {

    BasketItem addItemToBasket(UUID basketId, UUID productId, int quantity);
    List<BasketItem> getItemsByBasket(UUID basketId);
    BasketItem getItemById(UUID basketItemId);
    BasketItem updateBasketItem(UUID basketId, UUID basketItemId, int newQuantity);
    BasketItem removeItemFromBasket(UUID basketId, UUID basketItemId);

    BasketItem incrementItemQuantity(UUID basketItemId);
    BasketItem decrementItemQuantity(UUID basketId, UUID basketItemId);

    void ensureStockAvailable(Product product, int quantity);

    BigDecimal calculateItemTotalPrice(UUID basketItemId);
}