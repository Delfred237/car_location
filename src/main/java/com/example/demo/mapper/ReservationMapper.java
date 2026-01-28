package com.example.demo.mapper;

import com.example.demo.dto.request.ReservationRequestDTO;
import com.example.demo.dto.response.ReservationResponseDTO;
import com.example.demo.entites.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface ReservationMapper {

    /**
     * Convertit ReservationRequestDTO → Reservation (Entity)
     * User, Car et TotalPrice seront définis dans le service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    Reservation toEntity(ReservationRequestDTO dto);

    /**
     * Convertit Reservation (Entity) → ReservationResponseDTO
     * On mappe les infos de User et Car
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "id", source = "car.id")
    @Mapping(target = "carBrand", source = "car.brand")
    @Mapping(target = "carModel", source = "car.model")
    @Mapping(target = "carLicensePlate", source = "car.licensePlate")
    ReservationResponseDTO toDTO(Reservation reservation);
}
