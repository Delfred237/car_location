package com.example.demo.controllers;

import com.example.demo.dto.request.PaymentRequestDTO;
import com.example.demo.dto.response.PaymentResponseDTO;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/payments")
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
        log.info("POST /api/payments - Création d'un paiement pour la réservation ID : {}", requestDTO.getReservationId());
        PaymentResponseDTO response = paymentService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer un paiement par ID
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        log.info("GET /api/payments/{} - Récupération du paiement", id);
        PaymentResponseDTO response = paymentService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Récupérer tous les paiements
     * GET /api/payments
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        log.info("GET /api/payments - Récupération de tous les paiements");
        List<PaymentResponseDTO> response = paymentService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Récupérer les paiements d'une réservation
     * GET /api/payments/reservation/{reservationId}
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByReservation(@PathVariable Long reservationId) {
        log.info("GET /api/payments/reservation/{} - Paiements de la réservation", reservationId);
        List<PaymentResponseDTO> response = paymentService.getByReservation(reservationId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Récupérer les paiements par statut
     * GET /api/payments/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        log.info("GET /api/payments/status/{} - Paiements par statut", status);
        List<PaymentResponseDTO> response = paymentService.getByStatus(status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Récupérer les paiements entre deux dates
     * GET /api/payments/dates?start=2026-01-01T00:00:00&end=2026-12-31T23:59:59
     */
    @GetMapping("/dates")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/payments/dates?start={}&end={}", start, end);
        List<PaymentResponseDTO> response = paymentService.getPaymentsBetweenDates(start, end);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Calculer le montant total payé pour une réservation
     * GET /api/payments/reservation/{reservationId}/total
     */
    @GetMapping("/reservation/{reservationId}/total")
    public ResponseEntity<Double> getTotalPaidAmount(@PathVariable Long reservationId) {
        log.info("GET /api/payments/reservation/{}/total - Calcul du montant total payé", reservationId);
        Double totalAmount = paymentService.getTotalPaidAmount(reservationId);
        return ResponseEntity.status(HttpStatus.OK).body(totalAmount);
    }

    /**
     * Vérifier si une réservation a un paiement complété
     * GET /api/payments/reservation/{reservationId}/has-completed
     */
    @GetMapping("/reservation/{reservationId}/has-completed")
    public ResponseEntity<Boolean> hasCompletedPayment(@PathVariable Long reservationId) {
        log.info("GET /api/payments/reservation/{}/has-completed", reservationId);
        Boolean hasCompleted = paymentService.hasCompletedPayment(reservationId);
        return ResponseEntity.status(HttpStatus.OK).body(hasCompleted);
    }

    /**
     * Obtenir les statistiques de paiement d'une réservation
     * GET /api/payments/reservation/{reservationId}/stats
     */
    @GetMapping("/reservation/{reservationId}/stats")
    public ResponseEntity<Map<String, Object>> getReservationPaymentStats(@PathVariable Long reservationId) {
        log.info("GET /api/payments/reservation/{}/stats", reservationId);
        Map<String, Object> stats = paymentService.getReservationPaymentStats(reservationId);
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

    /**
     * Obtenir le résumé des paiements par statut
     * GET /api/payments/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getPaymentsSummary() {
        log.info("GET /api/payments/summary");
        Map<String, Object> stats = paymentService.getPaymentsSummary();
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

    /**
     * Traiter un paiement (simuler le processus de paiement)
     * POST /api/payments/{id}/process
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(@PathVariable Long id) {
        log.info("POST /api/payments/{}/process - Traitement du paiement", id);
        PaymentResponseDTO response = paymentService.processPayment(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Mettre à jour le statut d'un paiement
     * PATCH /api/payments/{id}/status?newStatus=COMPLETED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponseDTO> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus newStatus) {
        log.info("PATCH /api/payments/{}/status?newStatus={}", id, newStatus);
        PaymentResponseDTO response = paymentService.updateStatus(id, newStatus);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Supprimer un paiement
     * DELETE /api/payments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        log.info("DELETE /api/payments/{} - Suppression du paiement", id);
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
