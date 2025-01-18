package com.fabiolima.e_commerce.service_implementation;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.*;
import com.fabiolima.e_commerce.model.enums.BasketStatus;
import com.fabiolima.e_commerce.repository.BasketRepository;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.ProductService;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * clearBasket must be used in case the user wants to keep the basket open, but want to delete all items in it.
 * deleteBasket must be used in case the user wants to delete the basket
 */

@Service
public class BasketServiceImpl implements BasketService {

    private final BasketRepository basketRepository;
    private final ProductService productService;

    @Autowired
    public BasketServiceImpl (BasketRepository basketRepository,
                              ProductService productService){
        this.basketRepository = basketRepository;
        this.productService = productService;
    }

    @Override
    @Transactional
    public Basket createBasketAndAddToUser(User theUser) {

        // Initialize the baskets collection
        Hibernate.initialize(theUser.getBaskets());

        //check if user already has an open basket
        Optional<Basket> existingBasket = basketRepository.findActiveBasketByUserId(theUser.getId(),BasketStatus.ACTIVE);
        if (existingBasket.isPresent())
            return existingBasket.get();

        //add basket to the user (addBasketToUser is a bidirectional helper method)
        Basket basket = new Basket();
        theUser.addBasketToUser(basket);

        return basketRepository.save(basket);
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
    public Basket updateBasketWhenItemsAreAddedOrModified(Basket basket) {
        return basketRepository.save(basket);
    }

    @Override
    public Basket deactivateBasketById(Long userId, Long basketId) {
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
        //inactivate basket
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
    @Transactional
    public void clearExpiredBasketAndAddNewOne() {

        //setting the no activity in basket for 1 day
        LocalDateTime expirationTime = LocalDateTime.now().minusDays(1);

        //get all open baskets from Users
        List<Basket> expiredBaskets = basketRepository.findByBasketStatusAndLastUpdatedBefore(BasketStatus.ACTIVE,expirationTime);

        if (!expiredBaskets.isEmpty()) {
            //iterate through the list of baskets and clear it by returning its quantity to stock, before deleting
            for(Basket b : expiredBaskets) {
                //remove all items from the basket
                clearBasket(b.getId());

                //deactivate basket
                b.setBasketStatus(BasketStatus.INACTIVE);

                //Add new basket to user
                createBasketAndAddToUser(b.getUser());
            }
            System.out.println("Deactivated expired baskets: " + expiredBaskets.size());
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
    @Override
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
        //delta = new quantity - current quantity -> delta = 0 - current quantity
        int delta = - itemFromList.getQuantity();
        productService.updateProductStock(product, delta);

        return itemFromList;

    }
}