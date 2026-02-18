package com.example.demo.stripe.service;

import com.example.demo.Repository.PaymentRepository;
import com.example.demo.Repository.ReservationRepository;
import com.example.demo.stripe.repository.StripeEventLogRepository;
import com.example.demo.email.EmailService;
import com.example.demo.entites.Payment;
import com.example.demo.entites.Reservation;
import com.example.demo.entites.StripeEventLog;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.stripe.dto.StripePaymentRequestDTO;
import com.example.demo.stripe.dto.StripePaymentResponseDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class StripePaymentServiceImpl implements StripePaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final StripeEventLogRepository stripeEventLogRepository;
    private final EmailService emailService;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;


    @Override
    public StripePaymentResponseDTO createCheckoutSession(StripePaymentRequestDTO requestDTO) {
        log.info("Création d'une session Stripe Checkout pour la réservation ID : {}", requestDTO.getReservationId());

        try {
            // Recuperer la reservation
            Reservation reservation = reservationRepository.findById(requestDTO.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", requestDTO.getReservationId()));

            BigDecimal amount = reservation.getTotalPrice();
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            // Metadonnees pour retrouver la reservation dans le webhook
            Map<String, String> metadata = new HashMap<>();
            metadata.put("reservationId", String.valueOf(reservation.getId()));
            metadata.put("userId", String.valueOf(reservation.getUser().getId()));
            metadata.put("cardId", String.valueOf(reservation.getCar().getId()));

            // Creer les parametres de la session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(requestDTO.getCurrency().toLowerCase())
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Location de vehicule")
                                                                    .setDescription(
                                                                            String.format("%s %s - Du %s au %s",
                                                                                    reservation.getCar().getBrand(),
                                                                                    reservation.getCar().getModel(),
                                                                                    reservation.getStartDate(),
                                                                                    reservation.getEndDate())
                                                                    )
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putAllMetadata(metadata)
                    .setCustomerEmail(reservation.getUser().getEmail())
                    .build();

            // Creer la session Stripe
            Session session = Session.create(params);

            // Creer l'enregistrement du paiement en base de donnee
            Payment payment = Payment.builder()
                    .reservation(reservation)
                    .amount(requestDTO.getAmount())
                    .paymentMethod("STRIPE")
                    .paymentDate(LocalDateTime.now())
                    .status(PaymentStatus.PENDING)
                    .transactionId(session.getPaymentIntent())
                    .build();
            paymentRepository.save(payment);

            log.info("Session Stripe créée avec succès : {}", session.getId());

            return StripePaymentResponseDTO.builder()
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .paymentIntentId(session.getPaymentIntent())
                    .status("pending")
                    .reservationId(reservation.getId())
                    .build();
        } catch (StripeException e) {
            log.error("Erreur Stripe lors de la création de la session : {}", e.getMessage(), e);
            throw new BusinessException("Impossible de créer la session de paiement Stripe : " + e.getMessage());
        }
    }

    @Override
    public void handleWebhookEvent(Event event) {
        log.info("Traitement du webhook Stripe : Type = {}, ID = {}", event.getType(), event.getId());

        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;

            case "charge.refunded":
                handleChargeRefunded(event);
                break;

            default:
                log.info("Type d'événement non géré : {}", event.getType());
        }

        // Enregistrer l’event comme traité
        stripeEventLogRepository.save(
                new StripeEventLog(
                        null,
                        event.getId(),
                        event.getType(),
                        LocalDateTime.now()
                )
        );
    }


    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

        if (session == null) {
            log.error("Session null dans l'événement checkout.session.completed");
            return;
        }

        log.info("Session Checkout complétée : {}", session.getId());

        // Recuperer l'ID de reservation depuis les metadonnees
        String reservationIdStr = session.getMetadata().get("reservationId");
        if (reservationIdStr == null) {
            log.error("Aucun reservationId dans les métadonnées de la session");
            return;
        }

        Long  reservationId = Long.valueOf(reservationIdStr);

        // Mettre a jour le statut du paiement
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        // Vérifier montant côté serveur
        BigDecimal expectedAmount = reservation.getTotalPrice();
        BigDecimal paidAmount = BigDecimal
                .valueOf(session.getAmountTotal())
                .divide(BigDecimal.valueOf(100));

        if (paidAmount.compareTo(expectedAmount) < 0) {
            log.error("Montant incohérent pour reservation {}", reservationId);
            return;
        }

        Payment payment = paymentRepository.findByReservationId(reservationId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING &&
                        "STRIPE".equalsIgnoreCase(p.getPaymentMethod()))
                .findFirst()
                .orElse(null);

        if (payment != null && payment.getStatus() == PaymentStatus.COMPLETED) {
            log.info("Paiement déjà traité pour la session {}", session.getId());
            return;
        }

        if (payment != null) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(session.getPaymentIntent());
            paymentRepository.save(payment);
        } else {
            // Creation d'un nouveau paiement
            log.warn("Aucun paiement PENDING trouvé, création d'un nouveau paiement");

            // Récupérer le montant depuis la session
            Long amountInCents = session.getAmountTotal();
            BigDecimal amount = BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100));

            Payment newPayment = Payment.builder()
                    .reservation(reservation)
                    .amount(amount)
                    .paymentMethod("STRIPE")
                    .paymentDate(LocalDateTime.now())
                    .status(PaymentStatus.COMPLETED)
                    .transactionId(session.getPaymentIntent())
                    .build();
            paymentRepository.save(newPayment);
        }

        // Vérifier si le paiement total est complet
        BigDecimal totalPaid = paymentRepository.findByReservationId(reservationId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Si le montant total est payé, confirmer la réservation
        if (totalPaid.compareTo(reservation.getTotalPrice()) >= 0) {
            if (reservation.getStatus() == ReservationStatus.PENDING) {
                reservation.setStatus(ReservationStatus.CONFIRMED);
                reservationRepository.save(reservation);
                log.info("Réservation ID {} confirmée suite au paiement Stripe complet", reservationId);
            }
        }

        // ✅ ENVOYER L'EMAIL DE CONFIRMATION (seulement si le paiement est COMPLETED)
        try {
            emailService.sendPaymentConfirmation(reservation, session.getPaymentIntent());
            log.info("Email de confirmation envoyé pour la réservation ID {}", reservationId);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de confirmation : {}", e.getMessage(), e);
        }

        log.info("Paiement Stripe traité avec succès pour la réservation ID : {}", reservationId);
    }

    private void handleChargeRefunded(Event event) {
        Charge charge = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);

        if (charge == null) {
            System.out.println("Charge null dans l'événement charge.refunded");
            return;
        }

        System.out.println(String.format("Charge remboursée : {}", charge.getId()));

        String paymentIntentId = charge.getPaymentIntent();
        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .orElse(null);

        if (payment != null) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            System.out.println(String.format("Paiement mis à jour en REFUNDED : Transaction ID = {}", paymentIntentId));
        }
    }


    @Override
    public void cancelPayment(String paymentIntentId) {
        System.out.println(String.format("Annulation du PaymentIntent : {}", paymentIntentId));

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            PaymentIntentCancelParams params = PaymentIntentCancelParams.builder().build();
            paymentIntent.cancel(params);

            // Mettre a jour en base de donnee
            Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", paymentIntentId));

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            System.out.println(String.format("PaymentIntent annulé avec succès : {}", paymentIntentId));
        } catch (StripeException e) {
            System.out.println(String.format("Erreur lors de l'annulation du PaymentIntent : {}", e.getMessage(), e));
            throw new BusinessException("Impossible d'annuler le paiement : " + e.getMessage());
        }
    }

    @Override
    public void refundPayment(String paymentIntentId, BigDecimal amount) {
        System.out.println(String.format("Remboursement du PaymentIntent : {}", paymentIntentId));

        try {
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setAmount(amountInCents)
                    .build();

            Refund refund = Refund.create(params);

            // Mettre à jour en base
            Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", paymentIntentId));

            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

           System.out.println(String.format("Remboursement effectué avec succès : {}", refund.getId()));

        } catch (StripeException e) {
            System.out.println(String.format("Erreur lors du remboursement : {}", e.getMessage(), e));
            throw new BusinessException("Impossible de rembourser le paiement : " + e.getMessage());
        }
    }

    @Override
    public String getPaymentStatus(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return paymentIntent.getStatus();
        } catch (StripeException e) {
            System.out.println(String.format("Erreur lors de la récupération du statut : {}", e.getMessage(), e));
            throw new BusinessException("Impossible de récupérer le statut du paiement");
        }
    }
}
