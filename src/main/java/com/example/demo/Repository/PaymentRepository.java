package com.example.demo.Repository;

import com.example.demo.entites.Payment;
import com.example.demo.entites.Reservation;
import com.example.demo.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Recherche tous les paiements d'une réservation
     * @param reservation la réservation
     * @return liste des paiements de la réservation
     */
    List<Payment> findByReservation(Reservation reservation);

    /**
     * Recherche tous les paiements d'une réservation par ID
     * @param reservationId l'ID de la réservation
     * @return liste des paiements de la réservation
     */
    List<Payment> findByReservationId(Long reservationId);

    /**
     * Recherche un paiement par son ID de transaction
     * @param transactionId l'ID de transaction
     * @return Optional contenant le paiement s'il existe
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Recherche tous les paiements par statut
     * @param status le statut du paiement
     * @return liste des paiements avec ce statut
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Recherche tous les paiements d'une réservation par statut
     * @param reservationId l'ID de la réservation
     * @param status le statut du paiement
     * @return liste des paiements correspondants
     */
    List<Payment> findByReservationIdAndStatus(Long reservationId, PaymentStatus status);

    /**
     * Recherche tous les paiements dans une période donnée
     * @param startDate date de début
     * @param endDate date de fin
     * @return liste des paiements dans cette période
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Vérifie si une réservation a au moins un paiement complété
     * @param reservationId l'ID de la réservation
     * @return true si un paiement complété existe
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Payment p WHERE p.reservation.id = :reservationId AND p.status = 'COMPLETED'")
    boolean hasCompletedPayment(@Param("reservationId") Long reservationId);

    /**
     * Calcule le montant total payé pour une réservation
     * @param reservationId l'ID de la réservation
     * @return le montant total des paiements complétés
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.reservation.id = :reservationId AND p.status = 'COMPLETED'")
    Double calculateTotalPaidAmount(@Param("reservationId") Long reservationId);

}
