package com.example.demo.controllers;

import com.example.demo.dto.request.AgencyRequestDTO;
import com.example.demo.dto.response.AgencyResponseDTO;
import com.example.demo.services.AgencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequestMapping("/agencies")
@RequiredArgsConstructor
@RestController
public class AgencyController {

    private final AgencyService agencyService;


    /**
     * Créer une nouvelle agence
     * POST /api/agencies
     */
    @PostMapping
    public ResponseEntity<AgencyResponseDTO> createAgency(@Valid @RequestBody AgencyRequestDTO requestDTO) {
        log.info("POST /api/agencies - Création d'une agence : {}", requestDTO.getName());
        AgencyResponseDTO response = agencyService.createAgency(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer toutes les agences
     * GET /api/agencies
     */
    @GetMapping
    public ResponseEntity<List<AgencyResponseDTO>> getAllAgencies() {
        log.info("GET /api/agencies - Récupération de toutes les agences");
        return ResponseEntity.status(HttpStatus.OK).body(agencyService.getAll());
    }

    /**
     * Rechercher une agence par id
     * GET /api/agencies/id/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Optional<AgencyResponseDTO>> getAgencyById(@PathVariable Long id) {
        log.info("GET /api/agencies/{} - Récupération de l'agence", id);
        return ResponseEntity.status(HttpStatus.OK).body(agencyService.getById(id));
    }

    /**
     * Rechercher des agences par ville
     * GET /api/agencies/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<AgencyResponseDTO>> getAgenciesByCity(@PathVariable String city) {
        log.info("GET /api/agencies/city/{} - Recherche par ville", city);
        return ResponseEntity.status(HttpStatus.OK).body(agencyService.getByCity(city));
    }

    /**
     * Mettre à jour une agence
     * PUT /api/agencies/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AgencyResponseDTO> updateAgency(
            @PathVariable Long id,
            @Valid @RequestBody AgencyRequestDTO requestDTO) {
        log.info("PUT /api/agencies/{} - Mise à jour de l'agence", id);
        return ResponseEntity.status(HttpStatus.OK).body(agencyService.updateAgency(id, requestDTO));
    }

    /**
     * Supprimer une agence
     * DELETE /api/agencies/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgency(@PathVariable Long id) {
        log.info("DELETE /api/agencies/{} - Suppression de l'agence", id);
        agencyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
