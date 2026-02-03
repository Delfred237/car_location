package com.example.demo.controllers;

import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;


    /**
     * Créer un nouveau utilisateur (inscription)
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(requestDTO));
    }

    /**
     * Récupérer un utilisateur par ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getById(id));
    }

    /**
     * Récupérer tous les utilisateurs
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAll());
    }

    /**
     * Rechercher un utilisateur par email
     * GET /api/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getByEmail(email));
    }

    /**
     * Mettre à jour un utilisateur
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.update(id, requestDTO));
    }

    /**
     * Supprimer un utilisateur
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Attribuer un rôle à un utilisateur (ADMIN uniquement)
     * POST /api/users/{id}/roles/{roleName}
     */
    @PostMapping("/{id}/roles/{roleName}")
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable Long id,
            @PathVariable String roleName) {
        userService.assignRoleToUser(id, roleName);
        return ResponseEntity.ok().build();
    }

    /**
     * Retirer un rôle d'un utilisateur (ADMIN uniquement)
     * DELETE /api/users/{id}/roles/{roleName}
     */
    @DeleteMapping("/{id}/roles/{roleName}")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable Long id,
            @PathVariable String roleName) {
        userService.removeRoleFromUser(id, roleName);
        return ResponseEntity.noContent().build();
    }

}
