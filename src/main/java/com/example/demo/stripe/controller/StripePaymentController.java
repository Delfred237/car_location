package com.example.demo.stripe.controller;

import com.example.demo.stripe.dto.StripePaymentRequestDTO;
import com.example.demo.stripe.dto.StripePaymentResponseDTO;
import com.example.demo.stripe.service.StripePaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequestMapping("/stripe")
@RequiredArgsConstructor
@RestController
public class StripePaymentController {

    private final StripePaymentService stripePaymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;


    /**
     * Créer une session de paiement Stripe Checkout
     * POST /api/stripe/create-checkout-session
     */
    @PostMapping("/create-checkout-session")
    public ResponseEntity<StripePaymentResponseDTO> createCheckoutSession(
            @Valid @RequestBody StripePaymentRequestDTO requestDTO) {
        System.out.println(String.format("POST /api/stripe/create-checkout-session - Réservation ID : {}", requestDTO.getReservationId()));
        StripePaymentResponseDTO response = stripePaymentService.createCheckoutSession(requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Créer un PaymentIntent
     * POST /api/stripe/create-payment-intent
     */
    @PostMapping("/create-payment-intent")
    public ResponseEntity<StripePaymentResponseDTO> createPaymentIntent(
            @Valid @RequestBody StripePaymentRequestDTO requestDTO) {
        System.out.println(String.format("POST /api/stripe/create-payment-intent - Réservation ID : {}", requestDTO.getReservationId()));
        StripePaymentResponseDTO response = stripePaymentService.createPaymentIntent(requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Webhook Stripe pour recevoir les événements
     * POST /api/stripe/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        System.out.println("POST /api/stripe/webhook - Réception d'un webhook Stripe");

        Event event;

        try {
            // Vérifier la signature du webhook
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            System.out.println(String.format("⚠️  Signature du webhook invalide : {}", e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // Traiter l'événement
        stripePaymentService.handleWebhookEvent(event);

        return ResponseEntity.status(HttpStatus.OK).body("Webhook received");
    }

    /**
     * Annuler un paiement
     * POST /api/stripe/cancel/{paymentIntentId}
     */
    @PostMapping("/cancel/{paymentIntentId}")
    public ResponseEntity<Void> cancelPayment(@PathVariable String paymentIntentId) {
        System.out.println(String.format("POST /api/stripe/cancel/{} - Annulation du paiement", paymentIntentId));
        stripePaymentService.cancelPayment(paymentIntentId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Rembourser un paiement
     * POST /api/stripe/refund/{paymentIntentId}?amount=50000
     */
    @PostMapping("/refund/{paymentIntentId}")
    public ResponseEntity<Void> refundPayment(
            @PathVariable String paymentIntentId,
            @RequestParam BigDecimal amount) {
        System.out.println(String.format("POST /api/stripe/refund/{} - Montant : {}", paymentIntentId, amount));
        stripePaymentService.refundPayment(paymentIntentId, amount);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Récupérer le statut d'un paiement
     * GET /api/stripe/status/{paymentIntentId}
     */
    @GetMapping("/status/{paymentIntentId}")
    public ResponseEntity<String> getPaymentStatus(@PathVariable String paymentIntentId) {
        System.out.println(String.format("GET /api/stripe/status/{}", paymentIntentId));
        String status = stripePaymentService.getPaymentStatus(paymentIntentId);
        return ResponseEntity.status(HttpStatus.OK).body(status);
    }
}
