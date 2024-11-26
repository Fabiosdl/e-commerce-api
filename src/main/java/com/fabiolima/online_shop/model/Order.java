package com.fabiolima.online_shop.model;

import com.fabiolima.online_shop.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString

@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE,
                    CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "user_id") //a user can place multiple orders
    private User user;

    @ToString.Exclude
    @OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, // a basket becomes an order
            CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "basket_id", unique = true, nullable = false)
    private Basket basket;

    @Column(name = "totalPrice")
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentStatus")
    private PaymentStatus paymentStatus;

    @Column(name = "createdAt")
    private OffsetDateTime orderCreatedAt;

    @Column(name = "updatedAt")
    private OffsetDateTime orderUpdatedAt;

    @PrePersist
    public void prePersist(){
        if(orderCreatedAt == null){
            orderCreatedAt = OffsetDateTime.now();
        }
        if(orderUpdatedAt == null){
            orderUpdatedAt = OffsetDateTime.now();
        }
    }
    @PreUpdate
    public void preUpdate(){
        orderUpdatedAt = OffsetDateTime.now();
    }
}