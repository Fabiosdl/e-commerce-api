package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketItemRepository extends JpaRepository<BasketItem,Long> {
}
