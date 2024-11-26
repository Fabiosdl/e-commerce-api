package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasketItemRepository extends JpaRepository<BasketItem,Long> {
}
