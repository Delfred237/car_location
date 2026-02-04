package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AgencyResponseDTO {

    private Long id;
    private String name;
    private String city;
    private String address;
    private String phoneNumber;
    private LocalDateTime createdDate;
}
