package com.example.demo.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "agencies")
@Entity
public class Agency extends BaseEntity {

    private String name;
    private String city;
    private String address;
    private String phoneNumber;
}
