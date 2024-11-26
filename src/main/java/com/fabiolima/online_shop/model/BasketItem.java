package com.fabiolima.online_shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.OffsetDateTime;

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
    private Long id;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "basket_id")
    private Basket basket;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity")
    @Min(1)
    private int quantity;

    @Column(name = "createdAt")
    private OffsetDateTime basketItemCreatedAt;

    @Column(name = "updatedAt")
    private OffsetDateTime basketItemUpdatedAt;

    public void incrementQuantity(int amount) {
        if (amount > 0) {
            this.quantity += amount;
        }
    }

    public void decrementQuantity(int amount) {
        if (amount > 0 && this.quantity >= amount) {
            this.quantity -= amount;
        }
    }

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
