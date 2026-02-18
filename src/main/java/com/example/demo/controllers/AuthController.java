package com.example.demo.controllers;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.AuthResponseDTO;
import com.example.demo.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    /**
     * Inscription d'un nouvel utilisateur
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDTO) {
        log.info("POST /api/auth/register - Inscription : {}", requestDTO.getEmail());
        AuthResponseDTO response = authService.register(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Connexion
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO requestDTO,
            HttpServletResponse response) {
        log.info("POST /api/auth/login - Connexion : {}", requestDTO.getEmail());
        AuthResponseDTO authResponse = authService.login(requestDTO, response);
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    /**
     * Vérifier le compte avec le code OTP
     * POST /api/auth/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<AuthResponseDTO> verifyAccount(
            @Valid @RequestBody VerifyOtpRequestDTO requestDTO,
            HttpServletResponse response) {
        log.info("POST /api/auth/verify - Vérification : {}", requestDTO.getEmail());
        AuthResponseDTO authResponse = authService.verifyAccount(requestDTO, response);
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    /**
     * Renvoyer le code de vérification
     * POST /api/auth/resend-code
     */
    @PostMapping("/resend-code")
    public ResponseEntity<Map<String, String>> resendVerificationCode(@RequestParam String email) {
        log.info("POST /api/auth/resend-code - Email : {}", email);
        authService.resendVerificationCode(email);
        return ResponseEntity.ok(Map.of("message", "Un nouveau code de vérification a été envoyé à votre email."));
    }

    /**
     * Demander la réinitialisation du mot de passe
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody PasswordResetRequestDTO requestDTO) {
        log.info("POST /api/auth/forgot-password - Email : {}", requestDTO.getEmail());
        authService.requestPasswordReset(requestDTO);
        return ResponseEntity.ok(Map.of("message", "Un code de réinitialisation a été envoyé à votre email."));
    }

    /**
     * Réinitialiser le mot de passe avec le code OTP
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmDTO requestDTO) {
        log.info("POST /api/auth/reset-password - Email : {}", requestDTO.getEmail());
        authService.resetPassword(requestDTO);
        return ResponseEntity.ok(Map.of("message", "Votre mot de passe a été réinitialisé avec succès."));
    }

    /**
     * Rafraîchir l'access token
     * POST /api/auth/refresh-token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDTO> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        log.info("POST /api/auth/refresh-token - Rafraîchissement du token");
        AuthResponseDTO authResponse = authService.refreshAccessToken(request, response);
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    /**
     * Déconnexion
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        log.info("POST /api/auth/logout - Déconnexion");
        authService.logout(request, response);
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie."));
    }

    /**
     * Vérifier si l'utilisateur est authentifié (endpoint de test)
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(HttpServletRequest request) {
        log.info("GET /api/auth/me - Récupération de l'utilisateur actuel");
        // L'utilisateur est automatiquement injecté par Spring Security
        return ResponseEntity.ok(Map.of(
                "message", "Vous êtes authentifié",
                "user", request.getUserPrincipal().getName()
        ));
    }
}
