package com.example.demo.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StripeWebhookEventDTO {

    private String eventId;
    private String eventType;
    private String paymentIntentId;
    private String sessionId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Map<String, String> metadata;
}
