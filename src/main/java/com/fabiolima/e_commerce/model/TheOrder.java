package com.fabiolima.e_commerce.model;

import com.fabiolima.e_commerce.model.enums.OrderStatus;
import com.fabiolima.e_commerce.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

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
    //order holds the user foreign key, so its the owning side of the relationship
    private User user;

    @ToString.Exclude
    @OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, // a basket becomes an order
            CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "basket_id", unique = true, nullable = false) //order is the owning side of the relationship
    private Basket basket;                                           //and it's entity holds the basket foreign key

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