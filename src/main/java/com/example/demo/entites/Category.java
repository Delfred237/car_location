package com.example.demo.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "categories")
@Entity
public class Category extends BaseEntity {

    private String name;
    private String description;
    private BigDecimal pricePerDay;
}
