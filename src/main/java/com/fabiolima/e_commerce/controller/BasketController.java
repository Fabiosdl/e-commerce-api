package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.Order;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.OrderService;
import com.fabiolima.e_commerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
    public ResponseEntity<Basket> getBasketById(@PathVariable("userId") Long userId,
                                                @PathVariable("basketId") Long basketId){
        Basket theBasket = basketService.getUserBasketById(userId,basketId);
        return ResponseEntity.ok(theBasket);
    }

    @Operation(summary = "Retrieve newest Active basket - Useful for the frontend to have always a valid basket to use")
    @GetMapping("/active-basket")
    public ResponseEntity<Basket> getNewestActiveBasket(@PathVariable("userId") Long userId){
        User user = userService.findUserByUserId(userId);
        return  ResponseEntity.ok(basketService.returnNewestActiveBasket(user));
    }

    @Operation(summary = "Creates new basket/cart for the user when it expires or is checked out")
    @PostMapping()
    public ResponseEntity<Basket> createBasket(@PathVariable("userId") Long userId){

        User user = userService.findUserByUserId(userId);
        Basket createdBasket = basketService.createBasketAndAddToUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBasket);
    }

    /**
     * Making user of HATEOAS to bind a link to get the order originated when the basket has become CHECKED_OUT
     */
    @Operation(summary = "Checks out a basket and creates an order for payment")
    @PatchMapping("/{basketId}/checkout")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Basket> convertBasketToOrder(@PathVariable("userId") Long userId,
                                                   @PathVariable("basketId") Long basketId){
        //1 - Change basket status to CHECKED_OUT
        Basket basket = basketService.checkoutBasket(userId, basketId);

        //2 - Generate the order from basket and add to user
        Order order = orderService.createOrderAndAddToUser(userId, basket);

        return ResponseEntity.ok(basket);
    }

    @Operation(summary = "Deactivate a basket if user don't update it for 1 day, giving back all items to product stock")
    @DeleteMapping("/{basketId}")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Basket> deactivateBasket(@PathVariable("userId") Long userId,
                                               @PathVariable("basketId") Long basketId) {
        Basket theBasket = basketService.deactivateBasketById(userId, basketId);
        return ResponseEntity.ok(theBasket);
    }

    @Operation(summary = "Remove all items in basket")
    @PostMapping("/{basketId}/clear-basket")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Basket> clearBasket(@PathVariable("basketId") Long basketId){
        Basket basket = basketService.clearBasket(basketId);
        return ResponseEntity.ok(basket);
    }

    @Operation(summary = "Retrieve the total amount of items in a basket")
    @GetMapping("/{basketId}/quant-of-items")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Integer> getTotalAmountOfItemsInBasket(@PathVariable("basketId") Long basketId){
        return ResponseEntity.ok(basketService.getTotalQuantity(basketId));
    }

    @Operation(summary = "Retrieves the total price of a basket")
    @GetMapping("{basketId}/total-price")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<BigDecimal> getTotalBasketPrice(@PathVariable("basketId") Long basketId){
        return ResponseEntity.ok(basketService.calculateTotalPrice(basketId));
    }
}