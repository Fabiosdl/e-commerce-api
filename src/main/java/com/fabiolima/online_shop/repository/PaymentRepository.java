package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
