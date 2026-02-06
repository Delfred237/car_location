package com.example.demo.controllers;

import com.example.demo.dto.request.PaymentRequestDTO;
import com.example.demo.dto.response.PaymentResponseDTO;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/payment")
@RequiredArgsConstructor
@RestController
public class PaymentController {

    private final PaymentService paymentService;


    /**
     * Créer un nouveau paiement
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO requestDTO) {
        System.out.println(String.format("POST /api/payments - Création d'un paiement pour la réservation ID : {}", requestDTO.getReservationId()));
        PaymentResponseDTO response = paymentService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer un paiement par ID
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        System.out.println(String.format("GET /api/payments/{} - Récupération du paiement", id));
        PaymentResponseDTO response = paymentService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer tous les paiements
     * GET /api/payments
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        System.out.println("GET /api/payments - Récupération de tous les paiements");
        List<PaymentResponseDTO> response = paymentService.getAll();
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les paiements d'une réservation
     * GET /api/payments/reservation/{reservationId}
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByReservation(@PathVariable Long reservationId) {
        System.out.println(String.format("GET /api/payments/reservation/{} - Paiements de la réservation", reservationId));
        List<PaymentResponseDTO> response = paymentService.getByReservation(reservationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les paiements par statut
     * GET /api/payments/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        System.out.println(String.format("GET /api/payments/status/{} - Paiements par statut", status));
        List<PaymentResponseDTO> response = paymentService.getByStatus(status);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les paiements entre deux dates
     * GET /api/payments/dates?start=2026-01-01T00:00:00&end=2026-12-31T23:59:59
     */
    @GetMapping("/dates")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        System.out.println(String.format("GET /api/payments/dates?start={}&end={}", start, end));
        List<PaymentResponseDTO> response = paymentService.getPaymentsBetweenDates(start, end);
        return ResponseEntity.ok(response);
    }

    /**
     * Calculer le montant total payé pour une réservation
     * GET /api/payments/reservation/{reservationId}/total
     */
    @GetMapping("/reservation/{reservationId}/total")
    public ResponseEntity<Double> getTotalPaidAmount(@PathVariable Long reservationId) {
        System.out.println(String.format("GET /api/payments/reservation/{}/total - Calcul du montant total payé", reservationId));
        Double totalAmount = paymentService.getTotalPaidAmount(reservationId);
        return ResponseEntity.ok(totalAmount);
    }

    /**
     * Vérifier si une réservation a un paiement complété
     * GET /api/payments/reservation/{reservationId}/has-completed
     */
    @GetMapping("/reservation/{reservationId}/has-completed")
    public ResponseEntity<Boolean> hasCompletedPayment(@PathVariable Long reservationId) {
        System.out.println(String.format("GET /api/payments/reservation/{}/has-completed", reservationId));
        Boolean hasCompleted = paymentService.hasCompletedPayment(reservationId);
        return ResponseEntity.ok(hasCompleted);
    }

    /**
     * Traiter un paiement (simuler le processus de paiement)
     * POST /api/payments/{id}/process
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(@PathVariable Long id) {
        System.out.println(String.format("POST /api/payments/{}/process - Traitement du paiement", id));
        PaymentResponseDTO response = paymentService.processPayment(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mettre à jour le statut d'un paiement
     * PATCH /api/payments/{id}/status?newStatus=COMPLETED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponseDTO> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus newStatus) {
        System.out.println(String.format("PATCH /api/payments/{}/status?newStatus={}", id, newStatus));
        PaymentResponseDTO response = paymentService.updateStatus(id, newStatus);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un paiement
     * DELETE /api/payments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        System.out.println(String.format("DELETE /api/payments/{} - Suppression du paiement", id));
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
