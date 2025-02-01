package com.fabiolima.e_commerce.model;

import com.fabiolima.e_commerce.model.enums.BasketStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString

@Entity
@Table(name = "basket")
public class Basket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BasketStatus basketStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime lastUpdated; // Automatically updated by Hibernate or DB

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, // a user can have multiple baskets
                CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "user_id") //column in the database that will join user to basket
    private User user;

    @ToString.Exclude
    @OneToOne(mappedBy = "basket", cascade = CascadeType.ALL)
    @JsonIgnore
    private Order order;

    @ToString.Exclude
    @OneToMany(mappedBy = "basket", // field in BasketItem Class
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private final List<BasketItem> basketItems = new ArrayList<>();

    @PrePersist
    public void defaultBasketStatus(){
        if(basketStatus == null)
            basketStatus = BasketStatus.ACTIVE;
    }

    //bidirectional helper method
    public void addBasketItemToBasket(BasketItem theBasketItem){
        basketItems.add(theBasketItem);
        theBasketItem.setBasket(this);
    }

}
