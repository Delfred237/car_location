package com.example.demo.dto.response;

import com.example.demo.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentResponseDTO {

    private Long id;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionId;
    private Long reservationId;
    private LocalDateTime createdAt;
}
