package com.example.demo.entites;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CategoryResponseDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerDay;
    private LocalDateTime createdAt;
}
