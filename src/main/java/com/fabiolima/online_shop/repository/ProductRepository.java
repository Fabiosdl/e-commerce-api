package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
