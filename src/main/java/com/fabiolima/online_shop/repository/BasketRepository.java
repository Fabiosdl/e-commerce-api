package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.Basket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketRepository extends JpaRepository<Basket,Long> {
}
