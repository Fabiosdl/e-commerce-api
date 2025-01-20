package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.TheOrder;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.OrderService;
import com.fabiolima.e_commerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/{userId}/basket")
public class BasketController {

    private final BasketService basketService;
    private final UserService userService;
    private final OrderService orderService;

    @Autowired
    public BasketController(BasketService basketService, UserService userService, OrderService orderService){
        this.basketService = basketService;
        this.userService = userService;
        this.orderService = orderService;
    }

    @PostMapping()
    public ResponseEntity<Basket> createBasket(@PathVariable("userId") Long userId){

        User user = userService.findUserByUserId(userId);
        Basket createdBasket = basketService.createBasketAndAddToUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBasket);
    }

    @GetMapping
    public ResponseEntity<Page<Basket>> getAllUsersBasket(@PathVariable("userId") Long userId,
                                                          @RequestParam(defaultValue = "0") int pgNum,
                                                          @RequestParam(defaultValue = "25") int pgSize){
        Page<Basket> usersBaskets = basketService.getUserBaskets(pgNum, pgSize, userId);
        return ResponseEntity.ok(usersBaskets);
    }

    @GetMapping("/{basketId}")
    public ResponseEntity<Basket> getBasketById(@PathVariable("userId") Long userId,
                                                @PathVariable("basketId") Long basketId){
        Basket theBasket = basketService.getUserBasketById(userId,basketId);
        return ResponseEntity.ok(theBasket);
    }

    @PatchMapping("/{basketId}/checkout")
    public ResponseEntity<TheOrder> convertBasketToOrder(@PathVariable("userId") Long userId,
                                                 @PathVariable("basketId") Long basketId){
        TheOrder order = orderService.convertBasketToOrder(userId, basketId);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{basketId}")
    public ResponseEntity<Basket> inactivateBasket(@PathVariable("userId") Long userId,
                                               @PathVariable("basketId") Long basketId) {
        Basket theBasket = basketService.deactivateBasketById(userId, basketId);
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
