package com.fabiolima.online_shop.service_implementation;

import com.fabiolima.online_shop.exceptions.ForbiddenException;
import com.fabiolima.online_shop.exceptions.NotFoundException;
import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.BasketItem;
import com.fabiolima.online_shop.model.Product;
import com.fabiolima.online_shop.model.User;
import com.fabiolima.online_shop.model.enums.BasketStatus;
import com.fabiolima.online_shop.repository.BasketRepository;
import com.fabiolima.online_shop.service.BasketService;
import com.fabiolima.online_shop.service.ProductService;
import com.fabiolima.online_shop.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final ProductService productService;

    @Autowired
    public BasketServiceImpl (BasketRepository basketRepository,
                              UserService userService, ProductService productService){
        this.basketRepository = basketRepository;
        this.userService = userService;
        this.productService = productService;
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

        return basket;
    }

    @Override
    public Page<Basket> getUserBaskets(int pgNum, int pgSize, Long userId) {

        Pageable pageable  = PageRequest.of(pgNum, pgSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return basketRepository.findAllByUserId(userId, pageable);

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
        if(!theBasket.getBasketStatus().equals(BasketStatus.ACTIVE))
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
        clearBasket(basketId);

        //delete basket if it is empty
        if (!reference.getBasketItems().isEmpty())
            throw new ForbiddenException("Basket must be empty before deleting it.");
        //deactivate basket
        reference.setBasketStatus(BasketStatus.INACTIVE);
        return basketRepository.save(reference);
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

        //Check if the basket is not to checked-out
        if(theBasket.getBasketStatus() == BasketStatus.CHECKED_OUT)
            throw new ForbiddenException("Cannot clear a Checked-out basket");

        //get the list of items and pass item by item to removeItemFromBasket method

        List<BasketItem> listOfItem = theBasket.getBasketItems();
        for(BasketItem item : listOfItem){
            Long itemId = item.getId();
            //this method will remove item from basket, update stock in database, update basket in database and
            //delete items from database
            removeItemFromBasket(theBasket,itemId);
        }
        return theBasket;
    }

    @Override
    @Scheduled(fixedRate = 60000) // run every 60 seconds
    public void deleteExpiredBasket() {

        //setting the no activity in basket for 15 minutes
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);

        //get all the open baskets from User
        List<Basket> expiredBaskets = basketRepository.findByBasketStatusAndLastUpdatedBefore(BasketStatus.ACTIVE,expirationTime);

        if (!expiredBaskets.isEmpty()) {
            //iterate through the list of baskets and clear it by returning its quantity to stock, before deleting
            for(Basket b : expiredBaskets) {
                clearBasket(b.getId());
                b.setBasketStatus(BasketStatus.INACTIVE);
            }
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
    @Override
    @Transactional
    public BasketItem removeItemFromBasket(Basket basket, Long basketItemId) {

        //retrieve the list of items in basket
        List<BasketItem> listOfItems = basket.getBasketItems();

        //retrieve the item from the basket items list
        BasketItem itemFromList = listOfItems.stream()
                .filter(basketItem ->
                        basketItem.getId().equals(basketItemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        String.format("Basket id %d do not contain Item with id %d",basket.getId(),basketItemId)));

        /**
         *As per orphanRemoval is enabled in the One-To- Many relationship between basket and item
         *Hibernate will automatically persist the modification into the database,
         * both for Basket and BasketItem entity
         */

        basket.getBasketItems().remove(itemFromList);

        /**
         * update stock
         */
        Product product = itemFromList.getProduct();
        int delta = - itemFromList.getQuantity();
        productService.updateProductStock(product, delta);

        return itemFromList;

    }
}