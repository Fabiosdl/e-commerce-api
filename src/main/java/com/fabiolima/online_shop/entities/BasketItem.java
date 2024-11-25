package com.fabiolima.online_shop.entities;

import com.fabiolima.online_shop.entities.enums.BasketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString

@Entity
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(
            cascade = {CascadeType.MERGE, CascadeType.DETACH,
            CascadeType.REFRESH, CascadeType.PERSIST}
    )
    @JoinColumn(name = "basket_id")
    private Basket basket;

    @ManyToOne(
            cascade = {CascadeType.MERGE, CascadeType.DETACH,
                    CascadeType.REFRESH, CascadeType.PERSIST}
    )
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "createdAt")
    private OffsetDateTime basketItemCreatedAt;

    @Column(name = "updatedAt")
    private OffsetDateTime basketItemUpdatedAt;

    @PrePersist
    public void prePersit(){
        if(basketItemCreatedAt == null){
            basketItemCreatedAt = OffsetDateTime.now();
        }
        if(basketItemUpdatedAt == null){
            basketItemUpdatedAt = OffsetDateTime.now();
        }
    }
    @PreUpdate
    public void preUpdate(){
        basketItemUpdatedAt = OffsetDateTime.now();
    }
}
