package com.example.demo.Repository;

import com.example.demo.entites.Role;
import com.example.demo.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Recherche un rôle par son nom
     * @param name le nom du rôle (ROLE_CLIENT, ROLE_ADMIN, etc.)
     * @return Optional contenant le rôle s'il existe
     */
    Optional<Role> findByName(RoleName name);

    /**
     * Vérifie si un rôle existe par son nom
     * @param name le nom du rôle
     * @return true si le rôle existe
     */
    boolean existsByName(RoleName name);
}
