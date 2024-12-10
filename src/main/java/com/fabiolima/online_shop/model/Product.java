package com.fabiolima.online_shop.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString

@Entity
@Table(name = "product")
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

    @ToString.Exclude // to avoid infinite looping
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private final List<BasketItem> basketItemList = new ArrayList<>();

    public void addProductInBasketItem(BasketItem theBasketItem){
        basketItemList.add(theBasketItem);
        theBasketItem.setProduct(this);
    }

}
