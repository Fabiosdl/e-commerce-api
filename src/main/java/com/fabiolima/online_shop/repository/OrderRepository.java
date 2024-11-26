package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
