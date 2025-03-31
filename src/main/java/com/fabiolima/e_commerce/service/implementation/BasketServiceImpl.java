package com.fabiolima.e_commerce.service.implementation;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.InvalidQuantityException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.entities.*;
import com.fabiolima.e_commerce.entities.enums.BasketStatus;
import com.fabiolima.e_commerce.entities.enums.UserStatus;
import com.fabiolima.e_commerce.repository.BasketRepository;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.aop.framework.AopContext;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.*;

/**
 * clearBasket must be used in case the user wants to keep the basket open/active, but want to delete all items in it.
 * deactivateBasket must be used in case the user wants to deactivate the basket
 * inactive basket will stay in db for merchandise purposes
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

        //check if user already has an open basket
        Optional<Basket> existingBasket = basketRepository.findActiveBasketByUserId(theUser.getId(),BasketStatus.ACTIVE);
        if (existingBasket.isPresent())
            return existingBasket.get();

        //add basket to the user (addBasketToUser is a bidirectional helper method)
        Basket basket = new Basket();
        basket.setBasketStatus(BasketStatus.ACTIVE);
        theUser.addBasketToUser(basket);

        return basketRepository.save(basket);
    }

    @Override
    public Page<Basket> getUserBaskets(int pgNum, int pgSize, UUID userId) {

        Pageable pageable  = PageRequest.of(pgNum, pgSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return basketRepository.findAllByUserId(userId, pageable);

    }

    @Override
    public Basket updateBasketWhenItemsAreAddedOrModified(Basket basket) {
        return basketRepository.save(basket);
    }

    @Transactional
    @Override
    public Basket deactivateBasketById(UUID userId, UUID basketId) {
        //00 self-invocation via proxy to ensure @Transactional works
        BasketService selfProxy = (BasketService) AopContext.currentProxy();

        //1 - Validate basket
        Basket reference = findBasketById(basketId);

        //2 - Check if its status is checked_out
        if(!reference.getBasketStatus().equals(BasketStatus.ACTIVE))
            throw new ForbiddenException("Only an ACTIVE basket can be deactivated.");

        //3 - If not, clear the basket, giving back to stock all the quantity in items
        if(!reference.getBasketItems().isEmpty())
            selfProxy.clearBasket(basketId);

        //4 - Inactivate basket
        reference.setBasketStatus(BasketStatus.INACTIVE);

        return basketRepository.save(reference);
    }

    @Override
    public Basket findBasketById(UUID basketId) {
        return basketRepository.findById(basketId)
                .orElseThrow(() -> new NotFoundException(String.format("Basket with Id %s not found",basketId.toString())));
    }

    @Override//I have to pass all the quantity items back to product
    @Transactional
    public Basket clearBasket(UUID basketId) {
        //find the basket
        Basket theBasket = findBasketById(basketId);

        //Check if the basket is not checked-out
        if(!theBasket.getBasketStatus().equals(BasketStatus.ACTIVE))
            throw new ForbiddenException("Can only clear an ACTIVE basket");

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
    public void deleteExpiredBasketAndAddNewOne() {
        // self-invocation via proxy to ensure @Transactional works
        BasketService selfProxy = (BasketService) AopContext.currentProxy();

        //setting the no activity in basket for 1 day
        LocalDateTime expirationTime = LocalDateTime.now().minusDays(1);

        //get all open baskets from Users
        List<Basket> expiredBaskets = basketRepository.findByBasketStatusAndLastUpdatedBefore(BasketStatus.ACTIVE,expirationTime);

        if (!expiredBaskets.isEmpty()) {
            //iterate through the list of baskets and clear it by returning its quantity to stock, before deleting
            for(Basket b : expiredBaskets) {

                // clear the basket, giving back to stock all the quantity in items
                selfProxy.clearBasket(b.getId());

                //delete basket in databases
                basketRepository.delete(b);
                basketRepository.flush();// ensure delete is persisted

                //Add new basket to user if user is active.
                User user = b.getUser();
                if(user.getUserStatus().equals(UserStatus.ACTIVE))
                    selfProxy.createBasketAndAddToUser(b.getUser());
            }
            log.info("{} Expired Baskets have been deleted", expiredBaskets.size());
        }
    }

    @Override
    @Transactional
    public Basket checkoutBasket(UUID userId, UUID basketId) {
        // self-invocation via proxy to ensure @Transactional works
        BasketService selfProxy = (BasketService) AopContext.currentProxy();

        //1-Retrieve the basket
        Basket basket = findBasketById(basketId);

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
        Basket newBasket = selfProxy.createBasketAndAddToUser(user);

        log.info("basket {} is checked-out and a new basket of id {} has been created to user {} - {}",
                basketId, newBasket.getId(), user.getId(), user.getName());
        return basketRepository.save(basket);
    }

    @Override
    public int getTotalQuantity(UUID basketId) {
        Basket theBasket = findBasketById(basketId);
        int count = 0;
        for (BasketItem item : theBasket.getBasketItems()){
            count += item.getQuantity();
        }
        return count;
    }

    @Override
    public BigDecimal calculateTotalPrice(UUID basketId) {

        Basket theBasket = findBasketById(basketId);
        // Stream through basket items and calculate the total
        // Map each basket item to its price (quantity * product price)
        // Reduce the BigDecimal stream to a single sum
        return theBasket.getBasketItems().stream()
                // Map each basket item to its price (quantity * product price)
                .map(basketItem -> BigDecimal.valueOf(basketItem.getQuantity())
                        .multiply(basketItem.getProduct().getProductPrice()).setScale(2, RoundingMode.HALF_UP))
                // Reduce the BigDecimal stream to a single sum
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Basket returnNewestActiveBasket(User user) {

        Optional<Basket> activeBasket = user.getBaskets().stream()
                .filter(basket -> BasketStatus.ACTIVE.equals(basket.getBasketStatus()))
                .max(Comparator.comparing(Basket::getCreatedAt));

        if (activeBasket.isEmpty())
            throw new NotFoundException("No active basket has been found");

        return activeBasket.get();
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