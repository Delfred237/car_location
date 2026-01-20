package com.example.demo.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "cars")
@Entity
public class Car extends BaseEntity {

    private String brand;
    private String model;
    private String registration;
    private Integer yearOfManufacture;
    private String color;
    private Integer mileage; // Kilometrage de la voiture
    private Category category;
    private Agency agency;
}
