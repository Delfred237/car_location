package com.example.demo.stripe.service;

import com.example.demo.stripe.dto.StripePaymentRequestDTO;
import com.example.demo.stripe.dto.StripePaymentResponseDTO;
import com.stripe.model.Event;

import java.math.BigDecimal;

public interface StripePaymentService {

    /**
     * Créer une session de paiement Stripe Checkout
     */
    StripePaymentResponseDTO createCheckoutSession(StripePaymentRequestDTO requestDTO);

    /**
     * Gérer les webhooks Stripe
     */
    void handleWebhookEvent(Event event);

    /**
     * Annuler un paiement
     */
    void cancelPayment(String paymentIntentId);

    /**
     * Rembourser un paiement
     */
    void refundPayment(String paymentIntentId, BigDecimal amount);

    /**
     * Récupérer le statut d'un paiement
     */
    String getPaymentStatus(String paymentIntentId);
}
