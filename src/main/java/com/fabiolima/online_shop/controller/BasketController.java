package com.fabiolima.online_shop.controller;

import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.service.BasketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user/{userId}/basket")
public class BasketController {

    private final BasketService basketService;

    @Autowired
    public BasketController(BasketService basketService){
        this.basketService = basketService;
    }

    @PostMapping()
    public ResponseEntity<Basket> createBasket(@PathVariable("userId") Long userId){

        Basket createdBasket = basketService.saveBasketAndAddToUser(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBasket);
    }

    @GetMapping
    public ResponseEntity<List<Basket>> getAllUsersBasket(@PathVariable("userId") Long userId){
        List<Basket> usersBaskets = basketService.getUserBaskets(userId);
        return ResponseEntity.ok(usersBaskets);
    }

    @GetMapping("/{basketId}")
    public ResponseEntity<Basket> getBasketById(@PathVariable("userId") Long userId,
                                                @PathVariable("basketId") Long basketId){
        Basket theBasket = basketService.getUserBasketById(userId,basketId);
        return ResponseEntity.ok(theBasket);
    }

    @PatchMapping("/{basketId}/checkout")
    public ResponseEntity<Basket> checkoutBasket(@PathVariable("userId") Long userId,
                                                 @PathVariable("basketId") Long basketId){
        Basket theBasket = basketService.checkOutBasket(userId, basketId);
        return ResponseEntity.ok(theBasket);
    }

    @DeleteMapping("/{basketId}")
    public ResponseEntity<Basket> deleteBasket(@PathVariable("userId") Long userId,
                                               @PathVariable("basketId") Long basketId) {
        Basket theBasket = basketService.deleteBasketById(userId, basketId);
        return ResponseEntity.ok(theBasket);
    }

    @PostMapping("/{basketId}/clear-basket")
    public ResponseEntity<Basket> clearBasket(@PathVariable("basketId") Long basketId){
        basketService.clearBasket(basketId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{basketId}/quant-of-items")
    public ResponseEntity<Integer> getTotalAmountOfItemsInBasket(@PathVariable("basketId") Long basketId){
        return ResponseEntity.ok(basketService.getTotalQuantity(basketId));
    }

    @GetMapping("{basketId}/total-price")
    public ResponseEntity<Double> getTotalBasketPrice(@PathVariable("basketId") Long basketId){
        return ResponseEntity.ok(basketService.calculateTotalPrice(basketId));
    }

}
