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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        return paymentMapper.toDTO(savedPayment);
    }

    @Override
    public PaymentResponseDTO getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        return paymentMapper.toDTO(payment);
    }

    @Override
    public List<PaymentResponseDTO> getAll() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDTO> getByReservation(Long reservationId) {
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
        return paymentRepository.findByStatus(status).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findPaymentsBetweenDates(startDate, endDate).stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponseDTO updateStatus(Long id, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        // Valider la transition de statut
        validateStatusTransition(payment.getStatus(), newStatus);

        payment.setStatus(newStatus);
        Payment updatedPayment = paymentRepository.save(payment);

        return paymentMapper.toDTO(updatedPayment);
    }

    @Override
    @Transactional
    public PaymentResponseDTO processPayment(Long id) {
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
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment processedPayment = paymentRepository.save(payment);
        return paymentMapper.toDTO(processedPayment);
    }

    @Override
    public Double getTotalPaidAmount(Long reservationId) {
        return 0.0;
    }

    @Override
    public boolean hasCompletedPayment(Long reservationId) {
        return false;
    }

    @Override
    public void delete(Long id) {

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
