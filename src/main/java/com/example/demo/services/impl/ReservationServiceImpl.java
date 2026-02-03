package com.example.demo.services.impl;

import com.example.demo.Repository.CarRepository;
import com.example.demo.Repository.ReservationRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.request.ReservationRequestDTO;
import com.example.demo.dto.response.ReservationResponseDTO;
import com.example.demo.email.EmailService;
import com.example.demo.entites.Car;
import com.example.demo.entites.Category;
import com.example.demo.entites.Reservation;
import com.example.demo.entites.User;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.InvalidReservationException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mapper.ReservationMapper;
import com.example.demo.services.ReservationService;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final ReservationMapper reservationMapper;


    @Override
    public ReservationResponseDTO create(ReservationRequestDTO requestDTO, Long userId) {
        // Récupérer l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Récupérer la voiture
        Car car = carRepository.findById(requestDTO.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", requestDTO.getCarId()));

        // Valider les dates
        validateReservationDates(requestDTO.getStartDate(), requestDTO.getEndDate());

        // Vérifier la disponibilité de la voiture
        if (!isCarAvailable(requestDTO.getCarId(), requestDTO.getStartDate(), requestDTO.getEndDate())) {
            throw new InvalidReservationException(
                    "La voiture n'est pas disponible pour les dates sélectionnées"
            );
        }

        Reservation reservation = reservationMapper.toEntity(requestDTO);
        reservation.setUser(user);
        reservation.setCar(car);
        reservation.setStatus(ReservationStatus.PENDING);

        // Calculer le prix total
        BigDecimal totalPrice = calculateTotalPrice(car.getCategory(), requestDTO.getStartDate(),
                requestDTO.getEndDate());
        reservation.setTotalPrice(totalPrice);

        Reservation savedReservation = reservationRepository.save(reservation);

        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO getById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        return reservationMapper.toDTO(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getAll() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getByUser(Long userId) {
        // Vérifier que l'utilisateur existe
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        return reservationRepository.findByUserId(userId).stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getByCar(Long carId) {
        // Vérifier que la voiture existe
        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car", "id", carId);
        }

        return reservationRepository.findByCarId(carId).stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status).stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getUpcomingReservationsByUser(Long userId) {
        // Vérifier que l'utilisateur existe
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        LocalDate currentDate = LocalDate.now();
        return reservationRepository.findUpcomingReservationsByUser(userId, currentDate).stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReservationHistoryByUser(Long userId) {
        // Vérifier que l'utilisateur existe
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        LocalDate currentDate = LocalDate.now();
        return reservationRepository.findReservationHistoryByUser(userId, currentDate).stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReservationResponseDTO updateStatus(Long id, ReservationStatus newStatus) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        // Valider la transition de statut
        validateStatusTransition(reservation.getStatus(), newStatus);

        reservation.setStatus(newStatus);
        Reservation updatedReservation = reservationRepository.save(reservation);

        return reservationMapper.toDTO(updatedReservation);
    }

    @Override
    @Transactional
    public ReservationResponseDTO cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        // Vérifier qu'on peut annuler
        if (reservation.getStatus() == ReservationStatus.COMPLETED
                || reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException("Cette réservation ne peut pas être annulée");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation cancelledReservation = reservationRepository.save(reservation);

        return reservationMapper.toDTO(cancelledReservation);
    }

    @Override
    public void delete(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        reservationRepository.delete(reservation);
    }


    // ========== MÉTHODES PRIVÉES ==========

    private void validateReservationDates(@NotNull(message = "La date de début est obligatoire") @FutureOrPresent(message = "La date de début ne peut pas être dans le passé") LocalDate startDate, @NotNull(message = "La date de fin est obligatoire") @Future(message = "La date de fin doit être dans le futur") LocalDate endDate) {
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(today)) {
            throw new InvalidReservationException("La date de début ne peut pas être dans le passé");
        }

        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new InvalidReservationException("La date de fin doit être après la date de début");
        }

        // Limite de réservation (ex: max 90 jours)
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 90) {
            throw new InvalidReservationException("La durée de location ne peut pas dépasser 90 jours");
        }
    }

    private boolean isCarAvailable(@NotNull(message = "La voiture est obligatoire") Long carId, @NotNull(message = "La date de début est obligatoire") @FutureOrPresent(message = "La date de début ne peut pas être dans le passé") LocalDate startDate, @NotNull(message = "La date de fin est obligatoire") @Future(message = "La date de fin doit être dans le futur") LocalDate endDate) {
        List<ReservationStatus> activeStatuses = Arrays.asList(
                ReservationStatus.CONFIRMED,
                ReservationStatus.ONGOING
        );

        return !reservationRepository.existsOverlappingReservation(
                carId, startDate, endDate, activeStatuses
        );
    }

    private BigDecimal calculateTotalPrice(Category category, LocalDate startDate, LocalDate endDate) {
        long numberOfDays = ChronoUnit.DAYS.between(startDate, endDate);

        if (numberOfDays <= 0) {
            numberOfDays = 1;   // Au moins 1 jour
        }

        return category.getPricePerDay().multiply(BigDecimal.valueOf(numberOfDays));
    }

    private void validateStatusTransition(ReservationStatus currentStatus, ReservationStatus newStatus) {
        // Définir les transitions valides
        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == ReservationStatus.CONFIRMED
                    || newStatus == ReservationStatus.CANCELLED;
            case CONFIRMED -> newStatus == ReservationStatus.ONGOING
                    || newStatus == ReservationStatus.CANCELLED;
            case ONGOING -> newStatus == ReservationStatus.COMPLETED
                    || newStatus == ReservationStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false; // États finaux
        };

        if (!isValidTransition) {
            throw new BusinessException(
                    String.format("Transition de statut invalide : %s -> %s", currentStatus, newStatus)
            );
        }
    }
}
