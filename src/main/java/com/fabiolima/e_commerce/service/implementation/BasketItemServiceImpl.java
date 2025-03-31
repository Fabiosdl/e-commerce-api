package com.fabiolima.e_commerce.service.implementation;

import com.fabiolima.e_commerce.exceptions.*;
import com.fabiolima.e_commerce.entities.Basket;
import com.fabiolima.e_commerce.entities.BasketItem;
import com.fabiolima.e_commerce.entities.Product;
import com.fabiolima.e_commerce.entities.enums.BasketStatus;
import com.fabiolima.e_commerce.repository.BasketItemRepository;
import com.fabiolima.e_commerce.repository.BasketRepository;
import com.fabiolima.e_commerce.service.BasketItemService;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class BasketItemServiceImpl implements BasketItemService {

    private final BasketItemRepository basketItemRepository;
    private final BasketRepository basketRepository;
    private final BasketService basketService;
    private final ProductService productService;

    @Autowired
    public BasketItemServiceImpl (BasketItemRepository basketItemRepository, BasketRepository basketRepository,
                                  BasketService basketService,
                                  ProductService productService){
        this.basketItemRepository = basketItemRepository;
        this.basketRepository = basketRepository;
        this.basketService = basketService;
        this.productService = productService;
    }

    @Override
    @Transactional
    public BasketItem addItemToBasket(UUID basketId, UUID productId, int quantity) {

        //validate quantity
        if(quantity <= 0)
            throw new InvalidQuantityException("Quantity cannot be zero or have negative values");

        // fetch the basket
        Basket theBasket = basketService.findBasketById(basketId);

        // check if the basket is active
        if(!theBasket.getBasketStatus().equals(BasketStatus.ACTIVE))
            throw new ForbiddenException("Can only insert items in a ACTIVE basket");

        // fetch product
        Product theProduct = productService.findProductById(productId);

        // Check if there's enough stock. If not it will throw an InsufficientStockException
        //This method always increment quantity, so the product stock is already updated
        ensureStockAvailable(theProduct, quantity);

        // Check if item already exists in the basket, if so update item quantity
        BasketItem existingItem = theBasket.getBasketItems().stream()
                .filter(item -> item.getProduct().getId().equals(theProduct.getId()))
                .findFirst()
                .orElse(null);

        /**
         * After checking that there's enough stock available
         * Update product stock
         */
        int delta = quantity;
        productService.updateProductStock(theProduct, delta);

        if (existingItem != null) {
            existingItem.incrementQuantity(quantity);
            log.info("Item id {} already exists in basket and its quantity is being updated", existingItem.getId());
            return basketItemRepository.save(existingItem);
        }

        // if item doesn't exist create a new basket item
        BasketItem newItem = BasketItem.builder()
                .product(theProduct)
                .quantity(quantity)
                .build();

        // add item to basket
        theBasket.addBasketItemToBasket(newItem);

        // update the time and date of insertion in basket
        theBasket.setLastUpdated(LocalDateTime.now());

        // save the Basket containing the new item
        basketService.updateBasketWhenItemsAreAddedOrModified(theBasket);

        log.info("Item has been created and added to basket.");
        return newItem;
    }

    @Override
    public List<BasketItem> getItemsByBasket(UUID basketId) {

        return basketService.findBasketById(basketId).getBasketItems();
    }

    @Override
    public BasketItem getItemById(UUID basketItemId) {

        return basketItemRepository.findById(basketItemId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Item with Id %s was not found.",basketItemId.toString())
                ));
    }

    @Override
    @Transactional
    /**
     * Method to be used in case the user changes the quantity of a product manually providing the quantity they want
      */
    public BasketItem updateBasketItem(UUID basketId, UUID basketItemId, int newQuantity) {
        //create basket to update it when an item is updated
        Basket basket = basketService.findBasketById(basketId);

        //retrieve the item
        BasketItem basketItem = getItemById(basketItemId);
        int currentQuantity = basketItem.getQuantity();
        int quantityDelta = newQuantity - currentQuantity;

        Product product = basketItem.getProduct();

        /**
         * Check if there's enough stock. If not it will throw an InsufficientStockException
         * Method also checks if the quantity is valid
         * as it is an update, it means that stock has already been taken, so it should check
         * only the delta between new quantity and current quantity
          */
        if(quantityDelta > 0) // if quantity delta is negative, product stock will be incremented and do not need to be checked
            ensureStockAvailable(product, quantityDelta);

        /**
         * check if quantity is 0. If so, remove item.
         * *** removeItemFromBasket update stock automatically
         */
        if(newQuantity == 0) {
            //item still holds the older quantity, so stock can be updated inside remove
            //removeItemFromBasket method update stock automatically
            log.info("Quantity of item id {} is zero and it will be removed from basket {}", basketItemId, basketId);
            basket.setLastUpdated(LocalDateTime.now());
            return basketService.removeItemFromBasket(basket, basketItem);
        }

        basketItem.setQuantity(newQuantity);

        /**
         * update stock quantity after ensuring stock availability
         */
        productService.updateProductStock(product, quantityDelta);

        // update the time and date of insertion in basket
        basket.setLastUpdated(LocalDateTime.now());
        basketRepository.save(basket);

        log.info("Item {} quantity has been updated", basketItemId);

        return basketItemRepository.save(basketItem);
    }

    @Override
    /**
     * Method to use in case the quantity is provided by pressing a button that increments the quantity by 1
     */
    public BasketItem incrementItemQuantity(UUID basketItemId) {

        //Delta -> New Quantity - current Quantity
        int delta = 1;

        BasketItem basketItem = getItemById(basketItemId);

        Product product = basketItem.getProduct();

        // Check if there's enough stock. If not it will throw an InsufficientStockException
        ensureStockAvailable(product, basketItem.getQuantity());

        //if there's enough stock, increment item and decrement stock
        basketItem.incrementQuantity(1);
        productService.updateProductStock(product, delta);

        // update the time and date of insertion in basket
        Basket theBasket = basketItem.getBasket();
        theBasket.setLastUpdated(LocalDateTime.now());
        basketRepository.save(theBasket);

        log.info("Item id {} quantity has been incremented by one", basketItemId);
        return basketItemRepository.save(basketItem);
    }

    @Override
    @Transactional
    /**
     * Method in case the quantity is provided by pressing a button that decrements the quantity by 1
     */
    public BasketItem decrementItemQuantity(UUID basketId, UUID basketItemId) {

        //Delta -> New Quantity - current Quantity
        int delta = -1;

        BasketItem basketItem = getItemById(basketItemId);
        Product product = basketItem.getProduct();

        if(basketItem.getQuantity() < 1)
            throw new InvalidQuantityException("Cannot decrement quantity below 0.");


        //in case there's only one quantity of an item, the item will be removed from basket
        if(basketItem.getQuantity() == 1){
            Basket basket = basketService.findBasketById(basketId);
            //removeItemFromBasket method update stock automatically
            log.info("Item id {} has been removed from basket as its quantity is now 0", basketItemId);
            return basketService.removeItemFromBasket(basket, basketItem);
        }

        //update stock
        productService.updateProductStock(product, delta);

        basketItem.decrementQuantity(1);

        // update the time and date of insertion in basket
        Basket theBasket = basketItem.getBasket();
        theBasket.setLastUpdated(LocalDateTime.now());
        basketRepository.save(theBasket);

        log.info("Item id {} quantity has been decremented by one", basketItemId);
        return basketItemRepository.save(basketItem);
    }

    @Override
    @Transactional
    public BasketItem removeItemFromBasket(UUID basketId, UUID basketItemId) {

        //retrieve the basket where the item is stored
        Basket basket = basketService.findBasketById(basketId);
        BasketItem item = getItemById(basketItemId);

        // update the time and date of insertion in basket
        basket.setLastUpdated(LocalDateTime.now());
        basketRepository.save(basket);

        return basketService.removeItemFromBasket(basket,item);

    }

    @Override
    public void ensureStockAvailable(Product product, int quantity) {
        if(product == null)
            throw new IllegalArgumentException("Product cannot be null");

        if(quantity < 0)
            throw new InvalidQuantityException("Quantity cannot have negative values");

        if(product.getStock() < quantity)
            throw new InsufficientStockException(
                    String.format("Not enough stock available for product '%s'. Available: %d, Requested: %d",
                            product.getProductName(), product.getStock(), quantity));
    }

    @Override
    public BigDecimal calculateItemTotalPrice(UUID basketItemId) {

        BasketItem basketItem = getItemById(basketItemId);
        BigDecimal itemQuantity = BigDecimal.valueOf(basketItem.getQuantity());
        BigDecimal totalPrice = itemQuantity.multiply(basketItem.getProduct().getProductPrice());

        return totalPrice.setScale(2, RoundingMode.HALF_UP);
    }
}