package com.example.demo.services.impl;

import com.example.demo.Repository.CategoryRepository;
import com.example.demo.dto.request.CategoryRequestDTO;
import com.example.demo.dto.response.CategoryResponseDTO;
import com.example.demo.entites.Category;
import com.example.demo.exceptions.ResourceAlreadyExistsException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO categoryRequestDTO) {
        // Verifier si une category existe deja
        if (categoryRepository.existsByName(categoryRequestDTO.getName())) {
            throw new ResourceAlreadyExistsException("Category", "name",  categoryRequestDTO.getName());
        }

        Category category = categoryMapper.toEntity(categoryRequestDTO);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toDTO(savedCategory);

    }

    @Override
    public List<CategoryResponseDTO> getAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDTO getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("category", "id", id));
        return categoryMapper.toDTO(category);
    }

    @Override
    public CategoryResponseDTO getByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("category", "name", name));
        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO categoryRequestDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("category", "id", id));

        // Verifier si le nouveau nom existe deja
        if (!category.getName().equals(categoryRequestDTO.getName())
                && categoryRepository.existsByName(categoryRequestDTO.getName())) {
            throw new ResourceAlreadyExistsException("Category", "name", categoryRequestDTO.getName());
        }

        Category categoryUpdated = categoryMapper.updateEntity(category, categoryRequestDTO);
        Category  savedCategory = categoryRepository.save(categoryUpdated);

        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category  = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("category", "id", id));

        categoryRepository.delete(category);
    }

    @Override
    public boolean existsByName(String name) {
        return false;
    }
}
