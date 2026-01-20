package com.example.demo.entites;

import com.example.demo.enums.ReservationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "reservations")
@Entity
public class Reservation extends BaseEntity{

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private ReservationStatus status;
    private User user;
    private Car car;
}
