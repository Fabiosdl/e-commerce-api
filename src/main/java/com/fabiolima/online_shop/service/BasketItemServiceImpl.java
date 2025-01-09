package com.fabiolima.online_shop.service;

import com.fabiolima.online_shop.exceptions.InsufficientStockException;
import com.fabiolima.online_shop.exceptions.InvalidIdException;
import com.fabiolima.online_shop.exceptions.InvalidQuantityException;
import com.fabiolima.online_shop.exceptions.NotFoundException;
import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.BasketItem;
import com.fabiolima.online_shop.model.Product;
import com.fabiolima.online_shop.repository.BasketItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BasketItemServiceImpl implements BasketItemService{

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
        updateProductStock(theProduct, delta);

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
            //removeItemFromBasket
            return removeItemFromBasket(basketId, basketItemId);
        }

        basketItem.setQuantity(newQuantity);

        /**
         * update stock quantity
         */
        updateProductStock(product, quantityDelta);

        return basketItemRepository.save(basketItem);
    }

    @Override
    /**
     * Method to use in case the quantity is provided by pressing a button that increments the quantity by 1
     */
    public BasketItem incrementItemQuantity(Long basketItemId) {

        //Delta -> New Quantity - current Quantity
        int delta = 1;

        BasketItem basketItem = getItemById(basketItemId);
        Product product = basketItem.getProduct();

        // Check if there's enough stock. If not it will throw an InsufficientStockException
        ensureStockAvailable(product, basketItem.getQuantity());

        //if there's enough stock, increment item and decrement stock;
        basketItem.incrementQuantity(1);
        updateProductStock(product, delta);

        return basketItemRepository.save(basketItem);
    }

    @Override
    @Transactional
    /**
     * Method in case the quantity is provided by pressing a button that decrements the quantity by 1
     */
    public BasketItem decrementItemQuantity(Long basketId, Long basketItemId) {
        //Delta -> New Quantity - current Quantity
        int delta = -1;

        BasketItem basketItem = getItemById(basketItemId);
        Product product = basketItem.getProduct();

        if(basketItem.getQuantity() < 1)
            throw new InvalidQuantityException("Cannot decrement quantity below 0.");


        //in case theres only one quantity of an item, the item will be removed from basket
        if(basketItem.getQuantity() == 1)
            //removeItemFromBasket method update stock automatically
            return removeItemFromBasket(basketId, basketItemId);

        //update stock
        updateProductStock(product, delta);

        basketItem.decrementQuantity(1);

        return basketItemRepository.save(basketItem);
    }

    @Override
    @Transactional
    public BasketItem removeItemFromBasket(Long basketId, Long basketItemId) {
        //retrieve the basket where the item is stored
        Basket basket = basketService.findBasketById(basketId);
        List<BasketItem> listOfItems = basket.getBasketItems();

        //retrieve the item from the basket items list
        BasketItem itemFromList = listOfItems.stream()
                .filter(basketItem ->
                    basketItem.getId().equals(basketItemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        String.format("Basket id %d do not contain Item with id %d",basketId,basketItemId)));

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
        updateProductStock(product, delta);

        //returning removed item so it can be tested in integration test using postman
        return itemFromList;
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

    protected void validateId(Long id){
        if(id == null)
            throw new InvalidIdException("The Id cannot be null");
        if(id <= 0)
            throw new InvalidIdException("The Id must be grater than 0");
    }

    protected void updateProductStock(Product product, int delta){

        /** Example of how the method works
         * initial product stock = 10 units
         * Firstly user puts 4 quantity of an item in basket
         * current product stock = 10 - 4 .: current stock = 6 units
         * now user wants 7 units INSTEAD of 4 units
         * delta = new item quantity - current item quantity
         * delta = 7 - 4 .: delta = 3
         * updated product stock = current stock - delta
         * updated stock = 6 - 3 = 3 units.
         *
         * It can be proved by getting the initial stock = 10, minus items in basket = 7
         * which results in a updated stock of 3 units
         */
        int currentStock = product.getStock();
        int updatedStock = currentStock - delta;

        product.setStock(updatedStock);
        productService.saveProduct(product);
    }
}