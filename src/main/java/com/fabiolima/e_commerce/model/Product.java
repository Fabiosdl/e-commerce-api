package com.fabiolima.e_commerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String productName;

    @Column(name = "description")
    private String productDescription;

    @Column(name = "price")
    private BigDecimal productPrice;

    @Column(name = "image_src")
    private String imageSrc;

    @Column(name = "stock")
    private int stock;

    @Column(name = "category")
    private String category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private final List<BasketItem> basketItemList = new ArrayList<>();

    public void addProductInBasketItem(BasketItem theBasketItem){
        basketItemList.add(theBasketItem);
        theBasketItem.setProduct(this);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", productPrice=" + productPrice +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                '}';
    }
}
