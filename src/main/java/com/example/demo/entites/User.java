package com.example.demo.entites;

import com.example.demo.enums.PieceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "users")
@Entity
public class User extends BaseEntity {

    @NotBlank(message = "Le nom complet est obligatoire")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(nullable = false)
    private String password;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @NotBlank(message = "Le document d'identité est obligatoire")
    @Column(nullable = false)
    private String piece;

    @Column(name = "piece_type", length = 20)
    private PieceType pieceType;

    @Column(name = "piece_number", nullable = false)
    private String pieceNumber; // Le numéro du document

    @Column(name = "piece_file_path")
    private String pieceFilePath;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Reservation> reservations = new ArrayList<>();
}
