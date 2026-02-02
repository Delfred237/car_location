package com.example.demo.services;

import com.example.demo.dto.request.ReservationRequestDTO;
import com.example.demo.dto.response.ReservationResponseDTO;
import com.example.demo.enums.ReservationStatus;

import java.util.List;

public interface ReservationService {

    ReservationResponseDTO create(ReservationRequestDTO requestDTO, Long userId);

    ReservationResponseDTO getById(Long id);

    List<ReservationResponseDTO> getAll();

    List<ReservationResponseDTO> getByUser(Long userId);

    List<ReservationResponseDTO> getByCar(Long carId);

    List<ReservationResponseDTO> getByStatus(ReservationStatus status);

    List<ReservationResponseDTO> getUpcomingReservationsByUser(Long userId);

    List<ReservationResponseDTO> getReservationHistoryByUser(Long userId);

    ReservationResponseDTO updateStatus(Long id, ReservationStatus newStatus);

    ReservationResponseDTO cancelReservation(Long id);

    void delete(Long id);
}
