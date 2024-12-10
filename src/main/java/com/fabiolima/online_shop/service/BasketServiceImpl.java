package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.exceptions.NotFoundException;
import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.repository.BasketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BasketServiceImpl implements BasketService {

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private UserService userService;

    @Override
    public Basket saveBasket(Basket theBasket) {
        return basketRepository.save(theBasket);
    }

    @Override
    public Basket findBasketById(Long basketId) {

        Optional<Basket> result = basketRepository.findById(basketId);
        if(result.isEmpty()) throw new NotFoundException("Basket not found");
        return result.get();
    }

    @Override
    public List<Basket> getUserBaskets(Long userId) {
        User theUser = userService.findUserByUserId(userId);
        return theUser.getBaskets();
    }

    @Override
    public Basket getUserBasketById(Long userId, Long basketId) {
        // check if the basket exists
        Basket theBasket = findBasketById(basketId);

        // check if the user owns the basket
        basketBelongToUser(userId, basketId);

        return theBasket;
    }

    @Override
    public Basket updateBasketStatus(Long userId, Long basketId, Basket theBasket) {
        // check if the basket belongs to user
        basketBelongToUser(userId, basketId);

        Basket existingBasket = findBasketById(basketId);
        existingBasket.setBasketStatus(theBasket.getBasketStatus());
        return basketRepository.save(existingBasket);
    }

    @Override
    public Basket deleteBasketById(Long userId, Long basketId) {

        basketBelongToUser(userId, basketId);
        Basket reference = findBasketById(basketId);
        basketRepository.deleteById(basketId);
        return reference;
    }

    private void basketBelongToUser(Long userId, Long basketId){

        User theUser = userService.findUserByUserId(userId);
        Basket theBasket = findBasketById(basketId);

        if(!theUser.equals(theBasket.getUser())) throw new NotFoundException(
                "Basket does not belong to the user."
        );
    }
}
