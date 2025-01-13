package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.Basket;
import com.fabiolima.online_shop.model.enums.BasketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BasketRepository extends JpaRepository<Basket,Long> {

    Optional<Basket> findBasketByIdAndUserId(@Param("basketId") Long basketId, @Param("userId") Long userId);

    List<Basket> findByBasketStatusAndLastUpdatedBefore(BasketStatus basketStatus, LocalDateTime time);
}
