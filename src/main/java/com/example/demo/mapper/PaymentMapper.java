package com.example.demo.mapper;

import com.example.demo.dto.request.PaymentRequestDTO;
import com.example.demo.dto.response.PaymentResponseDTO;
import com.example.demo.entites.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {

    /**
     * Convertit PaymentRequestDTO → Payment (Entity)
     * Reservation sera définie dans le service via reservationId
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    Payment toEntity(PaymentRequestDTO dto);

    /**
     * Convertit Payment (Entity) → PaymentResponseDTO
     */
    @Mapping(target = "reservationId", source = "reservation.id")
    PaymentResponseDTO toDTO(Payment payment);
}
