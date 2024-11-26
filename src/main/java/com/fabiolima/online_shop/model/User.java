package com.fabiolima.online_shop.model;

import com.fabiolima.online_shop.model.enums.UserRole;
import com.fabiolima.online_shop.model.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    private Long id;

    @Column(name = "name")
    @Size(min = 2, max = 20)
    private String name;

    @Column(name = "email")
    @Email(regexp = "[a-z0-9._%-+]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus userStatus;

    @Column(name = "createdAt")
    private OffsetDateTime userCreatedAt;

    @Column(name = "updatedAt")
    private OffsetDateTime userUpdatedAt;

    @ToString.Exclude
    @OneToMany(mappedBy = "user",  //field "user" in Order Class
            cascade = CascadeType.ALL)
    private final List<Order> orders = new ArrayList<>();
    //it's final to reassure that the orders list belongs to the user

    @ToString.Exclude
    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL)
    private final List<Basket> baskets = new ArrayList<>();

    /**
    * This method is invoked before the entity is persisted (i.e., when it is first saved).
    * It ensures createdAt and updatedAt are set to the current time if they are null.*/
    @PrePersist
    public void prePersist() {
        if (userCreatedAt == null) {
            userCreatedAt = OffsetDateTime.now();
        }
        if (userUpdatedAt == null) {
            userUpdatedAt = OffsetDateTime.now();
        }
        if (userRole == null) {
            userRole = UserRole.CUSTOMER;
        }
        if (userStatus == null) {
            userStatus = UserStatus.ENABLED;
        }
    }

    /**
    * This method is invoked whenever the entity is updated.
    * It ensures that updatedAt is set to the current time whenever the entity is modified. */
    @PreUpdate
    public void preUpdate() {
        userUpdatedAt = OffsetDateTime.now();
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