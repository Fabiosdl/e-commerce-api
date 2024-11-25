package com.fabiolima.online_shop.entities;

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
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String categoryName;

    @Column(name = "description")
    private String categoryDescription;

    @Column(name = "createdAt")
    private OffsetDateTime categoryCreatedAt;

    @Column(name = "updatedAt")
    private OffsetDateTime categoryUpdatedAt;

    @PrePersist
    public void prePersit(){
        if(categoryCreatedAt == null){
            categoryCreatedAt = OffsetDateTime.now();
        }
        if(categoryUpdatedAt == null){
            categoryUpdatedAt = OffsetDateTime.now();
        }
    }
    @PreUpdate
    public void preUpdate(){
        categoryUpdatedAt = OffsetDateTime.now();
    }

}
