package com.fabiolima.e_commerce.controller.admin;

import com.fabiolima.e_commerce.entities.Basket;
import com.fabiolima.e_commerce.service.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class ManageBasketController {
    private final BasketService basketService;

    @Autowired
    public ManageBasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    @Operation(summary = "Retrieve all the baskets of an user")
    @GetMapping("/{adminId}/user/{userId}")
    public ResponseEntity<Page<Basket>> getAllUsersBasket(@PathVariable("userId") UUID userId,
                                                          @RequestParam(defaultValue = "0") int pgNum,
                                                          @RequestParam(defaultValue = "25") int pgSize){
        Page<Basket> usersBaskets = basketService.getUserBaskets(pgNum, pgSize, userId);
        return ResponseEntity.ok(usersBaskets);
    }


    @Operation(summary = "Retrieve basket by its id")
    @GetMapping("/{basketId}")
    public ResponseEntity<Basket> getBasketById(@PathVariable("userId") UUID userId,
                                                @PathVariable("basketId") UUID basketId){
        Basket theBasket = basketService.findBasketById(basketId);
        return ResponseEntity.ok(theBasket);
    }
}
