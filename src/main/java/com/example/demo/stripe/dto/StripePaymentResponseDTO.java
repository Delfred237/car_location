package com.example.demo.stripe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StripePaymentResponseDTO {

    private String sessionId;
    private String sessionUrl;  // URL de redirection vers Stripe
    private String paymentIntentId;
    private String status;
    private Long reservationId;
}
