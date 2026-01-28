package com.example.demo.dto.response;

import com.example.demo.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReservationResponseDTO {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private ReservationStatus status;

    // Infos de l'utilisateur
    private Long userId;
    private String userFullName;
    private String userEmail;

    // Infos de la voiture
    private Long carId;
    private String carBrand;
    private String carModel;
    private String carLicensePlate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
