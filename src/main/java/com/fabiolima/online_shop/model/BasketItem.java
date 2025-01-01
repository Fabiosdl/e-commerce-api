package com.fabiolima.online_shop.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString

@Entity
@Table(name = "basket_item")
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "basket_id")
    //@JsonBackReference
    private Basket basket;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "product_id")
    //@JsonBackReference
    private Product product;

    @Column(name = "quantity")
    @Min(1)
    private int quantity;

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

}
