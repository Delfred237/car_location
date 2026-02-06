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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO categoryRequestDTO) {
        log.info("Création d'une nouvelle catégorie : {}", categoryRequestDTO.getName());

        // Verifier si une category existe deja
        if (categoryRepository.existsByName(categoryRequestDTO.getName())) {
            throw new ResourceAlreadyExistsException("Category", "name",  categoryRequestDTO.getName());
        }

        Category category = categoryMapper.toEntity(categoryRequestDTO);
        Category savedCategory = categoryRepository.save(category);

        log.info("Catégorie créée avec succès : ID {}", savedCategory.getId());
        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    public List<CategoryResponseDTO> getAll() {
        log.info("Récupération de toutes les catégories");

        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDTO getById(Long id) {
        log.info("Recherche de la catégorie avec ID : {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("category", "id", id));
        return categoryMapper.toDTO(category);
    }

    @Override
    public CategoryResponseDTO getByName(String name) {
        log.info("Recherche de la catégorie : {}", name);

        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("category", "name", name));
        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO categoryRequestDTO) {
        log.info("Mise à jour de la catégorie ID : {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("category", "id", id));

        // Verifier si le nouveau nom existe deja
        if (!category.getName().equals(categoryRequestDTO.getName())
                && categoryRepository.existsByName(categoryRequestDTO.getName())) {
            throw new ResourceAlreadyExistsException("Category", "name", categoryRequestDTO.getName());
        }

        Category updatedCategory = categoryMapper.updateEntity(category, categoryRequestDTO);
        Category  savedCategory = categoryRepository.save(updatedCategory);

        log.info("Catégorie mise à jour avec succès : ID {}", updatedCategory.getId());
        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Suppression de la catégorie ID : {}", id);

        Category category  = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("category", "id", id));

        categoryRepository.delete(category);
        log.info("Catégorie supprimée avec succès : ID {}", id);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
