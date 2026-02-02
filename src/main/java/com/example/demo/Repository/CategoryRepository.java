package com.example.demo.Repository;

import com.example.demo.entites.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {

    /**
     * Recherche une catégorie par son nom
     * @param name le nom de la catégorie
     * @return Optional contenant la catégorie si elle existe
     */
    Optional<Category> findByName(String name);

    /**
     * Vérifie si une catégorie existe par son nom
     * @param name le nom de la catégorie
     * @return true si la catégorie existe
     */
    boolean existsByName(String name);
}
