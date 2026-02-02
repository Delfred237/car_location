package com.example.demo.services;

import com.example.demo.dto.request.PaymentRequestDTO;
import com.example.demo.dto.response.PaymentResponseDTO;
import com.example.demo.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    PaymentResponseDTO create(PaymentRequestDTO requestDTO);

    PaymentResponseDTO getById(Long id);

    List<PaymentResponseDTO> getAll();

    List<PaymentResponseDTO> getByReservation(Long reservationId);

    List<PaymentResponseDTO> getByStatus(PaymentStatus status);

    List<PaymentResponseDTO> getPaymentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    PaymentResponseDTO updateStatus(Long id, PaymentStatus newStatus);

    PaymentResponseDTO processPayment(Long id);

    Double getTotalPaidAmount(Long reservationId);

    boolean hasCompletedPayment(Long reservationId);

    void delete(Long id);
}
