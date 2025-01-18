package com.fabiolima.e_commerce.repository;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.enums.BasketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BasketRepository extends JpaRepository<Basket,Long> {

    Optional<Basket> findBasketByIdAndUserId(@Param("basketId") Long basketId, @Param("userId") Long userId);

    Page<Basket> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    List<Basket> findByBasketStatusAndLastUpdatedBefore(BasketStatus basketStatus, LocalDateTime time);

    @Query("SELECT b FROM Basket b WHERE b.user.id = :userId AND b.basketStatus = :status")
    Optional<Basket> findActiveBasketByUserId(@Param("userId") Long userId, @Param("status") BasketStatus status);
}
