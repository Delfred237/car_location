package com.example.demo.services;

import com.example.demo.dto.request.AgencyRequestDTO;
import com.example.demo.dto.response.AgencyResponseDTO;

import java.util.List;
import java.util.Optional;

public interface AgencyService {

    AgencyResponseDTO createAgency(AgencyRequestDTO agencyRequestDTO);

    List<AgencyResponseDTO> getAll();

    Optional<AgencyResponseDTO> getById(Long id);

    Optional<AgencyResponseDTO> getByName(String name);

    List<AgencyResponseDTO> getByCity(String city);

    AgencyResponseDTO updateAgency(Long id, AgencyRequestDTO agencyRequestDTO);

    void deleteById(Long id);

    boolean existsByName(String name);

}
