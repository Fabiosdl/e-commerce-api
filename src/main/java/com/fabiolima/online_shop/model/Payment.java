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
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "paymentMethod")
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus paymentStatus;

    @Column(name = "transactionId")
    private String transactionId;

    @Column(name = "createdAt")
    private OffsetDateTime paymentCreatedAt;

    @PrePersist
    public void prePersist(){
        if (paymentCreatedAt == null){
            paymentCreatedAt = OffsetDateTime.now();
        }
        if (paymentStatus == null){
            paymentStatus = PaymentStatus.PENDING;
        }
    }
}
