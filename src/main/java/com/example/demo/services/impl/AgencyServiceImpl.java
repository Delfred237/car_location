package com.example.demo.services.impl;

import com.example.demo.Repository.AgencyRepository;
import com.example.demo.dto.request.AgencyRequestDTO;
import com.example.demo.dto.response.AgencyResponseDTO;
import com.example.demo.entites.Agency;
import com.example.demo.exceptions.ResourceAlreadyExistsException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mapper.AgencyMapper;
import com.example.demo.services.AgencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AgencyServiceImpl implements AgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;


    @Override
    @Transactional
    public AgencyResponseDTO createAgency(AgencyRequestDTO agencyRequestDTO) {
        log.info("Création d'une nouvelle agence : {}", agencyRequestDTO.getName());

        // Verifier si l'agence existe deja avec ce nom
        if (agencyRepository.existsByName(agencyRequestDTO.getName())) {
            throw new ResourceAlreadyExistsException("Agency", "name", agencyRequestDTO.getName());
        }

        Agency agency = agencyMapper.toEntity(agencyRequestDTO);
        Agency savedAgency = agencyRepository.save(agency);

        log.info("Agence créée avec succès : ID {}", savedAgency.getId());
        return agencyMapper.toDto(savedAgency);
    }

    @Override
    public List<AgencyResponseDTO> getAll() {
        log.info("Récupération de toutes les agences");

        return agencyRepository.findAll().stream()
                .map(agencyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AgencyResponseDTO> getById(Long id) {
        log.info("Recherche de l'agence avec ID : {}", id);

        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", id));
        return Optional.ofNullable(agencyMapper.toDto(agency));
    }

    @Override
    public Optional<AgencyResponseDTO> getByName(String name) {
        log.info("Recherche de l'agence avec le nom : {}", name);

        Agency agency = agencyRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "name", name));
        return Optional.ofNullable(agencyMapper.toDto(agency));
    }

    @Override
    public List<AgencyResponseDTO> getByCity(String city) {
        log.info("Recherche des agences dans la ville : {}", city);

        Agency agency = agencyRepository.findByCity(city)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "city", city));
        return agencyRepository.findAll().stream()
                .map(agencyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AgencyResponseDTO updateAgency(Long id, AgencyRequestDTO agencyRequestDTO) {
        log.info("Mise à jour de l'agence ID : {}", id);

        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", id));

        if (!agency.getName().equals(agencyRequestDTO.getName())
                && agencyRepository.existsByName(agencyRequestDTO.getName())) {
            throw new ResourceNotFoundException("Agency", "name", agencyRequestDTO.getName());
        }

        Agency updatedAgency = agencyMapper.updateEnity(agency, agencyRequestDTO);
        Agency agencySaved = agencyRepository.save(updatedAgency);

        log.info("Agence mise à jour avec succès : ID {}", updatedAgency.getId());
        return agencyMapper.toDto(agencySaved);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Suppression de l'agence ID : {}", id);

        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", id));

        agencyRepository.delete(agency);
        log.info("Agence supprimée avec succès : ID {}", id);
    }

    @Override
    public boolean existsByName(String name) {
        return agencyRepository.existsByName(name);
    }
}
