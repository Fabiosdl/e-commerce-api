package com.fabiolima.online_shop.model;

import com.fabiolima.online_shop.model.enums.BasketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString

@Entity
@Table(name = "basket")
public class Basket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BasketStatus basketStatus;

    @ToString.Exclude
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, // a user can have multiple baskets
                CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "user_id") //column in the database that will join user to basket
    private User user;

    @ToString.Exclude
    @OneToOne(mappedBy = "basket", cascade = CascadeType.ALL)
    private TheOrder order;


    @ToString.Exclude
    @OneToMany(mappedBy = "basket", // field in BasketItem Class
            cascade = CascadeType.ALL
    )
    private final List<BasketItem> basketItems = new ArrayList<>();

    @PrePersist
    public void defaultBasketStatus(){
        if(basketStatus == null)
            basketStatus = BasketStatus.OPEN;
    }

    //bidirectional helper method
    public void addBasketItemToBasket(BasketItem theBasketItem){
        basketItems.add(theBasketItem);
        theBasketItem.setBasket(this);
    }

}
