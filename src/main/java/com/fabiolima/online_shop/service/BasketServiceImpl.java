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

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Basket saveBasketAndAddToUser(Long userId, Basket theBasket) {
        //find user
        User theUser = userService.findUserByUserId(userId);

        //add basket to the user (addBasketToUser is a bidirectional helper method)
        theUser.addBasketToUser(theBasket);

        //save the user that will cascade to saving the basket
        userService.saveUser(theUser);

        return theBasket;
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
                .orElseThrow(() -> new NotFoundException(String.format("Basket with ID %d not found",basketId)));
    }

    private Basket validateAndFetchBasket(Long userId, Long basketId){

        return basketRepository.findBasketByIdAndUserId(basketId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Basket with Id %d does not belong to the user with Id %d."
                                ,basketId,userId)));
    }
}
