package com.example.demo.Repository;

import com.example.demo.entites.Car;
import com.example.demo.entites.Reservation;
import com.example.demo.entites.User;
import com.example.demo.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Recherche toutes les réservations d'un utilisateur
     * @param user l'utilisateur
     * @return liste des réservations de l'utilisateur
     */
    List<Reservation> findByUser(User user);

    /**
     * Recherche toutes les réservations d'un utilisateur par ID
     * @param userId l'ID de l'utilisateur
     * @return liste des réservations de l'utilisateur
     */
    List<Reservation> findByUserId(Long userId);

    /**
     * Recherche toutes les réservations d'une voiture
     * @param car la voiture
     * @return liste des réservations de la voiture
     */
    List<Reservation> findByCar(Car car);

    /**
     * Recherche toutes les réservations d'une voiture par ID
     * @param carId l'ID de la voiture
     * @return liste des réservations de la voiture
     */
    List<Reservation> findByCarId(Long carId);

    /**
     * Recherche toutes les réservations par statut
     * @param status le statut de la réservation
     * @return liste des réservations avec ce statut
     */
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Recherche les réservations d'un utilisateur par statut
     * @param userId l'ID de l'utilisateur
     * @param status le statut de la réservation
     * @return liste des réservations correspondantes
     */
    List<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    /**
     * Vérifie si une voiture a des réservations qui chevauchent une période donnée
     * @param carId l'ID de la voiture
     * @param startDate date de début
     * @param endDate date de fin
     * @param statuses les statuts de réservation à considérer
     * @return true si un chevauchement existe
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Reservation r WHERE r.car.id = :carId AND " +
            "r.status IN :statuses AND " +
            "(r.startDate <= :endDate AND r.endDate >= :startDate)")
    boolean existsOverlappingReservation(@Param("carId") Long carId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         @Param("statuses") List<ReservationStatus> statuses);

    /**
     * Recherche les réservations en cours (ongoing)
     * @param currentDate la date actuelle
     * @return liste des réservations en cours
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'ONGOING' AND " +
            "r.startDate <= :currentDate AND r.endDate >= :currentDate")
    List<Reservation> findOngoingReservations(@Param("currentDate") LocalDate currentDate);

    /**
     * Recherche les réservations à venir pour un utilisateur
     * @param userId l'ID de l'utilisateur
     * @param currentDate la date actuelle
     * @return liste des réservations futures
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND " +
            "r.status IN ('CONFIRMED', 'PENDING') AND r.startDate > :currentDate " +
            "ORDER BY r.startDate ASC")
    List<Reservation> findUpcomingReservationsByUser(@Param("userId") Long userId,
                                                     @Param("currentDate") LocalDate currentDate);

    /**
     * Recherche l'historique des réservations d'un utilisateur
     * @param userId l'ID de l'utilisateur
     * @param currentDate la date actuelle
     * @return liste des réservations passées
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND " +
            "(r.status = 'COMPLETED' OR (r.status = 'CANCELLED' AND r.endDate < :currentDate)) " +
            "ORDER BY r.endDate DESC")
    List<Reservation> findReservationHistoryByUser(@Param("userId") Long userId,
                                                   @Param("currentDate") LocalDate currentDate);
}
