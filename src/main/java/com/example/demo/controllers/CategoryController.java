package com.example.demo.controllers;

import com.example.demo.dto.request.CategoryRequestDTO;
import com.example.demo.dto.response.CategoryResponseDTO;
import com.example.demo.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/categories")
@RequiredArgsConstructor
@RestController
public class CategoryController {

    private final CategoryService categoryService;


    /**
     * Créer une nouvelle catégorie
     * POST /api/categories
     */
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
       return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(requestDTO));
    }

    /**
     * Récupérer une catégorie par ID
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.getById(id));
    }

    /**
     * Récupérer toutes les catégories
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.getAll());
    }

    /**
     * Rechercher une catégorie par nom
     * GET /api/categories/search?name=SUV
     */
    @GetMapping("/search")
    public ResponseEntity<CategoryResponseDTO> getCategoryByName(@RequestParam String name) {
       return ResponseEntity.status(HttpStatus.OK).body(categoryService.getByName(name));
    }

    /**
     * Mettre à jour une agence
     * PUT /api/agencies/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateAgency(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.update(id, requestDTO));
    }

    /**
     * Supprimer une catégorie
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
