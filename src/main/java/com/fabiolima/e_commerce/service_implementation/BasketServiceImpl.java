package com.fabiolima.e_commerce.service_implementation;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.InvalidQuantityException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.*;
import com.fabiolima.e_commerce.model.enums.BasketStatus;
import com.fabiolima.e_commerce.model.enums.UserStatus;
import com.fabiolima.e_commerce.repository.BasketRepository;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * clearBasket must be used in case the user wants to keep the basket open, but want to delete all items in it.
 * deleteBasket must be used in case the user wants to delete the basket
 */

@Slf4j
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
        Hibernate. initialize(theUser.getBaskets());

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
        //1 - Validate basket
        Basket reference = validateAndFetchBasket(userId, basketId);

        //2 - Check if its status is checked_out
        if(reference.getBasketStatus() == BasketStatus.CHECKED_OUT)
            throw new ForbiddenException("Cannot delete a checked out basket.");

        //3 - If not, clear the basket, giving back to stock all the quantity in items
        clearBasket(basketId);

        //4 - Clear basket if it is empty
        if (!reference.getBasketItems().isEmpty())
            throw new ForbiddenException("Basket must be empty before deleting it.");

        //5 - Inactivate basket
        reference.setBasketStatus(BasketStatus.INACTIVE);

        return basketRepository.save(reference);
    }

    @Override
    public void deleteBasketById(Long userId, Long basketId) {
        //1 - Validate basket
        Basket reference = validateAndFetchBasket(userId, basketId);

        //2 - Check if its status is checked_out
        if(reference.getBasketStatus() == BasketStatus.CHECKED_OUT)
            throw new ForbiddenException("Cannot delete a checked out basket.");

        //3 - If not, clear the basket, giving back to stock all the quantity in items
        clearBasket(basketId);

        //4 - check if basket is empty before deleting it
        if (!reference.getBasketItems().isEmpty())
            throw new ForbiddenException("Basket must be empty before deleting it.");

        //5 - Delete basket
        basketRepository.delete(reference);
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

        //Check if the basket is not checked-out
        if(theBasket.getBasketStatus() == BasketStatus.CHECKED_OUT)
            throw new ForbiddenException("Cannot clear a Checked-out basket");

        //get the list of items and pass item by item to removeItemFromBasket method
        Iterator<BasketItem> listOfItem = theBasket.getBasketItems().iterator();
        while(listOfItem.hasNext()){

            BasketItem item = listOfItem.next();
            /**
             *As per orphanRemoval is enabled in the One-To- Many relationship between basket and item
             *Hibernate will automatically persist the modification into the database,
             * both for Basket and BasketItem entity
             */

            listOfItem.remove();

            /**
             * update stock
             */
            Product product = item.getProduct();
            //delta = new quantity - current quantity -> delta = 0 - current quantity
            int delta = - item.getQuantity();
            productService.updateProductStock(product, delta);
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

                //delete basket
                deleteBasketById(b.getUser().getId(), b.getId());

                //Add new basket to user if user is active.
                User user = b.getUser();
                if(user.getUserStatus().equals(UserStatus.ACTIVE))
                    createBasketAndAddToUser(b.getUser());
            }
            System.out.println("Deactivated expired baskets: " + expiredBaskets.size());
        }
    }

    @Override
    @Transactional
    public Basket checkoutBasket(Long userId, Long basketId) {

        //1-Retrieve the basket
        Basket basket = getUserBasketById(userId, basketId);

        //2-Check if the basket is empty
        if(basket.getBasketItems().isEmpty())
            throw new InvalidQuantityException("The current basket is empty.");

        //3-Check if basket status is ACTIVE and set it to Checked out
        if(!basket.getBasketStatus().equals(BasketStatus.ACTIVE))
            throw new ForbiddenException("Can only check out an ACTIVE basket.");

        //4-Change the status
        basket.setBasketStatus(BasketStatus.CHECKED_OUT);

        //5- Create a new basket to the user
        User user = basket.getUser();
        Basket newBasket = createBasketAndAddToUser(user);

        log.info("basket {} is checked-out and a new basket, id {} has been created to user {} - {}", basketId, user.getId(), newBasket.getId(), user.getName());
        return basketRepository.save(basket);
    }

    @Override
    public int getTotalQuantity(Long basketId) {
        Basket theBasket = findBasketById(basketId);
        return theBasket.getBasketItems().size();
    }

    @Override
    public BigDecimal calculateTotalPrice(Long basketId) {

        Basket theBasket = findBasketById(basketId);
        // Stream through basket items and calculate the total
        BigDecimal totalPrice = theBasket.getBasketItems().stream()
                // Map each basket item to its price (quantity * product price)
                .map(basketItem -> BigDecimal.valueOf(basketItem.getQuantity())
                        .multiply(basketItem.getProduct().getProductPrice()).setScale(2, RoundingMode.HALF_UP))
                // Reduce the BigDecimal stream to a single sum
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                return totalPrice;
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
    public BasketItem removeItemFromBasket(Basket basket, BasketItem item) {

        //retrieve the list of items in basket
        List<BasketItem> listOfItems = basket.getBasketItems();

        //Confirm that the item belongs to basket
        if(!listOfItems.contains(item))
            throw new NotFoundException(
                        String.format("Basket id %d do not contain Item with id %d",basket.getId(),item.getId()));

        /**
         *As per orphanRemoval is enabled in the One-To- Many relationship between basket and item
         *Hibernate will automatically persist the modification into the database,
         * both for Basket and BasketItem entity
         */

        basket.getBasketItems().remove(item);

        /**
         * update stock
         */
        Product product = item.getProduct();
        //delta = new quantity - current quantity -> delta = 0 - current quantity
        int delta = - item.getQuantity();
        productService.updateProductStock(product, delta);

        return item;
    }
}