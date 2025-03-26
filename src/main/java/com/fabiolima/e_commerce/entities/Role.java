package com.fabiolima.e_commerce.entities;

import com.fabiolima.e_commerce.entities.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id",columnDefinition = "BINARY(16)")
    private UUID roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private UserRole name;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    @Override
    public String toString() {
        return "Role{id=" + roleId + ", name=" + name + "}";
    }
}