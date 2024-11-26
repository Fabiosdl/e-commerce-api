package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
