package com.example.demo.Repository;

import com.example.demo.entites.Car;
import com.example.demo.entites.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    Optional<Car> findByName(String name);
    Optional<Car> findByLicensePlate(String licensePlate);
    boolean existsByLicensePlate(String licensePlate);
    List<Car> findByCategory(Long categoryId);
    List<Car> findByAgency(Long agencyId);
    List<Car> findByBrand(String brand);
    List<Car> findByBrandAndModel(String brand, String model);

    @Query("SELECT c FROM Car c WHERE c.id NOT IN " +
            "(SELECT r.car.id FROM Reservation r WHERE " +
            "r.status IN ('CONFIRMED', 'ONGOING') AND " +
            "(r.startDate <= :endDate AND r.endDate >= :startDate))")
    List<Car> findAvailableCars(@Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
}
