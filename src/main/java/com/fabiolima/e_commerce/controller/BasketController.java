package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.Order;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.OrderService;
import com.fabiolima.e_commerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/{userId}/basket")
@PreAuthorize("@userAuthenticationService.isOwner(#userId, authentication)")
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

    @Operation(summary = "Creates new basket/cart for the user when it expires or is checked out")
    @PostMapping()
    public ResponseEntity<Basket> createBasket(@PathVariable("userId") Long userId){

        User user = userService.findUserByUserId(userId);
        Basket createdBasket = basketService.createBasketAndAddToUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBasket);
    }

    @Operation(summary = "Checks-out a basket and creates a new order to user")
    @PatchMapping("/{basketId}/checkout")
    public ResponseEntity<Order> convertBasketToOrder(@PathVariable("userId") Long userId,
                                                      @PathVariable("basketId") Long basketId){
        Order order = orderService.convertBasketToOrder(userId, basketId);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Deactivate a basket if user don't update it for 1 day, giving back all items to product stock")
    @DeleteMapping("/{basketId}")
    public ResponseEntity<Basket> inactivateBasket(@PathVariable("userId") Long userId,
                                               @PathVariable("basketId") Long basketId) {
        Basket theBasket = basketService.deactivateBasketById(userId, basketId);
        return ResponseEntity.ok(theBasket);
    }

    @Operation(summary = "Remove all items in basket")
    @PostMapping("/{basketId}/clear-basket")
    public ResponseEntity<Basket> clearBasket(@PathVariable("basketId") Long basketId){
        basketService.clearBasket(basketId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Retrieve the total amount of items in a basket")
    @GetMapping("/{basketId}/quant-of-items")
    public ResponseEntity<Integer> getTotalAmountOfItemsInBasket(@PathVariable("basketId") Long basketId){
        return ResponseEntity.ok(basketService.getTotalQuantity(basketId));
    }

    @Operation(summary = "Retrieves the total price of a basket")
    @GetMapping("{basketId}/total-price")
    public ResponseEntity<Double> getTotalBasketPrice(@PathVariable("basketId") Long basketId){
        return ResponseEntity.ok(basketService.calculateTotalPrice(basketId));
    }
}