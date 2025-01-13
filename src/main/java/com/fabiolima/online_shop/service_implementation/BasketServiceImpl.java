package com.fabiolima.online_shop.service_implementation;

import com.fabiolima.online_shop.exceptions.ForbiddenException;
import com.fabiolima.online_shop.exceptions.NotFoundException;
import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.BasketItem;
import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.model.enums.BasketStatus;
import com.fabiolima.online_shop.repository.BasketRepository;
import com.fabiolima.online_shop.service.BasketItemService;
import com.fabiolima.online_shop.service.BasketService;
import com.fabiolima.online_shop.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * clearBasket must be used in case the user wants to keep the basket open, but want to delete all items in it.
 * deleteBasket must be used in case the user wants to delete the basket
 */
@Service
public class BasketServiceImpl implements BasketService {

    private final BasketRepository basketRepository;
    private final UserService userService;
    private final BasketItemService basketItemService;

    @Autowired
    public BasketServiceImpl (BasketRepository basketRepository,
                              UserService userService,
                              @Lazy BasketItemService basketItemService){
        this.basketRepository = basketRepository;
        this.userService = userService;
       this.basketItemService = basketItemService;
    }


    @Override
    @Transactional
    public Basket saveBasketAndAddToUser(Long userId) {
        //find user
        User theUser = userService.findUserByUserId(userId);

        //add basket to the user (addBasketToUser is a bidirectional helper method)
        Basket basket = new Basket();
        theUser.addBasketToUser(basket);

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
        //check if userId and basketId are valid
        if(basketId == null)
            throw new IllegalArgumentException ("Basket id cannot be null");
        if(userId == null)
            throw new IllegalArgumentException ("User id cannot be null");
        if(basketId <= 0L)
            throw new IllegalArgumentException ("Basket id must be greater than 0");
        if(userId <= 0L)
            throw new IllegalArgumentException ("User id must be greater than 0");

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
    public Basket updateBasketWhenItemsAreAddedOrModified(Basket basket) {
        return basketRepository.save(basket);
    }

    @Override
    public Basket deleteBasketById(Long userId, Long basketId) {
        // validate basket
        Basket reference = validateAndFetchBasket(userId, basketId);

        //check if its status is checked_out
        if(reference.getBasketStatus() == BasketStatus.CHECKED_OUT)
            throw new ForbiddenException("Cannot delete a checked out basket.");

        //if not, clear the basket, giving back to stock all the quantity in items
        //clearBasket(basketId);

        //delete basket if it is empty
        if (!reference.getBasketItems().isEmpty())
            throw new ForbiddenException("Basket must be empty before deleting it.");
        basketRepository.deleteById(basketId);
        return reference;
    }

    @Override
    public Basket findBasketById(Long basketId) {
        return basketRepository.findById(basketId)
                .orElseThrow(() -> new NotFoundException(String.format("Basket with Id %d not found",basketId)));
    }

    @Override//I have to pass all the quantity items back to product
    @Transactional
    public Basket clearBasket(Long basketId) {
        //find the basket
        Basket theBasket = findBasketById(basketId);

        //get the list of items and pass item by item to removeItemFromBasket method

        List<BasketItem> listOfItem = theBasket.getBasketItems();
        for(BasketItem item : listOfItem){
            Long itemId = item.getId();
            //this method will remove item from basket, update stock in database, update basket in database and
            //delete items from database
            //basketItemService.removeItemFromBasket(basketId,itemId);
        }
        return theBasket;
    }

    @Override
    @Scheduled(fixedRate = 60000) // run every 60 seconds
    public void deleteExpiredBasket() {

        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);

        List<Basket> expiredBaskets = basketRepository.findAllByLastUpdatedBefore(expirationTime);

        if (!expiredBaskets.isEmpty()) {
            basketRepository.deleteAll(expiredBaskets);
            System.out.println("Deleted expired baskets: " + expiredBaskets.size());
        }
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

    public Basket validateAndFetchBasket(Long userId, Long basketId){

        return basketRepository.findBasketByIdAndUserId(basketId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Basket with Id %d does not belong to the user with Id %d."
                                ,basketId,userId)));
    }
}
