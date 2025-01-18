package com.fabiolima.e_commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
    private Basket basket;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "product_id")
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