package com.fabiolima.online_shop.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String productName;

    @Column(name = "description")
    private String productDescription;

    @Column(name = "price")
    private BigDecimal productPrice;

    @Column(name = "stock")
    private int stock;

    @Column(name = "category")
    private String category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private final List<BasketItem> basketItemList = new ArrayList<>();

    @Column(name = "createdAt")
    private OffsetDateTime productCreatedAt;

    @Column(name = "updatedAt")
    private OffsetDateTime productUpdatedAt;

    @PrePersist
    public void prePersist(){

        if (productCreatedAt == null){
            productCreatedAt = OffsetDateTime.now();
        }
        if (productUpdatedAt == null){
            productUpdatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate(){
        productUpdatedAt = OffsetDateTime.now();
    }
}
