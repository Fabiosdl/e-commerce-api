package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.entities.BasketItem;
import com.fabiolima.e_commerce.service.BasketItemService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/basket/{basketId}/item")
@PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
public class BasketItemController {

    private final BasketItemService basketItemService;
    @Autowired
    public BasketItemController(BasketItemService basketItemService){
        this.basketItemService = basketItemService;
    }

    @Operation(summary = "Add items to basket")
    @PostMapping
    public ResponseEntity<BasketItem> addItemToBasket(@PathVariable("basketId") UUID basketId,
                                                       @RequestParam UUID productId,
                                                       @RequestParam int quant){

        BasketItem item = basketItemService.addItemToBasket(basketId, productId, quant);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @Operation(summary = "Retrieve all items in a basket")
    @GetMapping
    public ResponseEntity<List<BasketItem>> getAllItemsInBasket(@PathVariable("basketId") UUID basketId){
        return ResponseEntity.ok(basketItemService.getItemsByBasket(basketId));
    }

    @Operation(summary = "Retrieve item by its id")
    @GetMapping("/{itemId}")
    public ResponseEntity<BasketItem> getItemById(@PathVariable("basketId") UUID basketId,
                                                  @PathVariable("itemId") UUID itemId){
        return ResponseEntity.ok(basketItemService.getItemById(itemId));
    }
    /*
    * Everytime the user update the quantity, the API performs stock validations,
    * then @PostMapping aligns better with thw API design, as the operation is more than just updating a field
    * */
    // increment quantity one by one in item
    @Operation(summary = "Increment the item quantity by the value of One in basket and decrement product stock")
    @PostMapping("/{itemId}/increment")
    public ResponseEntity<BasketItem> incrementItemInBasket(@PathVariable("basketId") UUID basketId,
                                                            @PathVariable("itemId") UUID itemId){

        BasketItem incrementedItem = basketItemService.incrementItemQuantity(itemId);
        return ResponseEntity.ok(incrementedItem);
    }

    // decrement quantity one by one in item
    @Operation(summary = "Decrement the item quantity by the value of One in basket and increment product stock")
    @PostMapping("/{itemId}/decrement")
    public ResponseEntity<BasketItem> decrementItemInBasket(@PathVariable("basketId") UUID basketId,
                                                            @PathVariable("itemId") UUID itemId){
        BasketItem decrementedItem = basketItemService.decrementItemQuantity(basketId, itemId);
        return ResponseEntity.ok(decrementedItem);
    }

    // change items quantity in basket
    @Operation(summary = "Update item quantity defined by customer. " +
            "Useful if website allows user to manually set the quantity. It also update product stock")
    @PostMapping("/{itemId}")
    public ResponseEntity<BasketItem> updateItemQuantityInBasket(@PathVariable("basketId") UUID basketId,
                                                                 @PathVariable("itemId") UUID itemId,
                                                                 @RequestParam int quant){
        BasketItem updatedBasketItem = basketItemService.updateBasketItem(basketId, itemId, quant);
        return ResponseEntity.ok(updatedBasketItem);
    }

    // get the total item price
    @Operation(summary = "Retrieve the total price of an item")
    @GetMapping("/{itemId}/total-price")
    public ResponseEntity<BigDecimal> getTotalItemPrice(@PathVariable("itemId") UUID itemId){
        return ResponseEntity.ok(basketItemService.calculateItemTotalPrice(itemId));
    }

    // remove item
    @Operation(summary = "Remove item from basket, and update product stock")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeItemFromBasket(@PathVariable("basketId") UUID basketId,
                                                     @PathVariable("itemId") UUID itemId){
        basketItemService.removeItemFromBasket(basketId,itemId);
        return ResponseEntity.noContent().build();
    }
}