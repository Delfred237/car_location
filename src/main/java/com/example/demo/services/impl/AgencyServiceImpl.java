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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AgencyServiceImpl implements AgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;


    @Override
    public AgencyResponseDTO createAgency(AgencyRequestDTO agencyRequestDTO) {
        // Verifier si l'agence existe deja avec ce nom
        if (agencyRepository.existsByName(agencyRequestDTO.getName())) {
            throw new ResourceAlreadyExistsException("Agency", "name", agencyRequestDTO.getName());
        }

        Agency agency = agencyMapper.toEntity(agencyRequestDTO);
        Agency agencySaved = agencyRepository.save(agency);

        return agencyMapper.toDto(agencySaved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgencyResponseDTO> getAll() {
        return agencyRepository.findAll().stream()
                .map(agencyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgencyResponseDTO> getById(Long id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", id));
        return Optional.ofNullable(agencyMapper.toDto(agency));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgencyResponseDTO> getByName(String name) {
        Agency agency = agencyRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "name", name));
        return Optional.ofNullable(agencyMapper.toDto(agency));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgencyResponseDTO> getByCity(String city) {
        Agency agency = agencyRepository.findByCity(city)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "city", city));
        return Optional.ofNullable(agencyMapper.toDto(agency));
    }

    @Override
    @Transactional
    public AgencyResponseDTO updateAgency(Long id, AgencyRequestDTO agencyRequestDTO) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", id));

        if (!agency.getName().equals(agencyRequestDTO.getName())
                && agencyRepository.existsByName(agencyRequestDTO.getName())) {
            throw new ResourceNotFoundException("Agency", "name", agencyRequestDTO.getName());
        }

        Agency agencyUpdated = agencyMapper.updateEnity(agency, agencyRequestDTO);
        Agency agencySaved = agencyRepository.save(agencyUpdated);

        return agencyMapper.toDto(agencySaved);
    }

    @Override
    public void deleteById(Long id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", id));

        agencyRepository.delete(agency);
    }

    @Override
    public boolean existsByName(String name) {
        return false;
    }
}
