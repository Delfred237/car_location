package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CategoryResponseDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerDay;
    private LocalDateTime createdDate;
}
