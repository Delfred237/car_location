package com.example.demo.entites;

import com.example.demo.enums.PieceType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "users")
@Entity
public class User extends BaseEntity {

    private String fullName;
    private String email;
    private String password;
    private String phoneNumber;
    private String piece;
    private PieceType pieceType;
    private String pieceFilePath;
    private Set<Role> roles = new HashSet<>();
}
