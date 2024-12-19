package com.fabiolima.online_shop.controller;

import com.fabiolima.online_shop.model.BasketItem;
import com.fabiolima.online_shop.service.BasketItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/basket/{basketId}/item")
public class BasketItemController {

    @Autowired
    private BasketItemService basketItemService;

    // add item to basket
    @PostMapping
    public ResponseEntity<BasketItem> createBasketItem(@PathVariable("basketId") Long basketId,
                                                       @RequestParam Long productId,
                                                       @RequestParam int quant){
        BasketItem item = basketItemService.addItemToBasket(basketId, productId, quant);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    // get all items in a basket
    @GetMapping
    public ResponseEntity<List<BasketItem>> getAllItemsInBasket(@PathVariable("basketId") Long basketId){
        return ResponseEntity.ok(basketItemService.getItemsByBasket(basketId));
    }

    // get item in basket
    @GetMapping("/{itemId}")
    public ResponseEntity<BasketItem> getItemById(@PathVariable("basketId") Long basketId,
                                                  @PathVariable("itemId") Long itemId){
        return ResponseEntity.ok(basketItemService.getItemById(itemId));
    }
    /*
    * Everytime the user update the quantity, the API performs stock validations,
    * then @PostMapping aligns better with thw API design, as the operation is more than just updating a field
    * */
    // increment quantity one by one in item
    @PostMapping("/{itemId}/increment")
    public ResponseEntity<BasketItem> incrementItemInBasket(@PathVariable("itemId") Long itemId){
        BasketItem incrementedItem = basketItemService.incrementItemQuantity(itemId);
        return ResponseEntity.ok(incrementedItem);
    }

    // decrement quantity one by one in item
    @PostMapping("/{itemId}/decrement")
    public ResponseEntity<BasketItem> decrementItemInBasket(@PathVariable("itemId") Long itemId){
        BasketItem decrementedItem = basketItemService.decrementItemQuantity(itemId);
        return ResponseEntity.ok(decrementedItem);
    }

    // change items quantity in basket
    @PostMapping("/{itemId}")
    public ResponseEntity<BasketItem> updateItemQuantityInBasket(@PathVariable("itemId") Long itemId,
                                                                 @RequestParam int quant){
        BasketItem updatedBasketItem = basketItemService.updateBasketItem(itemId, quant);
        return ResponseEntity.ok(updatedBasketItem);
    }

    // get the total item price
    @GetMapping("/{itemId}/total-price")
    public ResponseEntity<Double> getTotalItemPrice(@PathVariable("itemId") Long itemId){
        return ResponseEntity.ok(basketItemService.calculateItemTotalPrice(itemId));
    }

    // remove item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeItemFromBasket(@PathVariable("itemId") Long itemId){
        basketItemService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }

}
