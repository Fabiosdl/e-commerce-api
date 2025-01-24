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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("user/{userId}/basket")
@PreAuthorize("@userAuthenticationService.isOwner(#userId, authentication)")
public class BasketController {

    private final BasketService basketService;
    private final UserService userService;
    private final OrderService orderService;
    private final OrderController orderController;

    @Autowired
    public BasketController(BasketService basketService, UserService userService, OrderService orderService, OrderController orderController){
        this.basketService = basketService;
        this.userService = userService;
        this.orderService = orderService;
        this.orderController = orderController;
    }

    @Operation(summary = "Retrieve basket by its id")
    @GetMapping("/{basketId}")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Basket> getBasketById(@PathVariable("userId") Long userId,
                                                @PathVariable("basketId") Long basketId){
        Basket theBasket = basketService.getUserBasketById(userId,basketId);
        return ResponseEntity.ok(theBasket);
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
    @Operation(summary = "Checks-out a basket and creates a new order to user")
    @PatchMapping("/{basketId}/checkout")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<EntityModel<Basket>> convertBasketToOrder(@PathVariable("userId") Long userId,
                                                   @PathVariable("basketId") Long basketId){
        //1 - Change basket status to CHECKED_OUT
        Basket basket = basketService.checkoutBasket(userId, basketId);

        //2 - Generate the order from basket and add to user
        Order order = orderService.createOrderAndAddToUser(userId, basket);

        //apply entity model to the order object
        EntityModel<Basket> entityModel = EntityModel.of(basket);

        //create a link to the basket that now has status CHECKED_OUT
        WebMvcLinkBuilder link = WebMvcLinkBuilder.linkTo(methodOn(OrderController.class).getUsersOrderByOrderId(userId,order.getId()));
        //Bind the link to the entityModel
        entityModel.add(link.withRel("Order Created From Basket"));

        return ResponseEntity.ok(entityModel);
    }

    @Operation(summary = "Deactivate a basket if user don't update it for 1 day, giving back all items to product stock")
    @DeleteMapping("/{basketId}")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Basket> inactivateBasket(@PathVariable("userId") Long userId,
                                               @PathVariable("basketId") Long basketId) {
        Basket theBasket = basketService.deactivateBasketById(userId, basketId);
        return ResponseEntity.ok(theBasket);
    }

    @Operation(summary = "Remove all items in basket")
    @PostMapping("/{basketId}/clear-basket")
    @PreAuthorize("@basketAuthenticationService.isOwner(#basketId, authentication)")
    public ResponseEntity<Basket> clearBasket(@PathVariable("basketId") Long basketId){
        basketService.clearBasket(basketId);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Double> getTotalBasketPrice(@PathVariable("basketId") Long basketId){
        return ResponseEntity.ok(basketService.calculateTotalPrice(basketId));
    }
}