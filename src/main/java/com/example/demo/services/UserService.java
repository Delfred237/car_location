package com.example.demo.services;

import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO create(UserRequestDTO userRequestDTO);

    UserResponseDTO getById(Long id);

    UserResponseDTO getByEmail(String email);

    List<UserResponseDTO> getAll();

    UserResponseDTO update(Long id, UserRequestDTO requestDTO);

    void delete(Long id);

    boolean existsByEmail(String email);

    void assignRoleToUser(Long userId, String roleName);

    void removeRoleFromUser(Long userId, String roleName);
}
