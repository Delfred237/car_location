package com.example.demo.mapper;

import com.example.demo.dto.request.AgencyRequestDTO;
import com.example.demo.dto.response.AgencyResponseDTO;
import com.example.demo.entites.Agency;
import org.mapstruct.*;

@Mapper(config = MapperConfig.class)
public interface AgencyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    Agency toEntity(AgencyRequestDTO dto);

    AgencyResponseDTO toDto(Agency agency);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Agency updateEnity(@MappingTarget Agency agency, AgencyRequestDTO dto);
}
