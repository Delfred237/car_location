package com.example.demo.Repository;

import com.example.demo.entites.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {

    /**
     * Recherche une agence par son nom
     * @param name le nom de l'agence
     * @return Optional contenant l'agence si elle existe
     */
    Optional<Agency> findByName(String name);

    /**
     * Vérifie si une agence existe par son nom
     * @param name le nom de l'agence
     * @return true si l'agence existe
     */
    boolean existsByName(String name);

    /**
     * Recherche toutes les agences d'une ville
     * @param city le nom de la ville
     * @return liste des agences de la ville
     */
    Optional<Agency> findByCity(String city);
}
