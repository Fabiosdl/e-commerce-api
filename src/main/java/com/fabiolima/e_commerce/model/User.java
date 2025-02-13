package com.fabiolima.e_commerce.model;

import com.fabiolima.e_commerce.model.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;


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
    @NotBlank
    @Size(min = 2, max = 40, message = "Name has to have a minimum of 2 characters and a maximum of 40.")
    private String name;

    @Column(name = "email",unique = true,nullable = false)
    private String email;

    @Column(name = "password")
    @NotBlank
    private String password;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus userStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime lastUpdated; // Automatically updated by Hibernate or DB

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "user_roles", //join table name
            joinColumns = @JoinColumn(name = "user_id"), // foreign key in the join table for user
            inverseJoinColumns = @JoinColumn(name = "role_id") // foreign key in the join table for role
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user",  //field "user" in Order Class
            cascade = CascadeType.ALL)
    @JsonIgnore
    private final List<Order> orders = new ArrayList<>();
    //it's final to reassure that the orders list belongs to the user

    @ToString.Exclude
    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL)
    @JsonIgnore
    private final List<Basket> baskets = new ArrayList<>();

    public void addOrderToUser(Order order){ //bi-directional method
        orders.add(order);
        order.setUser(this);
    }

    public void addBasketToUser(Basket theBasket){ //bi-directional method
        baskets.add(theBasket);
        theBasket.setUser(this);
    }

    public void addRoleToUser(Role role){
        roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRoleFromUser(Role role){
        roles.remove(role);
        role.getUsers().remove(this);
    }

    @PrePersist
    public void defaultUserStatus(){
        if(userStatus == null){
            userStatus = UserStatus.ACTIVE;
        }
    }
}