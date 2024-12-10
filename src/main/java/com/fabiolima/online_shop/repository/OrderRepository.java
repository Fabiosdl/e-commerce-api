package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.TheOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<TheOrder,Long> {
}
