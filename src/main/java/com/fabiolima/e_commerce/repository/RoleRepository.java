package com.fabiolima.e_commerce.repository;

import com.fabiolima.e_commerce.entities.Role;
import com.fabiolima.e_commerce.entities.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(UserRole name);
}