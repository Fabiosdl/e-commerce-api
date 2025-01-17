package com.fabiolima.e_commerce.repository;

import com.fabiolima.e_commerce.model.Role;
import com.fabiolima.e_commerce.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findByName(UserRole name);
}