package com.fabiolima.e_commerce.model;

import com.fabiolima.e_commerce.model.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
    @NotBlank(message = "Email should not be blank.")
    @Email(regexp = "[a-z0-9._%-+]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Invalid email format.")
    private String email;

    @Column(name = "password")
    @Size(min = 8)
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must have at least 8 characters, one uppercase letter, one number, and one special character."
    )
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
    private final List<TheOrder> orders = new ArrayList<>();
    //it's final to reassure that the orders list belongs to the user

    @ToString.Exclude
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