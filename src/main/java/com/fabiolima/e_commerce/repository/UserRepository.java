package com.fabiolima.e_commerce.repository;

import com.fabiolima.e_commerce.entities.User;
import com.fabiolima.e_commerce.entities.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);
    Page<User> findAllByUserStatus(UserStatus userStatus, Pageable pageable);
    Optional<User> findByEmail(String email);
    Optional<User> findByBaskets_Id(UUID basketId);
}
