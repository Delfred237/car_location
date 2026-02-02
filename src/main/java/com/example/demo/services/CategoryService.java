package com.example.demo.services;

import com.example.demo.dto.request.CategoryRequestDTO;
import com.example.demo.dto.response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO create(CategoryRequestDTO categoryRequestDTO);

    List<CategoryResponseDTO> getAll();

    CategoryResponseDTO getById(Long id);

    CategoryResponseDTO getByName(String name);

    CategoryResponseDTO update(Long id, CategoryRequestDTO categoryRequestDTO);

    void delete(Long id);

    boolean existsByName(String name);
}
