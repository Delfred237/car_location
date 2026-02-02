package com.example.demo.services;

import com.example.demo.dto.request.CarRequestDTO;
import com.example.demo.dto.response.CarResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface CarService {

    CarResponseDTO create(CarRequestDTO requestDTO);

    CarResponseDTO getById(Long id);

    List<CarResponseDTO> getAll();

    List<CarResponseDTO> getByCategory(Long categoryId);

    List<CarResponseDTO> getByAgency(Long agencyId);

    List<CarResponseDTO> getAvailableCars(LocalDate startDate, LocalDate endDate);

    List<CarResponseDTO> getAvailableCarsByCategory(Long categoryId, LocalDate startDate, LocalDate endDate);

    CarResponseDTO update(Long id, CarRequestDTO requestDTO);

    void delete(Long id);

    boolean isCarAvailable(Long carId, LocalDate startDate, LocalDate endDate);
}
