package com.fabiolima.e_commerce.repository;

import com.fabiolima.e_commerce.model.Basket;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    Page<User> findAllByUserStatus(UserStatus userStatus, Pageable pageable);
    Optional<User> findByEmail(String email);
    Optional<User> findByBaskets_Id(Long basketId);
}
