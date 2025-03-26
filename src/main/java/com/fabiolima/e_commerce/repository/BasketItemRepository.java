package com.fabiolima.e_commerce.repository;

import com.fabiolima.e_commerce.entities.Basket;
import com.fabiolima.e_commerce.entities.BasketItem;
import com.fabiolima.e_commerce.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository//optional (only for jpa is optional)
public interface BasketItemRepository extends JpaRepository<BasketItem, UUID> {

    Optional<BasketItem> findByBasketAndProduct(@Param("basket") Basket basket, @Param("product") Product product);
}
