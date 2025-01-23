package com.fabiolima.e_commerce.repository;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.BasketItem;
import com.fabiolima.e_commerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository//optional (only for jpa is optional)
public interface BasketItemRepository extends JpaRepository<BasketItem,Long> {

    Optional<BasketItem> findByBasketAndProduct(@Param("basket") Basket basket, @Param("product") Product product);
}
