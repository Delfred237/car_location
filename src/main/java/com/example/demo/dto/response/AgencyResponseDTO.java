package com.example.demo.dto.response;

import com.example.demo.dto.request.AgencyRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
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
