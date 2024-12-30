package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.exceptions.ForbiddenException;
import com.fabiolima.online_shop.exceptions.NotFoundException;
import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.model.enums.BasketStatus;
import com.fabiolima.online_shop.repository.BasketRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BasketServiceImpl implements BasketService {

    private final BasketRepository basketRepository;
    private final UserService userService;

    @Autowired
    public BasketServiceImpl (BasketRepository basketRepository,
                              UserService userService){
        this.basketRepository = basketRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Basket saveBasketAndAddToUser(Long userId) {
        //find user
        User theUser = userService.findUserByUserId(userId);

        //add basket to the user (addBasketToUser is a bidirectional helper method)
        theUser.addBasketToUser(new Basket());

        //save the user that will cascade to saving the basket
        userService.saveUser(theUser);

        return theUser.getBaskets().getLast();
    }

    @Override
    public List<Basket> getUserBaskets(Long userId) {
        User theUser = userService.findUserByUserId(userId);
        return theUser.getBaskets();
    }

    @Override
    public Basket getUserBasketById(Long userId, Long basketId) {
        // check if the basket exists
        return validateAndFetchBasket(userId, basketId);
    }

    @Override
    public Basket checkOutBasket(Long userId, Long basketId) {
        // check if the basket belongs to user
        Basket theBasket = validateAndFetchBasket(userId, basketId);

        // check if basket status is OPEN and set it to Checked out
        if(!theBasket.getBasketStatus().equals(BasketStatus.OPEN))
            throw new ForbiddenException("Can only check out an open basket.");
        theBasket.setBasketStatus(BasketStatus.CHECKED_OUT);

        // persist the updated basket
        return basketRepository.save(theBasket);
    }

    @Override
    public Basket deleteBasketById(Long userId, Long basketId) {

        Basket reference = validateAndFetchBasket(userId, basketId);
        if(reference.getBasketStatus() == BasketStatus.CHECKED_OUT)
            throw new ForbiddenException("Cannot delete a checked out basket.");
        basketRepository.deleteById(basketId);
        return reference;
    }

    @Override
    public Basket findBasketById(Long basketId) {
        return basketRepository.findById(basketId)
                .orElseThrow(() -> new NotFoundException(String.format("Basket with Id %d not found",basketId)));
    }

    @Override
    @Transactional
    public void clearBasket(Long basketId) {
        Basket theBasket = findBasketById(basketId);
        theBasket.getBasketItems().clear();
        basketRepository.save(theBasket);
    }

    @Override
    public int getTotalQuantity(Long basketId) {
        Basket theBasket = findBasketById(basketId);
        return theBasket.getBasketItems().size();
    }

    @Override
    public double calculateTotalPrice(Long basketId) {

        Basket theBasket = findBasketById(basketId);
        return theBasket.getBasketItems().stream()
                .mapToDouble(basketItem -> basketItem.getQuantity() * basketItem.getProduct().getProductPrice())
                .sum();
    }

    private Basket validateAndFetchBasket(Long userId, Long basketId){

        return basketRepository.findBasketByIdAndUserId(basketId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Basket with Id %d does not belong to the user with Id %d."
                                ,basketId,userId)));
    }
}
