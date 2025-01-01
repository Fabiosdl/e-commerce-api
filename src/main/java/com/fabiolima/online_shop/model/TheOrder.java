package com.fabiolima.online_shop.model;

import com.fabiolima.online_shop.model.enums.OrderStatus;
import com.fabiolima.online_shop.model.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "the_order")
public class TheOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE,
                    CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "user_id") //a user can place multiple orders
    //@JsonBackReference
    private User user;

    @ToString.Exclude
    @OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, // a basket becomes an order
            CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "basket_id", unique = true, nullable = false)
    private Basket basket;

    @Column(name = "total_price")
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @PrePersist
    public void defaultOrderAndPaymentStatus(){
        if(orderStatus == null)
            orderStatus = OrderStatus.PENDING;
        if(paymentStatus == null)
            paymentStatus = PaymentStatus.PENDING;

    }

}