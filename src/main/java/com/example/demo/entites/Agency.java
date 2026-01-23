package com.example.demo.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "agencies")
@Entity
public class Agency extends BaseEntity {

    @NotBlank(message = "Le nom de l'agence est obligatoire")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "La ville est obligatoire")
    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 255)
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
}
