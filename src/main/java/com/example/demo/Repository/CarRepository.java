package com.example.demo.Repository;

import com.example.demo.entites.Agency;
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

    /**
     * Recherche une voiture par sa plaque d'immatriculation
     * @param licensePlate la plaque d'immatriculation
     * @return Optional contenant la voiture si elle existe
     */
    Optional<Car> findByLicensePlate(String licensePlate);

    /**
     * Vérifie si une plaque d'immatriculation existe
     * @param licensePlate la plaque d'immatriculation
     * @return true si la plaque existe
     */
    boolean existsByLicensePlate(String licensePlate);

    /**
     * Recherche toutes les voitures d'une catégorie
     * @param category la catégorie
     * @return liste des voitures de cette catégorie
     */
    List<Car> findByCategory(Category category);

    /**
     * Recherche toutes les voitures d'une catégorie par ID
     * @param categoryId l'ID de la catégorie
     * @return liste des voitures de cette catégorie
     */
    List<Car> findByCategoryId(Long categoryId);

    /**
     * Recherche toutes les voitures d'une agence
     * @param agency l'agence
     * @return liste des voitures de cette agence
     */
    List<Car> findByAgency(Agency agency);

    /**
     * Recherche toutes les voitures d'une agence par ID
     * @param agencyId l'ID de l'agence
     * @return liste des voitures de cette agence
     */
    List<Car> findByAgencyId(Long agencyId);

    /**
     * Recherche toutes les voitures par marque
     * @param brand la marque
     * @return liste des voitures de cette marque
     */
    List<Car> findByBrand(String brand);

    /**
     * Recherche toutes les voitures par marque et modèle
     * @param brand la marque
     * @param model le modèle
     * @return liste des voitures correspondantes
     */
    List<Car> findByBrandAndModel(String brand, String model);

    /**
     * Recherche les voitures disponibles pour une période donnée
     * (voitures qui n'ont pas de réservation confirmée sur cette période)
     * @param startDate date de début
     * @param endDate date de fin
     * @return liste des voitures disponibles
     */
    @Query("SELECT c FROM Car c WHERE c.id NOT IN " +
            "(SELECT r.car.id FROM Reservation r WHERE " +
            "r.status IN ('CONFIRMED', 'ONGOING') AND " +
            "(r.startDate <= :endDate AND r.endDate >= :startDate))")
    List<Car> findAvailableCars(@Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    /**
     * Recherche les voitures disponibles d'une catégorie pour une période donnée
     * @param categoryId l'ID de la catégorie
     * @param startDate date de début
     * @param endDate date de fin
     * @return liste des voitures disponibles
     */
    @Query("SELECT c FROM Car c WHERE c.category.id = :categoryId AND c.id NOT IN " +
            "(SELECT r.car.id FROM Reservation r WHERE " +
            "r.status IN ('CONFIRMED', 'ONGOING') AND " +
            "(r.startDate <= :endDate AND r.endDate >= :startDate))")
    List<Car> findAvailableCarsByCategory(@Param("categoryId") Long categoryId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}
