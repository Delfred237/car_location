package com.example.demo.dto.response;

import com.example.demo.enums.PieceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String piece;
    private PieceType pieceType;
    private String pieceNumber;
    private Set<String> roles; // Noms des rôles uniquement
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
