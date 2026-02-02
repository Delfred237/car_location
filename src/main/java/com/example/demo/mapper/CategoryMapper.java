package com.example.demo.mapper;

import com.example.demo.dto.request.CategoryRequestDTO;
import com.example.demo.entites.Category;
import com.example.demo.dto.response.CategoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    Category toEntity(CategoryRequestDTO dto);

    CategoryResponseDTO toDTO(Category category);

    Category updateEntity(@MappingTarget Category category, CategoryRequestDTO dto);
}
