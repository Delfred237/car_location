package com.example.demo.stripe.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StripePaymentRequestDTO {

    @NotNull(message = "L'ID de la réservation est obligatoire")
    private Long reservationId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal amount;

    @NotNull(message = "La devise est obligatoire")
    private String currency = "XAF";

    private String description;
}
