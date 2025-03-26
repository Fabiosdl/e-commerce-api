package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.entities.Basket;
import com.fabiolima.e_commerce.entities.User;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.OrderService;
import com.fabiolima.e_commerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

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

    @Operation(summary = "Retrieve basket by its id")
    @GetMapping("/{basketId}")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Basket> getBasketById(@PathVariable("userId") UUID userId,
                                                @PathVariable("basketId") UUID basketId){
        Basket theBasket = basketService.findBasketById(basketId);
        return ResponseEntity.ok(theBasket);
    }

    @Operation(summary = "Retrieve newest Active basket - Useful for the frontend to have always a valid basket to use")
    @GetMapping("/active-basket")
    public ResponseEntity<Basket> getNewestActiveBasket(@PathVariable("userId") UUID userId){
        User user = userService.findUserByUserId(userId);
        return  ResponseEntity.ok(basketService.returnNewestActiveBasket(user));
    }

    @Operation(summary = "Creates new basket/cart for the user when it expires or is checked out")
    @PostMapping()
    public ResponseEntity<Basket> createBasket(@PathVariable("userId") UUID userId){

        User user = userService.findUserByUserId(userId);
        Basket createdBasket = basketService.createBasketAndAddToUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBasket);
    }

    @Operation(summary = "Change Basket status to checked out. A checked out basket cannot be updated anymore.")
    @PatchMapping("/{basketId}/checkout")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Basket> convertBasketToOrder(@PathVariable("userId") UUID userId,
                                                   @PathVariable("basketId") UUID basketId){
        //1 - Change basket status to CHECKED_OUT
        /**

         Basket should only be checked out after order sent for payment.
         This is necessary because after basket is checked out, it cannot be modified.
         In a situation that the user clicks on check out, goes to order page where they
         can pay for the order, but decides to buy another item, it is still possible if the
         basket is as Active status. Therefore, the basket will only be checked out after
         the user decides to pay for the order, and be redirected to PayPal page.
        */
        Basket basket = basketService.checkoutBasket(userId, basketId);

        return ResponseEntity.ok(basket);
    }

    @Operation(summary = "Deactivate a basket if user don't update it for 1 day, giving back all items to product stock")
    @DeleteMapping("/{basketId}")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Basket> deactivateBasket(@PathVariable("userId") UUID userId,
                                               @PathVariable("basketId") UUID basketId) {
        Basket theBasket = basketService.deactivateBasketById(userId, basketId);
        return ResponseEntity.ok(theBasket);
    }

    @Operation(summary = "Remove all items in basket")
    @PostMapping("/{basketId}/clear-basket")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Basket> clearBasket(@PathVariable("basketId") UUID basketId){
        Basket basket = basketService.clearBasket(basketId);
        return ResponseEntity.ok(basket);
    }

    @Operation(summary = "Retrieve the total amount of items in a basket")
    @GetMapping("/{basketId}/quant-of-items")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Integer> getTotalAmountOfItemsInBasket(@PathVariable("basketId") UUID basketId){
        return ResponseEntity.ok(basketService.getTotalQuantity(basketId));
    }

    @Operation(summary = "Retrieves the total price of a basket")
    @GetMapping("{basketId}/total-price")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<BigDecimal> getTotalBasketPrice(@PathVariable("basketId") UUID basketId){
        return ResponseEntity.ok(basketService.calculateTotalPrice(basketId));
    }
}