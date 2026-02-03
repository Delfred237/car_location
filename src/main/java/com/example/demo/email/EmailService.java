package com.example.demo.email;

import com.example.demo.entites.Reservation;

public interface EmailService {

    void sendSimpleEmail(String to, String subject, String body);

    void sendHtmlEmail(EmailDetailsDTO emailDetailsDTO);

    // Emails lies aux reservations
    void sendReservationConfirmation(Reservation reservation);

    void sendReservationCancellation(Reservation reservation);

    void sendReservationReminder(Reservation reservation);

    void sendPaymentConfirmation(Reservation reservation, String transactionId);
}
