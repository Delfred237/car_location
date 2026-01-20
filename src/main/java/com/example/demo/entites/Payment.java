package com.example.demo.entites;

import com.example.demo.enums.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "payments")
@Entity
public class Payment extends BaseEntity {

    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    private String paymentMethod; // CARD, CASH, TRANSFER
    private String transactionId;

}
