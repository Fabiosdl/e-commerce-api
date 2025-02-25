package com.fabiolima.e_commerce.model;

import com.fabiolima.e_commerce.model.enums.OrderStatus;
import com.fabiolima.e_commerce.model.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "`order`")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paypal_order_id",unique = true)
    private String paypalOrderId;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE,
                    CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "user_id") //a user can place multiple orders
    //order holds the user foreign key, so its the owning side of the relationship
    @JsonIgnore
    private User user;

    @ToString.Exclude
    @OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, // a basket becomes an order
            CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "basket_id", unique = true, nullable = false) //order is the owning side of the relationship
    @JsonIgnore
    private Basket basket;                                           //and it's entity holds the basket foreign key

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "order_list")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime lastUpdated;

    //bidirectional helper method
    public void addOrderItemToOrder(OrderItem orderItem){
        if(items == null){
            items = new ArrayList<>();
        }
        items.add(orderItem);
        orderItem.setOrder(this);
    }

    @PrePersist
    public void defaultOrderAndPaymentStatus(){
        if(orderStatus == null)
            orderStatus = OrderStatus.PENDING;
        if(paymentStatus == null)
            paymentStatus = PaymentStatus.PENDING;

    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", paypalOrderId='" + paypalOrderId + '\'' +
                ", user=" + user +
                ", totalPrice=" + totalPrice +
                ", items=" + items +
                ", paymentStatus=" + paymentStatus +
                ", orderStatus=" + orderStatus +
                '}';
    }
}