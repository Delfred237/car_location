package com.example.demo.dto.request;

import com.example.demo.entites.Agency;
import com.example.demo.entites.Category;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CarRequestDTO {

    @NotBlank(message = "La marque est obligatoire")
    @Size(min = 2, max = 50, message = "La marque doit contenir entre 2 et 50 caractères")
    private String brand;

    @NotBlank(message = "Le modèle est obligatoire")
    @Size(min = 2, max = 50, message = "Le modèle doit contenir entre 2 et 50 caractères")
    private String model;

    @NotBlank(message = "La plaque d'immatriculation est obligatoire")
    @Size(min = 5, max = 20, message = "La plaque doit contenir entre 5 et 20 caractères")
    private String registration;

    @Min(value = 1900, message = "L'année de fabrication doit être supérieure à 1900")
    @Max(value = 2030, message = "L'année de fabrication ne peut pas dépasser 2030")
    private Integer yearOfManufacture;

    @Column(length = 30)
    private String color;

    @Min(value = 0, message = "Le kilométrage ne peut pas être négatif")
    private Integer mileage = 0; // Kilometrage de la voiture

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;

    private Long agencyId;
}
