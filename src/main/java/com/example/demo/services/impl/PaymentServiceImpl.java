package com.example.demo.services.impl;

import com.example.demo.Repository.PaymentRepository;
import com.example.demo.Repository.ReservationRepository;
import com.example.demo.dto.request.PaymentRequestDTO;
import com.example.demo.dto.response.PaymentResponseDTO;
import com.example.demo.email.EmailService;
import com.example.demo.entites.Payment;
import com.example.demo.entites.Reservation;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mapper.PaymentMapper;
import com.example.demo.services.PaymentService;
import com.example.demo.stripe.StripePaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final StripePaymentService stripePaymentService;


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

        // Vérifier que le montant est valide
        BigDecimal totalPaid = getTotalPaidAmountBigDecimal(requestDTO.getReservationId());
        BigDecimal remainingAmount = reservation.getTotalPrice().subtract(totalPaid);

        if (requestDTO.getAmount().compareTo(remainingAmount) > 0) {
            throw new BusinessException(
                    String.format("Le montant dépasse le solde restant de %s FCFA", remainingAmount)
            );
        }

        // Créer le paiement avec statut PENDING
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

        // Vérifier la méthode de paiement
        if ("STRIPE".equalsIgnoreCase(payment.getPaymentMethod())) {
            // Le paiement Stripe est géré par le webhook
            // On ne fait rien ici car le statut sera mis à jour automatiquement
            throw new BusinessException("Les paiements Stripe sont gérés automatiquement via webhook");
        }

        // Pour les autres méthodes de paiement
        boolean paymentSuccessful = simulatePaymentProcessing(payment.getPaymentMethod());

        if (paymentSuccessful) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(generateTransactionId());

            // Mise à jour de la réservation si le paiement est complet
            handlePaymentCompleted(payment);
            log.info("Paiement traité avec succès : ID {}", id);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Échec du traitement du paiement : ID {}", id);
        }

        Payment processedPayment = paymentRepository.save(payment);
        return paymentMapper.toDTO(processedPayment);
    }

    @Override
    public Map<String, Object> getReservationPaymentStats(Long reservationId) {
        log.info("Traitement des statistiques de paiement d'une reservation {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        Double totalPaid = getTotalPaidAmount(reservationId);
        BigDecimal totalPaidBD = totalPaid != null ? BigDecimal.valueOf(totalPaid) : BigDecimal.ZERO;
        BigDecimal remaining = reservation.getTotalPrice().subtract(totalPaidBD);

        boolean isFullyPaid = totalPaidBD.compareTo(reservation.getTotalPrice()) >= 0;
        boolean hasCompleted = hasCompletedPayment(reservationId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("reservationId", reservationId);
        stats.put("totalAmount", reservation.getTotalPrice());
        stats.put("paidAmount", totalPaidBD);
        stats.put("remainingAmount", remaining.max(BigDecimal.ZERO));
        stats.put("isFullyPaid", isFullyPaid);
        stats.put("hasCompletedPayment", hasCompleted);
        stats.put("paymentPercentage", totalPaidBD.divide(reservation.getTotalPrice(), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)));

        return stats;
    }

    @Override
    public Map<String, Object> getPaymentsSummary() {
        log.info("Traitement du resume des paiements par statut");

        Map<String, Object> summary = new HashMap<>();

        for (PaymentStatus status : PaymentStatus.values()) {
            List<Payment> payments = paymentRepository.findByStatus(status);
            BigDecimal total = payments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> statusData = new HashMap<>();
            statusData.put("count", payments.size());
            statusData.put("totalAmount", total);

            summary.put(status.name(), statusData);
        }

        return summary;
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
            throw new BusinessException("Les paiements complétés ne peuvent pas être supprimés pour des raisons d'audit");
        }

        // Si c'est un paiement Stripe en attente, annuler le PaymentIntent
        if ("STRIPE".equalsIgnoreCase(payment.getPaymentMethod()) &&
                payment.getTransactionId() != null &&
                payment.getStatus() == PaymentStatus.PENDING) {
            try {
                stripePaymentService.cancelPayment(payment.getTransactionId());
            } catch (Exception e) {
                log.warn("Impossible d'annuler le PaymentIntent Stripe : {}", e.getMessage());
            }
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

    /**
     * Gérer les actions après qu'un paiement soit complété
     */
    private void handlePaymentCompleted(Payment payment) {
        Reservation reservation = payment.getReservation();

        // Calculer le montant total payé
        BigDecimal totalPaid = getTotalPaidAmountBigDecimal(reservation.getId());

        // Vérifier si le paiement est complet
        if (totalPaid.compareTo(reservation.getTotalPrice()) >= 0) {
            // Mise a jour du statut de la réservation à CONFIRMED si elle était en PENDING
            if (reservation.getStatus() == ReservationStatus.PENDING) {
                reservation.setStatus(ReservationStatus.CONFIRMED);
                reservationRepository.save(reservation);

                log.info("Réservation ID {} confirmée suite au paiement complet", reservation.getId());
            }
        }

        // Envoyer l'email de confirmation de paiement
        try {
            emailService.sendPaymentConfirmation(reservation, payment.getTransactionId());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de confirmation : {}", e.getMessage());
        }
    }

    /**
     * Simuler le traitement d'un paiement (pour les méthodes non-Stripe)
     */
    private boolean simulatePaymentProcessing(String paymentMethod) {
        // Simuler un taux de réussite de 95%
        // TODO: Remplacer par une vraie intégration de paiement
        if ("CASH".equalsIgnoreCase(paymentMethod)) {
            // Le paiement en espèces est toujours accepté manuellement
            return true;
        }

        if ("TRANSFER".equalsIgnoreCase(paymentMethod)) {
            // Simuler une vérification de virement bancaire
            return Math.random() > 0.05;
        }

        if ("CARD".equalsIgnoreCase(paymentMethod)) {
            // Simuler un paiement par carte (sans Stripe)
            return Math.random() > 0.05;
        }

        return false;
    }

    /**
     * Générer un ID de transaction unique
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" +
                String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * Récupérer le montant total payé en BigDecimal
     */
    private BigDecimal getTotalPaidAmountBigDecimal(Long reservationId) {
        Double totalPaidDouble = paymentRepository.calculateTotalPaidAmount(reservationId);
        return totalPaidDouble != null ? BigDecimal.valueOf(totalPaidDouble) : BigDecimal.ZERO;
    }
}
