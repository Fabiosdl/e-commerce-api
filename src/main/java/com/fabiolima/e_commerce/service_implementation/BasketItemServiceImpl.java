package com.fabiolima.e_commerce.service_implementation;

import com.fabiolima.e_commerce.exceptions.InsufficientStockException;
import com.fabiolima.e_commerce.exceptions.InvalidIdException;
import com.fabiolima.e_commerce.exceptions.InvalidQuantityException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.BasketItem;
import com.fabiolima.e_commerce.model.Product;
import com.fabiolima.e_commerce.repository.BasketItemRepository;
import com.fabiolima.e_commerce.service.BasketItemService;
import com.fabiolima.e_commerce.service.BasketService;
import com.fabiolima.e_commerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BasketItemServiceImpl implements BasketItemService {

    private final BasketItemRepository basketItemRepository;
    private final BasketService basketService;
    private final ProductService productService;

    @Autowired
    public BasketItemServiceImpl (BasketItemRepository basketItemRepository,
                                  BasketService basketService,
                                  ProductService productService){
        this.basketItemRepository = basketItemRepository;
        this.basketService = basketService;
        this.productService = productService;
    }

    @Override
    @Transactional
    public BasketItem addItemToBasket(Long basketId, Long productId, int quantity) {
        //validate the parameters Ids
        validateId(basketId);
        validateId(productId);

        //validate quantity
        if(quantity <= 0)
            throw new InvalidQuantityException("Quantity cannot be zero or have negative values");

        // fetch the basket
        Basket theBasket = basketService.findBasketById(basketId);

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
            return basketItemRepository.save(existingItem);
        }

        // if item doesn't exist create a new basket item
        BasketItem newItem = BasketItem.builder()
                .product(theProduct)
                .quantity(quantity)
                .build();

        // add item to basket
        theBasket.addBasketItemToBasket(newItem);

        // save the Basket containing the new item
        basketService.updateBasketWhenItemsAreAddedOrModified(theBasket);
        return newItem;
    }

    @Override
    public List<BasketItem> getItemsByBasket(Long basketId) {
        //validate basketId
        validateId(basketId);
        return basketService.findBasketById(basketId).getBasketItems();
    }

    @Override
    public BasketItem getItemById(Long basketItemId) {

        validateId(basketItemId);

        return basketItemRepository.findById(basketItemId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Item with Id %d was not found.",basketItemId)
                ));
    }

    @Override
    @Transactional
    /**
     * Method to be used in case the user changes the quantity of a product manually providing the quantity they want
      */
    public BasketItem updateBasketItem(Long basketId, Long basketItemId, int newQuantity) {
        validateId(basketId);
        validateId(basketItemId);

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

            Basket basket = basketService.findBasketById(basketId);
            //removeItemFromBasket method update stock automatically
            basketService.removeItemFromBasket(basket, basketItem);
        }

        basketItem.setQuantity(newQuantity);

        /**
         * update stock quantity after ensuring stock availability
         */
        productService.updateProductStock(product, quantityDelta);

        return basketItemRepository.save(basketItem);
    }

    @Override
    /**
     * Method to use in case the quantity is provided by pressing a button that increments the quantity by 1
     */
    public BasketItem incrementItemQuantity(Long basketItemId) {
        validateId(basketItemId);

        //Delta -> New Quantity - current Quantity
        int delta = 1;

        BasketItem basketItem = getItemById(basketItemId);
        Product product = basketItem.getProduct();

        // Check if there's enough stock. If not it will throw an InsufficientStockException
        ensureStockAvailable(product, basketItem.getQuantity());

        //if there's enough stock, increment item and decrement stock;
        basketItem.incrementQuantity(1);
        productService.updateProductStock(product, delta);

        return basketItemRepository.save(basketItem);
    }

    @Override
    @Transactional
    /**
     * Method in case the quantity is provided by pressing a button that decrements the quantity by 1
     */
    public BasketItem decrementItemQuantity(Long basketId, Long basketItemId) {
        validateId(basketId);
        validateId(basketItemId);

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
            basketService.removeItemFromBasket(basket, basketItem);
        }

        //update stock
        productService.updateProductStock(product, delta);

        basketItem.decrementQuantity(1);

        return basketItemRepository.save(basketItem);
    }

    @Override
    @Transactional
    public BasketItem removeItemFromBasket(Long basketId, Long basketItemId) {

        //retrieve the basket where the item is stored
        Basket basket = basketService.findBasketById(basketId);
        BasketItem item = getItemById(basketItemId);
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
    public double calculateItemTotalPrice(Long basketItemId) {
        validateId(basketItemId);
        BasketItem basketItem = getItemById(basketItemId);
        return (basketItem.getQuantity() * basketItem.getProduct().getProductPrice());
    }

    public void validateId(Long id){
        if(id == null)
            throw new InvalidIdException("The Id cannot be null");
        if(id <= 0)
            throw new InvalidIdException("The Id must be grater than 0");
    }

}