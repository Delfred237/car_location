package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CarResponseDTO {

    private Long id;
    private String brand;
    private String model;
    private String licensePlate;
    private Integer yearOfManufacture;
    private String color;
    private Integer mileage;

    // Infos de la catégorie
    private Long categoryId;
    private String categoryName;

    // Infos de l'agence (optionnel)
    private Long agencyId;
    private String agencyName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
