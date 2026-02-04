package com.example.demo.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "cars")
@Entity
public class Car extends BaseEntity {

    @NotBlank(message = "La marque est obligatoire")
    @Column(nullable = false, length = 50)
    private String brand;

    @NotBlank(message = "Le modèle est obligatoire")
    @Column(nullable = false, length = 50)
    private String model;

    @NotBlank(message = "La plaque d'immatriculation est obligatoire")
    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(name = "year_of_manufacture")
    private Integer yearOfManufacture;

    @Column(length = 30)
    private String color;

    @Column(name = "mileage", nullable = false)
    private Integer mileage = 0; // Kilometrage de la voiture

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency")
    private Agency agency;
}
