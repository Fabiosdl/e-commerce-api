package com.fabiolima.online_shop.repository;

import com.fabiolima.online_shop.model.Role;
import com.fabiolima.online_shop.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findByName(UserRole name);
}