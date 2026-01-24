package com.example.demo.entites;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @JsonIgnoreProperties("cars")
    @OneToMany(mappedBy = "agency", cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Car> cars = new ArrayList<>();
}
