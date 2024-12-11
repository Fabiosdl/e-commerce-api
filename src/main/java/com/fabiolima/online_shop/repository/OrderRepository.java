package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.TheOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<TheOrder,Long> {

    //@Query("SELECT o FROM TheOrder o WHERE o.id = :orderId AND o.user.id = :userId")
    Optional<TheOrder> findOrderByIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);
    // Don't need the query because JPA recognizes the method name and know what to do

}
