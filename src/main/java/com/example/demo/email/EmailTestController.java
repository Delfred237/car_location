package com.example.demo.email;

import com.example.demo.Repository.ReservationRepository;
import com.example.demo.entites.Reservation;
import com.example.demo.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;
    private final ReservationRepository reservationRepository;

    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello World");
    }

    /**
     * Test email texte simple
     */
    @GetMapping("/simple")
    public ResponseEntity<String> sendSimpleEmail(
            @RequestParam String to
    ) {
        log.info("sendSimpleEmail");
        emailService.sendSimpleEmail(
                to,
                "Test email simple",
                "Ceci est un email de test (texte simple)."
        );
        log.info("sendSimpleEmail succes");
        return ResponseEntity.ok("Email simple envoyé");
    }

    /**
     * Test email HTML générique (template simple)
     */
    @GetMapping("/html")
    public ResponseEntity<String> sendHtmlEmail(
            @RequestParam String to
    ) {
        Map<String, Object> model = new HashMap<>();
        model.put("userName", "Del Sensei");
        model.put("message", "Ceci est un email HTML de test.");

        EmailDetailsDTO emailDetails = EmailDetailsDTO.builder()
                .recipient(to)
                .subject("Test email HTML")
                .templateName("welcome") // templates/test-email.html
                .templateModel(model)
                .build();

        emailService.sendHtmlEmail(emailDetails);

        return ResponseEntity.ok("Email HTML envoyé");
    }

    /**
     * Test confirmation de réservation
     */
    @GetMapping("/reservation/{id}/confirmation")
    public ResponseEntity<String> sendReservationConfirmation(
            @PathVariable Long id
    ) {
        Reservation reservation = getReservation(id);
        emailService.sendReservationConfirmation(reservation);
        return ResponseEntity.ok("Email de confirmation envoyé");
    }

    /**
     * Test annulation de réservation
     */
    @GetMapping("/reservation/{id}/cancellation")
    public ResponseEntity<String> sendReservationCancellation(
            @PathVariable Long id
    ) {
        Reservation reservation = getReservation(id);
        emailService.sendReservationCancellation(reservation);
        return ResponseEntity.ok("Email d'annulation envoyé");
    }

    /**
     * Test rappel de réservation
     */
    @GetMapping("/reservation/{id}/reminder")
    public ResponseEntity<String> sendReservationReminder(
            @PathVariable Long id
    ) {
        Reservation reservation = getReservation(id);
        emailService.sendReservationReminder(reservation);
        return ResponseEntity.ok("Email de rappel envoyé");
    }

    /**
     * Test confirmation de paiement
     */
    @GetMapping("/reservation/{id}/payment")
    public ResponseEntity<String> sendPaymentConfirmation(
            @PathVariable Long id,
            @RequestParam String transactionId
    ) {
        Reservation reservation = getReservation(id);
        emailService.sendPaymentConfirmation(reservation, transactionId);
        return ResponseEntity.ok("Email de confirmation de paiement envoyé");
    }

    /**
     * Méthode utilitaire
     */
    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Réservation introuvable"));
    }
}
