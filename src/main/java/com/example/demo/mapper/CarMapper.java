package com.example.demo.mapper;

import com.example.demo.dto.request.CarRequestDTO;
import com.example.demo.dto.response.CarResponseDTO;
import com.example.demo.entites.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {

    /**
     * Convertit CarRequestDTO → Car (Entity)
     * Category et Agency seront définis dans le service via categoryId et agencyId
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "agency", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    Car toEntity(CarRequestDTO dto);

    /**
     * Convertit Car (Entity) → CarResponseDTO
     * On mappe les infos de Category et Agency
     */
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "agencyId", source = "agency.id")
    @Mapping(target = "agencyName", source = "agency.name")
    CarResponseDTO toDTO(Car car);

    /**
     * Met à jour une entité Car existante
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "agency", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    void updateEntity(@MappingTarget Car car, CarRequestDTO dto);
}
