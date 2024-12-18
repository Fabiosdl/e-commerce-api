package com.fabiolima.online_shop.controller;

import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.service.BasketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user/{userId}/basket")
public class BasketController {

    private BasketService basketService;

    @PostMapping()
    public ResponseEntity<Basket> createBasket(@PathVariable("userId") Long userId,
                                               @RequestBody Basket theBasket){
        Basket createdBasket = basketService.saveBasketAndAddToUser(userId, theBasket);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBasket);
    }

    @GetMapping
    public ResponseEntity<List<Basket>> getAllUsersBasket(@PathVariable("userId") Long userId){
        List<Basket> usersBaskets = basketService.getUserBaskets(userId);
        return ResponseEntity.ok(usersBaskets);
    }

    @GetMapping("/{basketId}")
    public ResponseEntity<Basket> getBasketById(@PathVariable("basketId") Long basketId){
        Basket theBasket = basketService.findBasketById(basketId);
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

    @PostMapping("/{basketId}/clearBasket")
    public ResponseEntity<Basket> clearBasket(@PathVariable("basketId") Long basketId){
        basketService.clearBasket(basketId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{basketId}/quantOfItems")
    public ResponseEntity<Integer> getTotalAmountOfItemsInBasket(@PathVariable("basketId") Long basketId){
        return ResponseEntity.ok(basketService.getTotalQuantity(basketId));
    }

    @GetMapping("{basketId}/totalPrice")
    public ResponseEntity<Double> getTotalBasketPrice(@PathVariable("basketId") Long basketId){
        return ResponseEntity.ok(basketService.calculateTotalPrice(basketId));
    }

}
