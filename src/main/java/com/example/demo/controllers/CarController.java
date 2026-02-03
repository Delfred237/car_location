package com.example.demo.controllers;

import com.example.demo.dto.request.CarRequestDTO;
import com.example.demo.dto.response.CarResponseDTO;
import com.example.demo.services.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/cars")
@RequiredArgsConstructor
@RestController
public class CarController {

    private final CarService carService;


    /**
     * Créer une nouvelle voiture
     * POST /api/cars
     */
    @PostMapping
    public ResponseEntity<CarResponseDTO> createCar(@Valid @RequestBody CarRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.create(requestDTO));
    }

    /**
     * Récupérer une voiture par ID
     * GET /api/cars/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CarResponseDTO> getCarById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getById(id));
    }

    /**
     * Récupérer toutes les voitures
     * GET /api/cars
     */
    @GetMapping
    public ResponseEntity<List<CarResponseDTO>> getAllCars() {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getAll());
    }

    /**
     * Rechercher des voitures par catégorie
     * GET /api/cars/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CarResponseDTO>> getCarsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getByCategory(categoryId));
    }

    /**
     * Rechercher des voitures par agence
     * GET /api/cars/agency/{agencyId}
     */
    @GetMapping("/agency/{agencyId}")
    public ResponseEntity<List<CarResponseDTO>> getCarsByAgency(@PathVariable Long agencyId) {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getByAgency(agencyId));
    }

    /**
     * Rechercher les voitures disponibles pour une période
     * GET /api/cars/available?startDate=2026-03-01&endDate=2026-03-05
     */
    @GetMapping("/available")
    public ResponseEntity<List<CarResponseDTO>> getAvailableCars(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getAvailableCars(startDate, endDate));
    }

    /**
     * Rechercher les voitures disponibles d'une catégorie pour une période
     * GET /api/cars/available/category/{categoryId}?startDate=2026-03-01&endDate=2026-03-05
     */
    @GetMapping("/available/category/{categoryId}")
    public ResponseEntity<List<CarResponseDTO>> getAvailableCarsByCategory(
            @PathVariable Long categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CarResponseDTO> response = carService.getAvailableCarsByCategory(categoryId, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(carService.getAvailableCarsByCategory(categoryId, startDate, endDate));
    }

    /**
     * Vérifier la disponibilité d'une voiture
     * GET /api/cars/{id}/availability?startDate=2026-03-01&endDate=2026-03-05
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> checkCarAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.status(HttpStatus.OK).body(carService.isCarAvailable(id, startDate, endDate));
    }

    /**
     * Mettre à jour une voiture
     * PUT /api/cars/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CarResponseDTO> updateCar(
            @PathVariable Long id,
            @Valid @RequestBody CarRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(carService.update(id, requestDTO));
    }

    /**
     * Supprimer une voiture
     * DELETE /api/cars/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
