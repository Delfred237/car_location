package com.example.demo.services.impl;


import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.entites.Role;
import com.example.demo.entites.User;
import com.example.demo.enums.RoleName;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.ResourceAlreadyExistsException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;


    @Override
    public UserResponseDTO create(UserRequestDTO userRequestDTO) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", userRequestDTO.getEmail());
        }

        User user = userMapper.toEntity(userRequestDTO);

        // Assigner le role CLIENT par defaut
        Role clientRole = roleRepository.findByName(RoleName.ROLE_CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", RoleName.ROLE_CLIENT));

        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO requestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Vérifier si le nouvel email existe déjà (si changé)
        if (!user.getEmail().equals(requestDTO.getEmail())
                && userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", requestDTO.getEmail());
        }

        userMapper.updateEntity(user, requestDTO);

        User updatedUser = userRepository.save(user);

        return userMapper.toDTO(updatedUser);
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.delete(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        RoleName roleNameEnum;
        try {
            roleNameEnum = RoleName.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Rôle invalide : " + roleName);
        }

        Role role = roleRepository.findByName(roleNameEnum)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleNameEnum));

        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        RoleName roleNameEnum;
        try {
            roleNameEnum = RoleName.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Rôle invalide : " + roleName);
        }

        Role role = roleRepository.findByName(roleNameEnum)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleNameEnum));

        user.getRoles().remove(role);
        userRepository.save(user);
    }
}
