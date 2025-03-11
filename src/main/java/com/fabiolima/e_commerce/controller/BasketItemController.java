package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.model.BasketItem;
import com.fabiolima.e_commerce.service.BasketItemService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
    public ResponseEntity<BasketItem> addItemToBasket(@PathVariable("basketId") Long basketId,
                                                       @RequestParam Long productId,
                                                       @RequestParam int quant){

        BasketItem item = basketItemService.addItemToBasket(basketId, productId, quant);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @Operation(summary = "Retrieve all items in a basket")
    @GetMapping
    public ResponseEntity<List<BasketItem>> getAllItemsInBasket(@PathVariable("basketId") Long basketId){
        return ResponseEntity.ok(basketItemService.getItemsByBasket(basketId));
    }

    @Operation(summary = "Retrieve item by its id")
    @GetMapping("/{itemId}")
    @PreAuthorize("@orderAuthenticationService.isOwner(#itemId, authentication)")
    public ResponseEntity<BasketItem> getItemById(@PathVariable("basketId") Long basketId,
                                                  @PathVariable("itemId") Long itemId){
        return ResponseEntity.ok(basketItemService.getItemById(itemId));
    }
    /*
    * Everytime the user update the quantity, the API performs stock validations,
    * then @PostMapping aligns better with thw API design, as the operation is more than just updating a field
    * */
    // increment quantity one by one in item
    @Operation(summary = "Increment the item quantity by the value of One in basket and decrement product stock")
    @PostMapping("/{itemId}/increment")
    @PreAuthorize("@orderAuthenticationService.isOwner(#itemId, authentication)")
    public ResponseEntity<BasketItem> incrementItemInBasket(@PathVariable("itemId") Long itemId){
        BasketItem incrementedItem = basketItemService.incrementItemQuantity(itemId);
        return ResponseEntity.ok(incrementedItem);
    }

    // decrement quantity one by one in item
    @Operation(summary = "Decrement the item quantity by the value of One in basket and increment product stock")
    @PostMapping("/{itemId}/decrement")
    @PreAuthorize("@orderAuthenticationService.isOwner(#itemId, authentication)")
    public ResponseEntity<BasketItem> decrementItemInBasket(@PathVariable("basketId") Long basketId,
                                                            @PathVariable("itemId") Long itemId){
        BasketItem decrementedItem = basketItemService.decrementItemQuantity(basketId, itemId);
        return ResponseEntity.ok(decrementedItem);
    }

    // change items quantity in basket
    @Operation(summary = "Update item quantity defined by customer. " +
            "Useful if website allows user to manually set the quantity. It also update product stock")
    @PostMapping("/{itemId}")
    @PreAuthorize("@orderAuthenticationService.isOwner(#itemId, authentication)")
    public ResponseEntity<BasketItem> updateItemQuantityInBasket(@PathVariable("basketId") Long basketId,
                                                                 @PathVariable("itemId") Long itemId,
                                                                 @RequestParam int quant){
        BasketItem updatedBasketItem = basketItemService.updateBasketItem(basketId, itemId, quant);
        return ResponseEntity.ok(updatedBasketItem);
    }

    // get the total item price
    @Operation(summary = "Retrieve the total price of an item")
    @GetMapping("/{itemId}/total-price")
    @PreAuthorize("@orderAuthenticationService.isOwner(#itemId, authentication)")
    public ResponseEntity<BigDecimal> getTotalItemPrice(@PathVariable("itemId") Long itemId){
        return ResponseEntity.ok(basketItemService.calculateItemTotalPrice(itemId));
    }

    // remove item
    @Operation(summary = "Remove item from basket, and update product stock")
    @DeleteMapping("/{itemId}")
    @PreAuthorize("@orderAuthenticationService.isOwner(#itemId, authentication)")
    public ResponseEntity<Void> removeItemFromBasket(@PathVariable("basketId") Long basketId,
                                                     @PathVariable("itemId") Long itemId){
        basketItemService.removeItemFromBasket(basketId,itemId);
        return ResponseEntity.noContent().build();
    }

}
