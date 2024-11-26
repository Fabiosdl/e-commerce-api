package com.fabiolima.online_shop.model;

import com.fabiolima.online_shop.model.enums.BasketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString

@Entity
public class Basket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ToString.Exclude
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, // a user can have multiple baskets
                CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "user_id") //column in the database that will join user to basket
    private User user;

    @ToString.Exclude
    @OneToOne(mappedBy = "basket", cascade = CascadeType.ALL)
    private Order order;


    @ToString.Exclude
    @OneToMany(mappedBy = "basket", // field in BasketItem Class
            cascade = CascadeType.ALL
    )
    private final List<BasketItem> basketItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BasketStatus basketStatus;

    @Column(name = "createdAt")
    private OffsetDateTime basketCreatedAt;

    @Column(name = "updatedAt")
    private OffsetDateTime basketUpdatedAt;

    @PrePersist
    public void prePersit(){
        if(basketCreatedAt == null){
            basketCreatedAt = OffsetDateTime.now();
        }
        if(basketUpdatedAt == null){
            basketUpdatedAt = OffsetDateTime.now();
        }
        if(basketStatus == null){
            basketStatus = BasketStatus.OPEN;
        }
    }
    @PreUpdate
    public void preUpdate(){
        basketUpdatedAt = OffsetDateTime.now();
    }
}
