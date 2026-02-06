package com.example.demo.services.impl;

import com.example.demo.Repository.PaymentRepository;
import com.example.demo.Repository.ReservationRepository;
import com.example.demo.dto.request.PaymentRequestDTO;
import com.example.demo.dto.response.PaymentResponseDTO;
import com.example.demo.email.EmailService;
import com.example.demo.entites.Payment;
import com.example.demo.entites.Reservation;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mapper.PaymentMapper;
import com.example.demo.services.PaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@AllArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentMapper paymentMapper;
    private final EmailService emailService;


    @Override
    @Transactional
    public PaymentResponseDTO create(PaymentRequestDTO requestDTO) {
        log.info("Création d'un nouveau paiement pour la réservation ID : {}", requestDTO.getReservationId());

        // Récupérer la réservation
        Reservation reservation = reservationRepository.findById(requestDTO.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", requestDTO.getReservationId()));

        // Vérifier que le montant est valide
        if (requestDTO.getAmount().compareTo(reservation.getTotalPrice()) > 0) {
            throw new BusinessException("Le montant du paiement dépasse le montant total de la réservation");
        }

        Payment payment = paymentMapper.toEntity(requestDTO);
        payment.setReservation(reservation);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Paiement créé avec succès : ID {}", savedPayment.getId());
        return paymentMapper.toDTO(savedPayment);
    }

    @Override
    public PaymentResponseDTO getById(Long id) {
        log.info("Recherche du paiement avec ID : {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        return paymentMapper.toDTO(payment);
    }

    @Override
    public List<PaymentResponseDTO> getAll() {
        log.info("Récupération de tous les paiements");

        return paymentRepository.findAll().stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDTO> getByReservation(Long reservationId) {
        log.info("Recherche des paiements de la réservation ID : {}", reservationId);

        // Vérifier que la réservation existe
        if (!reservationRepository.existsById(reservationId)) {
            throw new ResourceNotFoundException("Reservation", "id", reservationId);
        }

        return paymentRepository.findByReservationId(reservationId).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDTO> getByStatus(PaymentStatus status) {
        log.info("Recherche des paiements avec le statut : {}", status);

        return paymentRepository.findByStatus(status).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Recherche des paiements entre {} et {}", startDate, endDate);

        return paymentRepository.findPaymentsBetweenDates(startDate, endDate).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponseDTO updateStatus(Long id, PaymentStatus newStatus) {
        log.info("Mise à jour du statut du paiement ID {} vers {}", id, newStatus);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        // Valider la transition de statut
        validateStatusTransition(payment.getStatus(), newStatus);

        payment.setStatus(newStatus);
        Payment updatedPayment = paymentRepository.save(payment);

        log.info("Statut du paiement mis à jour avec succès : ID {}", updatedPayment.getId());
        return paymentMapper.toDTO(updatedPayment);
    }

    @Override
    @Transactional
    public PaymentResponseDTO processPayment(Long id) {
        log.info("Traitement du paiement ID : {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Seuls les paiements en attente peuvent être traités");
        }

        // TODO: Intégrer avec un système de paiement réel (Stripe, PayPal, etc.)
        boolean paymentSuccessful = simulatePaymentProcessing();

        if (paymentSuccessful) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(generateTransactionId());

            // Envoie de l'email de confirmation de paiement
            emailService.sendPaymentConfirmation(payment.getReservation(), payment.getTransactionId());
            log.info("Paiement traité avec succès : ID {}", id);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Échec du traitement du paiement : ID {}", id);
        }

        Payment processedPayment = paymentRepository.save(payment);
        return paymentMapper.toDTO(processedPayment);
    }

    @Override
    public Double getTotalPaidAmount(Long reservationId) {
        log.info("Calcul du montant total payé pour la réservation ID : {}", reservationId);

        // Vérifier que la réservation existe
        if (!reservationRepository.existsById(reservationId)) {
            throw new ResourceNotFoundException("Reservation", "id", reservationId);
        }

        return paymentRepository.calculateTotalPaidAmount(reservationId);
    }

    @Override
    public boolean hasCompletedPayment(Long reservationId) {
        log.info("Vérification de l'existence d'un paiement complété pour la réservation ID : {}", reservationId);

        // Vérifier que la réservation existe
        if (!reservationRepository.existsById(reservationId)) {
            throw new ResourceNotFoundException("Reservation", "id", reservationId);
        }

        return paymentRepository.hasCompletedPayment(reservationId);
    }

    @Override
    public void delete(Long id) {
        log.info("Suppression du paiement ID : {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        // Ne pas supprimer les paiements complétés (pour l'audit)
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new BusinessException("Les paiements complétés ne peuvent pas être supprimés");
        }

        paymentRepository.delete(payment);
        log.info("Paiement supprimé avec succès : ID {}", id);
    }


    // ========== MÉTHODES PRIVÉES ==========
    private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == PaymentStatus.COMPLETED
                    || newStatus == PaymentStatus.FAILED;
            case COMPLETED -> newStatus == PaymentStatus.REFUNDED;
            case FAILED -> newStatus == PaymentStatus.PENDING; // Retry
            case REFUNDED -> false; // État final
        };

        if (!isValidTransition) {
            throw new BusinessException(
                    String.format("Transition de statut invalide : %s -> %s", currentStatus, newStatus)
            );
        }
    }

    private boolean simulatePaymentProcessing() {
        // Simuler un taux de réussite de 95%
        // TODO: Remplacer par une vraie intégration de paiement
        return Math.random() > 0.05;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
