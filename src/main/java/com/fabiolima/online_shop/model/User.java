package com.fabiolima.online_shop.model;

import com.fabiolima.online_shop.model.enums.UserRole;
import com.fabiolima.online_shop.model.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

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
    @Size(min = 2, max = 40, message = "Name has to have a minimum of 2 characters and a maximum of 40.")
    private String name;

    @Column(name = "email")
    @Email(regexp = "[a-z0-9._%-+]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email;

    @Column(name = "password")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus userStatus;

    @OneToMany(mappedBy = "user",  //field "user" in Order Class
            cascade = CascadeType.ALL)
    @JsonIgnore
    private final List<TheOrder> orders = new ArrayList<>();
    //it's final to reassure that the orders list belongs to the user

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL)
    @JsonIgnore
    private final List<Basket> baskets = new ArrayList<>();

    public void addOrderToUser(TheOrder theOrder){ //bi-directional method
        orders.add(theOrder);
        theOrder.setUser(this);
    }

    public void addBasketToUser(Basket theBasket){ //bi-directional method
        baskets.add(theBasket);
        theBasket.setUser(this);
    }

    @PrePersist
    public void defaultUserStatus(){
        if(userStatus == null){
            userStatus = UserStatus.ACTIVE;
        }
        if(userRole == null){
            userRole = UserRole.CUSTOMER;
        }
    }
}