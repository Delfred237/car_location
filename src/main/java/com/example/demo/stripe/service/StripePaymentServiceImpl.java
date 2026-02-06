package com.example.demo.stripe.service;

import com.example.demo.Repository.PaymentRepository;
import com.example.demo.Repository.ReservationRepository;
import com.example.demo.email.EmailService;
import com.example.demo.entites.Payment;
import com.example.demo.entites.Reservation;
import com.example.demo.enums.PaymentStatus;
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
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Transactional
@RequiredArgsConstructor
@Service
public class StripePaymentServiceImpl implements StripePaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;


    @Override
    public StripePaymentResponseDTO createCheckoutSession(StripePaymentRequestDTO requestDTO) {
        System.out.println(String.format("Création d'une session Stripe Checkout pour la réservation ID : {}", requestDTO.getReservationId()));

        try {
            // Recuperer la reservation
            Reservation reservation = reservationRepository.findById(requestDTO.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", requestDTO.getReservationId()));

            // Convertir le montant en centimes (Stripe utilise les plus petites unites)
            long amountInCents = requestDTO.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

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

            System.out.println("Session Stripe créée avec succès : {}" + session.getId());

            return StripePaymentResponseDTO.builder()
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .paymentIntentId(session.getPaymentIntent())
                    .status("pending")
                    .reservationId(reservation.getId())
                    .build();
        } catch (StripeException e) {
            System.out.println(String.format("Erreur Stripe lors de la création de la session : {}", e.getMessage(), e));
            throw new BusinessException("Impossible de créer la session de paiement Stripe : " + e.getMessage());
        }
    }

    @Override
    public StripePaymentResponseDTO createPaymentIntent(StripePaymentRequestDTO requestDTO) {
        System.out.println(String.format("Création d'un PaymentIntent pour la réservation ID : {}", requestDTO.getReservationId()));

        try {
            // Recuperer la reservation
            Reservation reservation = reservationRepository.findById(requestDTO.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", requestDTO.getReservationId()));

            long amountInCents = requestDTO.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

            // Metadonnees pour retrouver la reservation dans le webhook
            Map<String, String> metadata = new HashMap<>();
            metadata.put("reservationId", String.valueOf(reservation.getId()));
            metadata.put("userId", String.valueOf(reservation.getUser().getId()));

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(requestDTO.getCurrency().toLowerCase())
                    .setDescription(requestDTO.getDescription())
                    .putAllMetadata(metadata)
                    .setReceiptEmail(reservation.getUser().getEmail())
                    .build();
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Creer l'enregistrement du paiement
            Payment payment = Payment.builder()
                    .reservation(reservation)
                    .amount(requestDTO.getAmount())
                    .paymentMethod("STRIPE")
                    .paymentDate(LocalDateTime.now())
                    .status(PaymentStatus.PENDING)
                    .transactionId(paymentIntent.getId())
                    .build();
            paymentRepository.save(payment);

            System.out.println(String.format("PaymentIntent créé avec succès : {}", paymentIntent.getId()));

            return StripePaymentResponseDTO.builder()
                    .paymentIntentId(paymentIntent.getId())
                    .status(paymentIntent.getStatus())
                    .reservationId(reservation.getId())
                    .build();
        } catch (StripeException e) {
            System.out.println(String.format("Erreur Stripe lors de la création du PaymentIntent : {}", e.getMessage(), e));
            throw new BusinessException("Impossible de créer le PaymentIntent : " + e.getMessage());
        }
    }

    @Override
    public void handleWebhookEvent(Event event) {
        System.out.println(String.format("Traitement du webhook Stripe : Type = {}, ID = {}", event.getType(), event.getId()));

        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;

            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;

            case "charge.refunded":
                handleChargeRefunded(event);
                break;

            default:
                System.out.println(String.format("Type d'événement non géré : {}", event.getType()));
        }
    }


    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

        if (session == null) {
            System.out.println("Session null dans l'événement checkout.session.completed");
            return;
        }

        System.out.println(String.format("Session Checkout complétée : {}", session.getId()));

        // Recuperer l'ID de reservation depuis les metadonnees
        String reservationIdStr = session.getMetadata().get("reservationId");
        if (reservationIdStr == null) {
            System.out.println("Aucun reservationId dans les métadonnées de la session");
            return;
        }

        Long  reservationId = Long.valueOf(reservationIdStr);

        // Mettre a jour le statut du paiement
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        Payment payment = paymentRepository.findByReservationId(reservationId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .findFirst()
                .orElse(null);

        if (payment != null) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(session.getPaymentIntent());
            paymentRepository.save(payment);

            // Envoyer l'email de confirmation
            emailService.sendPaymentConfirmation(reservation, session.getPaymentIntent());

            System.out.println(String.format("Paiement marqué comme COMPLETED pour la réservation ID : {}", reservationId));
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

        if (paymentIntent == null) {
            System.out.println("PaymentIntent null dans l'événement payment_intent.succeeded");
            return;
        }

        System.out.println(String.format("PaymentIntent réussi : {}", paymentIntent.getId()));

        // Mettre a jour le statut du paiement
        Payment payment = paymentRepository.findByTransactionId(paymentIntent.getId())
                .orElse(null);

        if (payment != null && payment.getStatus().equals(PaymentStatus.PENDING)) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(paymentIntent.getId());
            paymentRepository.save(payment);

            System.out.println(String.format("Paiement mis à jour en COMPLETED : Transaction ID = {}", paymentIntent.getId()));
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

        if (paymentIntent == null) {
            System.out.println("PaymentIntent null dans l'événement payment_intent.succeeded");
            return;
        }

        System.out.println(String.format("PaymentIntent réussi : {}", paymentIntent.getId()));

        Payment payment = paymentRepository.findByTransactionId(paymentIntent.getId())
                .orElse(null);

        if (payment != null) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setTransactionId(paymentIntent.getId());
            paymentRepository.save(payment);

            System.out.println(String.format("Paiement mis à jour en FAILED : Transaction ID = {}", paymentIntent.getId()));
        }
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
