package com.example.demo.mapper;

import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.entites.Role;
import com.example.demo.entites.User;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

public interface UserMapper {

    /**
     * Convertit UserRequestDTO → User (Entity)
     * On ignore les champs qui seront gérés par le service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    User toEntity(UserRequestDTO dto);

    /**
     * Convertit User (Entity) → UserResponseDTO
     * On utilise une méthode custom pour convertir Set<Role> → Set<String>
     */
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    UserResponseDTO toDTO(User user);


    /**
     * Met à jour une entité User existante avec les données du DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Géré séparément pour sécurité
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    void updateEntity(@MappingTarget User user, UserRequestDTO dto);

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
