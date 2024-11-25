package com.fabiolima.online_shop.entities;

import com.fabiolima.online_shop.entities.enums.BasketStatus;
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
    private int id;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE,
                CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "user_id") //column in the database that will join user to basket
    private User user;

    @OneToMany(mappedBy = "basket", // field in BasketItem Class
            cascade = CascadeType.ALL
    )
    private final List<BasketItem> basketItems = new ArrayList<>();

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
