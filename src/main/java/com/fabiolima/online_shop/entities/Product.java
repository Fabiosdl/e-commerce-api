package com.fabiolima.online_shop.entities;

import jakarta.persistence.Entity;
import lombok.*;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString

@Entity
public class Product {

    private int id;
    private String productName;
    private String productDescription;
    private double price;

}
