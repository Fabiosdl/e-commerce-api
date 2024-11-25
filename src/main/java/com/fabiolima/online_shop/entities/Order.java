package com.fabiolima.online_shop.entities;

import com.fabiolima.online_shop.entities.enums.PaymentStatus;
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
    private int id;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE,
                    CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "user_id") //name of the column in the database
    private User user;

    @OneToOne
    private Basket basket;

    @Column(name = "totalPrice")
    private double totalPrice;

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