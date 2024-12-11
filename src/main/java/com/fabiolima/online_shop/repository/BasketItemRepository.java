package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.BasketItem;
import com.fabiolima.online_shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BasketItemRepository extends JpaRepository<BasketItem,Long> {

    Optional<BasketItem> findByBasketAndProduct(@Param("basket") Basket basket, @Param("product") Product product);
}
