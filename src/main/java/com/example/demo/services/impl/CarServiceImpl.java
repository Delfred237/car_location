package com.example.demo.services.impl;

import com.example.demo.Repository.AgencyRepository;
import com.example.demo.Repository.CarRepository;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Repository.ReservationRepository;
import com.example.demo.dto.request.CarRequestDTO;
import com.example.demo.dto.response.CarResponseDTO;
import com.example.demo.entites.Agency;
import com.example.demo.entites.Car;
import com.example.demo.entites.Category;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.exceptions.ResourceAlreadyExistsException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mapper.CarMapper;
import com.example.demo.services.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CategoryRepository categoryRepository;
    private final AgencyRepository agencyRepository;
    private final ReservationRepository reservationRepository;
    private final CarMapper carMapper;


    @Override
    @Transactional
    public CarResponseDTO create(CarRequestDTO requestDTO) {
        // Vérifier si la plaque existe déjà
        if (carRepository.existsByLicensePlate(requestDTO.getLicensePlate())) {
            throw new ResourceAlreadyExistsException("Car", "licensePlate", requestDTO.getLicensePlate());
        }

        // Récupérer la catégorie
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", requestDTO.getCategoryId()));

        // Récupérer l'agence si spécifiée
        Agency agency = null;
        if (requestDTO.getAgencyId() != null) {
            agency = agencyRepository.findById(requestDTO.getAgencyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", requestDTO.getAgencyId()));
        }

        Car car = carMapper.toEntity(requestDTO);
        car.setCategory(category);
        car.setAgency(agency);

        Car savedCar = carRepository.save(car);

        return carMapper.toDTO(savedCar);
    }

    @Override
    public CarResponseDTO getById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));

        return carMapper.toDTO(car);
    }

    @Override
    public List<CarResponseDTO> getAll() {
        return carRepository.findAll().stream()
                .map(carMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarResponseDTO> getByCategory(Long categoryId) {
        // Vérifier que la catégorie existe
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }

        return carRepository.findByCategoryId(categoryId).stream()
                .map(carMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarResponseDTO> getByAgency(Long agencyId) {
        // Vérifier que l'agence existe
        if (!agencyRepository.existsById(agencyId)) {
            throw new ResourceNotFoundException("Agency", "id", agencyId);
        }

        return carRepository.findByAgencyId(agencyId).stream()
                .map(carMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarResponseDTO> getAvailableCars(LocalDate startDate, LocalDate endDate) {
        return carRepository.findAvailableCars(startDate, endDate).stream()
                .map(carMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarResponseDTO> getAvailableCarsByCategory(Long categoryId, LocalDate startDate, LocalDate endDate) {
        // Vérifier que la catégorie existe
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }

        return carRepository.findAvailableCarsByCategory(categoryId, startDate, endDate).stream()
                .map(carMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CarResponseDTO update(Long id, CarRequestDTO requestDTO) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));

        // Vérifier si la nouvelle plaque existe déjà (si changée)
        if (!car.getLicensePlate().equals(requestDTO.getLicensePlate())
                && carRepository.existsByLicensePlate(requestDTO.getLicensePlate())) {
            throw new ResourceAlreadyExistsException("Car", "licensePlate", requestDTO.getLicensePlate());
        }

        // Récupérer la nouvelle catégorie
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", requestDTO.getCategoryId()));

        // Récupérer la nouvelle agence si spécifiée
        Agency agency = null;
        if (requestDTO.getAgencyId() != null) {
            agency = agencyRepository.findById(requestDTO.getAgencyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", requestDTO.getAgencyId()));
        }

        carMapper.updateEntity(car, requestDTO);
        car.setCategory(category);
        car.setAgency(agency);

        Car updatedCar = carRepository.save(car);

        return carMapper.toDTO(updatedCar);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));

        carRepository.delete(car);
    }

    @Override
    public boolean isCarAvailable(Long carId, LocalDate startDate, LocalDate endDate) {
        // Vérifier que la voiture existe
        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car", "id", carId);
        }

        // Vérifier qu'il n'y a pas de réservations qui chevauchent
        List<ReservationStatus> activeStatuses = Arrays.asList(
                ReservationStatus.CONFIRMED,
                ReservationStatus.ONGOING
        );

        return !reservationRepository.existsOverlappingReservation(
                carId, startDate, endDate, activeStatuses
        );
    }
}
