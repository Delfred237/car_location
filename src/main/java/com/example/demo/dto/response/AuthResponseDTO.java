package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponseDTO {

    private Long userId;
    private String email;
    private String fullName;
    private Set<String> roles;
    private String message;
    private boolean accountVerified;
}
