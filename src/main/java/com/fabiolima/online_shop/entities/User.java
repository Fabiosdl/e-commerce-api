package com.fabiolima.online_shop.entities;

import com.fabiolima.online_shop.entities.enums.UserRole;
import com.fabiolima.online_shop.entities.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "address")
    private String address;

    @Column(name = "role")
    private UserRole userRole;

    @Column(name = "status")
    private UserStatus userStatus;

    @Column(name = "createdAt")
    private OffsetDateTime createdAt;

    @Column(name = "updatedAt")
    private OffsetDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "user",  //field "user" in Order Class
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private final List<Order> orders = new ArrayList<>();
    //it's final to reassure that the orders list belongs to the user

    @JsonIgnore
    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL)
    private final List<Basket> baskets = new ArrayList<>();

    /**
    * This method is invoked before the entity is persisted (i.e., when it is first saved).
    * It ensures createdAt and updatedAt are set to the current time if they are null.*/
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

/**
 * This method is invoked whenever the entity is updated.
 * It ensures that updatedAt is set to the current time whenever the entity is modified. */
    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void addOrderToUser(Order theOrder){ //bi-directional method
        orders.add(theOrder);
        theOrder.setUser(this);
    }

    public void addBasketToUser(Basket theBasket){ //bi-directional method
        baskets.add(theBasket);
        theBasket.setUser(this);
    }
}