package com.example.demo.controllers;

import com.example.demo.dto.request.ReservationRequestDTO;
import com.example.demo.dto.response.ReservationResponseDTO;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/reservations")
@RequiredArgsConstructor
@RestController
public class ReservationController {

    private final ReservationService reservationService;


    /**
     * Créer une nouvelle réservation
     * POST /api/reservations?userId=1
     *
     * Note: Dans une vraie app, userId viendrait du token JWT (authentification)
     */
    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody ReservationRequestDTO requestDTO) {
        log.info("POST /api/reservations?userId={} - Création d'une réservation", requestDTO.getUserId());
        ReservationResponseDTO response = reservationService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer une réservation par ID
     * GET /api/reservations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getReservationById(@PathVariable Long id) {
        log.info("GET /api/reservations/{} - Récupération de la réservation", id);
        ReservationResponseDTO response = reservationService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Récupérer toutes les réservations
     * GET /api/reservations
     */
    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations() {
        log.info("GET /api/reservations - Récupération de toutes les réservations");
        List<ReservationResponseDTO> response = reservationService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Récupérer les réservations d'un utilisateur
     * GET /api/reservations/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByUser(@PathVariable Long userId) {
        log.info("GET /api/reservations/user/{} - Réservations de l'utilisateur", userId);
        List<ReservationResponseDTO> response = reservationService.getByUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Récupérer les réservations d'une voiture
     * GET /api/reservations/car/{carId}
     */
    @GetMapping("/car/{carId}")
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByCar(@PathVariable Long carId) {
        log.info("GET /api/reservations/car/{} - Réservations de la voiture", carId);
        List<ReservationResponseDTO> response = reservationService.getByCar(carId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Récupérer les réservations par statut
     * GET /api/reservations/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByStatus(@PathVariable ReservationStatus status) {
        log.info("GET /api/reservations/status/{} - Réservations par statut", status);
        List<ReservationResponseDTO> response = reservationService.getByStatus(status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Récupérer les réservations à venir d'un utilisateur
     * GET /api/reservations/user/{userId}/upcoming
     */
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<ReservationResponseDTO>> getUpcomingReservations(@PathVariable Long userId) {
        log.info("GET /api/reservations/user/{}/upcoming - Réservations à venir", userId);
        List<ReservationResponseDTO> response = reservationService.getUpcomingReservationsByUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Récupérer l'historique des réservations d'un utilisateur
     * GET /api/reservations/user/{userId}/history
     */
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<ReservationResponseDTO>> getReservationHistory(@PathVariable Long userId) {
        log.info("GET /api/reservations/user/{}/history - Historique des réservations", userId);
        List<ReservationResponseDTO> response = reservationService.getReservationHistoryByUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Mettre à jour le statut d'une réservation
     * PATCH /api/reservations/{id}/status?newStatus=CONFIRMED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationResponseDTO> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus newStatus) {
        log.info("PATCH /api/reservations/{}/status?newStatus={}", id, newStatus);
        ReservationResponseDTO response = reservationService.updateStatus(id, newStatus);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Annuler une réservation
     * POST /api/reservations/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancelReservation(@PathVariable Long id) {
        log.info("POST /api/reservations/{}/cancel - Annulation de la réservation", id);
        ReservationResponseDTO response = reservationService.cancelReservation(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Supprimer une réservation
     * DELETE /api/reservations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        log.info("DELETE /api/reservations/{} - Suppression de la réservation", id);
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
