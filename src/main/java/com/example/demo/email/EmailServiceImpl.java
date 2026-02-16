package com.example.demo.email;

import com.example.demo.Repository.PaymentRepository;
import com.example.demo.entites.Payment;
import com.example.demo.entites.Reservation;
import com.example.demo.entites.User;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.exceptions.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map; 

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final PaymentRepository paymentRepository;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.fromName}")
    private String fromName;

    @Value("${app.base.url}")
    private String baseUrl;


    @Override
    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
        } catch (Exception e){
            throw new BusinessException("Impossible d'envoyer l' email "+ e.getMessage());
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(EmailDetailsDTO emailDetailsDTO) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Configuration de l'email
            helper.setFrom(fromEmail, fromName);
            helper.setTo(emailDetailsDTO.getRecipient());
            helper.setSubject(emailDetailsDTO.getSubject());

            // Generer le contenu HTML depuis le template
            Context context = new Context();
            if (emailDetailsDTO.getTemplateModel() != null) {
                context.setVariables(emailDetailsDTO.getTemplateModel());
            }

            String htmlContent = templateEngine.process(emailDetailsDTO.getTemplateName(), context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Impossible d'envoyer l'email HTML");
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de l'envoi de l'email");
        }
    }

    @Override
    public void sendReservationConfirmation(Reservation reservation) {
        Map<String, Object> model = new HashMap<>();
        model.put("userName", reservation.getUser().getFullName());
        model.put("reservationId", reservation.getId());
        model.put("carBrand", reservation.getCar().getBrand());
        model.put("carModel", reservation.getCar().getModel());
        model.put("startDate", formatDate(reservation.getStartDate()));
        model.put("endDate", formatDate(reservation.getEndDate()));
        model.put("totalPrice", reservation.getTotalPrice());
        model.put("status", reservation.getStatus().name());

        EmailDetailsDTO emailDetailsDTO = EmailDetailsDTO.builder()
                .recipient(reservation.getUser().getEmail())
                .subject("Confirmation de réservation #" + reservation.getId())
                .templateName("/reservation-confirmation")
                .templateModel(model)
                .build();

        sendHtmlEmail(emailDetailsDTO);
    }

    @Override
    public void sendReservationCancellation(Reservation reservation) {
        Map<String, Object> model = new HashMap<>();
        model.put("userName", reservation.getUser().getFullName());
        model.put("reservationId", reservation.getId());
        model.put("carBrand", reservation.getCar().getBrand());
        model.put("carModel", reservation.getCar().getModel());
        model.put("startDate", formatDate(reservation.getStartDate()));
        model.put("endDate", formatDate(reservation.getEndDate()));

        EmailDetailsDTO emailDetails = EmailDetailsDTO.builder()
                .recipient(reservation.getUser().getEmail())
                .subject("Annulation de votre réservation #" + reservation.getId())
                .templateName("/reservation-cancellation")
                .templateModel(model)
                .build();

        sendHtmlEmail(emailDetails);
    }

    @Override
    public void sendReservationReminder(Reservation reservation) {
        Map<String, Object> model = new HashMap<>();
        model.put("userName", reservation.getUser().getFullName());
        model.put("reservationId", reservation.getId());
        model.put("carBrand", reservation.getCar().getBrand());
        model.put("carModel", reservation.getCar().getModel());
        model.put("startDate", formatDate(reservation.getStartDate()));
        model.put("agencyName", reservation.getCar().getAgency() != null
                ? reservation.getCar().getAgency().getName() : "N/A");
        model.put("agencyAddress", reservation.getCar().getAgency() != null
                ? reservation.getCar().getAgency().getAddress() : "N/A");

        EmailDetailsDTO emailDetails = EmailDetailsDTO.builder()
                .recipient(reservation.getUser().getEmail())
                .subject("Rappel : Votre location commence bientôt !")
                .templateName("/reservation-reminder")
                .templateModel(model)
                .build();

        sendHtmlEmail(emailDetails);
    }

    @Override
    public void sendPaymentConfirmation(Reservation reservation, String transactionId) {
        // Récupérer le dernier paiement complété
        Payment lastPayment = paymentRepository.findByReservationId(reservation.getId())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .max(Comparator.comparing(Payment::getPaymentDate))
                .orElse(null);

        Map<String, Object> model = new HashMap<>();
        model.put("userName", reservation.getUser().getFullName());
        model.put("reservationId", reservation.getId());
        model.put("transactionId", transactionId);
        model.put("amount", reservation.getTotalPrice());
        model.put("carBrand", reservation.getCar().getBrand());
        model.put("carModel", reservation.getCar().getModel());
        model.put("paymentMethod", lastPayment != null ? lastPayment.getPaymentMethod() : "Stripe");
        model.put("paymentDate", lastPayment != null ? lastPayment.getPaymentDate() : LocalDateTime.now());

        EmailDetailsDTO emailDetails = EmailDetailsDTO.builder()
                .recipient(reservation.getUser().getEmail())
                .subject("Confirmation de paiement - Réservation #" + reservation.getId())
                .templateName("/payment-confirmation")
                .templateModel(model)
                .build();

        sendHtmlEmail(emailDetails);
    }

    @Override
    public void sendAccountVerification(User user, String verificationToken) {
        log.info("Envoi de l'email de vérification de compte à : {}", user.getEmail());

        String verificationUrl = baseUrl + "/api/auth/verify?token=" + verificationToken;

        Map<String, Object> model = new HashMap<>();
        model.put("userName", user.getFullName());
        model.put("verificationUrl", verificationUrl);
        model.put("expirationTime", "24 heures");

        EmailDetailsDTO emailDetails = EmailDetailsDTO.builder()
                .recipient(user.getEmail())
                .subject("Vérification de votre compte Car Rental")
                .templateName("/account-verification")
                .templateModel(model)
                .build();

        sendHtmlEmail(emailDetails);
    }

    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        log.info("Envoi de l'email de réinitialisation de mot de passe à : {}", user.getEmail());

        String resetUrl = baseUrl + "/api/auth/reset-password?token=" + resetToken;

        Map<String, Object> model = new HashMap<>();
        model.put("userName", user.getFullName());
        model.put("resetUrl", resetUrl);
        model.put("expirationTime", "1 heure");

        EmailDetailsDTO emailDetails = EmailDetailsDTO.builder()
                .recipient(user.getEmail())
                .subject("Réinitialisation de votre mot de passe")
                .templateName("/password-reset")
                .templateModel(model)
                .build();

        sendHtmlEmail(emailDetails);
    }

    @Override
    public void sendWelcomeEmail(User user) {
        log.info("Envoi de l'email de bienvenue à : {}", user.getEmail());

        Map<String, Object> model = new HashMap<>();
        model.put("userName", user.getFullName());
        model.put("loginUrl", baseUrl + "/login");

        EmailDetailsDTO emailDetails = EmailDetailsDTO.builder()
                .recipient(user.getEmail())
                .subject("Bienvenue chez Car Rental !")
                .templateName("/welcome")
                .templateModel(model)
                .build();

        sendHtmlEmail(emailDetails);
    }

    private String formatDate(java.time.LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

}
