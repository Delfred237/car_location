package com.example.demo.security;

import com.example.demo.Repository.RefreshTokenRepository;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.AuthResponseDTO;
import com.example.demo.email.EmailService;
import com.example.demo.entites.RefreshToken;
import com.example.demo.entites.Role;
import com.example.demo.entites.User;
import com.example.demo.entites.VerificationToken;
import com.example.demo.enums.RoleName;
import com.example.demo.enums.TokenType;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.ResourceAlreadyExistsException;
import com.example.demo.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;


    @Value("${cookie.access-token.name}")
    private String accessTokenCookieName;

    @Value("${cookie.refresh-token.name}")
    private String refreshTokenCookieName;

    @Value("${cookie.domain}")
    private String cookieDomain;

    @Value("${cookie.secure}")
    private String cookieSecure;

    @Value("${jwt.access-token.expiration}")
    private String accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private String refreshTokenExpiration;



    @Override
    public AuthResponseDTO register(RegisterRequestDTO requestDTO) {

        // Verifier si l'email existe deja
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("user", "email", requestDTO.getEmail());
        }

        // Creer l'utilisateur
        User user = User.builder()
                .fullName(requestDTO.getFullName())
                .email(requestDTO.getEmail())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .phoneNumber(requestDTO.getPhoneNumber())
                .enabled(false)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .failedLoginAttempts(0)
                .build();

        // Assigner le role CLIENT par defaut
        Role clientRole = roleRepository.findByName(RoleName.ROLE_CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", RoleName.ROLE_CLIENT));

        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Generer un code OTP et l'envoyer par email
        VerificationToken verificationToken = otpService.createVerificationToken(
                savedUser,
                TokenType.ACCOUNT_VERIFICATION
        );

        log.info("Envoie du mail a l'utilisateur : {} (OTP : {})", savedUser.getEmail(), verificationToken.getToken());
        emailService.sendAccountVerification(savedUser, verificationToken.getToken());

        log.info("Utilisateur inscrit avec succès : {} (ID : {})", savedUser.getEmail(), savedUser.getId());

        return AuthResponseDTO.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .roles(savedUser.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .message("Inscription réussie ! Un code de vérification a été envoyé à votre adresse email.")
                .accountVerified(false)
                .build();
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO requestDTO, HttpServletResponse response) {
        log.info("Tentative de connexion pour l'utilisateur : {}", requestDTO.getEmail());

        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new BusinessException("Email ou mot de passe incorrect"));

        // Vérifier si le compte est verrouillé
        if (!user.isAccountNonLocked()) {
            throw new BusinessException("Votre compte a été verrouillé. Veuillez contacter le support.");
        }

        try {
            // Authnetifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDTO.getEmail(),
                            requestDTO.getPassword()
                    )
            );

            // Renitialiser les tentatives echouees
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }
        } catch (DisabledException e) {
            log.warn("Tentative de connexion avec un compte non vérifié : {}", requestDTO.getEmail());
            throw new BusinessException("Votre compte n'est pas encore vérifié. Veuillez vérifier votre email.");
        } catch (BadCredentialsException e) {
            log.warn("Tentative de connexion avec des identifiants incorrects : {}", requestDTO.getEmail());

            // Incrémenter les tentatives échouées
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            // Verrouiller le compte après 5 tentatives
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountNonLocked(false);
                userRepository.save(user);
                throw new BusinessException("Trop de tentatives échouées. Votre compte a été verrouillé.");
            }

            userRepository.save(user);
            throw new BusinessException(
                    String.format("Email ou mot de passe incorrect. Il vous reste %d tentative(s).",
                            5 - user.getFailedLoginAttempts())
            );
        }

        // Generer les tokens
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Ajouter les tokens dans les cookies
        addTokensToCookies(response, accessToken, refreshToken.getToken());

        log.info("Connexion réussie pour l'utilisateur : {}", user.getEmail());

        return AuthResponseDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .message("Connexion réussie !")
                .accountVerified(user.isEnabled())
                .build();
    }

    @Override
    public AuthResponseDTO verifyAccount(VerifyOtpRequestDTO requestDTO, HttpServletResponse response) {
        log.info("Vérification du compte pour l'utilisateur : {}", requestDTO.getEmail());

        // Valider l'otp
        boolean isValid = otpService.validateOtp(
                requestDTO.getEmail(),
                requestDTO.getOtp(),
                TokenType.ACCOUNT_VERIFICATION
        );

        if (!isValid) {
            throw new BusinessException("Code de vérification invalide");
        }

        // Activer le compte
        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", requestDTO.getEmail()));

        user.setEnabled(true);
        userRepository.save(user);

        // Envoyer l'email de bienvenue
        emailService.sendWelcomeEmail(user);

        // Generer les tokens et les ajouter aux cookies
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        addTokensToCookies(response, accessToken, refreshToken.getToken());

        log.info("Compte vérifié avec succès pour l'utilisateur : {}", user.getEmail());

        return AuthResponseDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .message("Votre compte a été vérifié avec succès ! Bienvenue chez Car Rental.")
                .accountVerified(true)
                .build();
    }

    @Override
    public void resendVerificationCode(String email) {
        log.info("Renvoi du code de vérification à : {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.isEnabled()) {
            throw new BusinessException("Votre compte est déjà vérifié");
        }

        // Créer un nouveau code OTP
        VerificationToken verificationToken = otpService.createVerificationToken(
                user,
                TokenType.ACCOUNT_VERIFICATION
        );

        // Envoyer par email
        emailService.sendAccountVerification(user, verificationToken.getToken());

        log.info("Code de vérification renvoyé avec succès à : {}", email);
    }

    @Override
    public void requestPasswordReset(PasswordResetRequestDTO requestDTO) {
        log.info("Demande de réinitialisation de mot de passe pour : {}", requestDTO.getEmail());

        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", requestDTO.getEmail()));

        // Créer un code OTP pour la réinitialisation
        VerificationToken verificationToken = otpService.createVerificationToken(
                user,
                TokenType.PASSWORD_RESET
        );

        // Envoyer par email
        emailService.sendPasswordResetEmail(user, verificationToken.getToken());

        log.info("Email de réinitialisation envoyé à : {}", requestDTO.getEmail());
    }

    @Override
    public void resetPassword(PasswordResetConfirmDTO requestDTO) {
        log.info("Réinitialisation du mot de passe pour : {}", requestDTO.getEmail());

        // Valider l'OTP
        boolean isValid = otpService.validateOtp(
                requestDTO.getEmail(),
                requestDTO.getOtp(),
                TokenType.PASSWORD_RESET
        );

        if (!isValid) {
            throw new BusinessException("Code de vérification invalide");
        }

        // Mettre à jour le mot de passe
        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", requestDTO.getEmail()));

        user.setPassword(passwordEncoder.encode(requestDTO.getNewPassword()));
        userRepository.save(user);

        // Revoquer tous les refresh tokens
        refreshTokenService.revokeAllUserTokens(user);

        log.info("Mot de passe réinitialisé avec succès pour : {}", user.getEmail());
    }

    @Override
    public AuthResponseDTO refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("Rafraîchissement de l'access token");

        // Récupérer le refresh token depuis les cookies
        String refreshTokenValue = extractRefreshTokenFromCookies(request);

        if (refreshTokenValue == null) {
            throw new BusinessException("Refresh token manquant");
        }

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenValue);

        User user = refreshToken.getUser();

        // Generer un nouvel access token
        String newAccessToken = jwtService.generateAccessToken(user);

        // Ajouter le nouveau access token dans les cookies
        addAccessTokenToCookie(response, newAccessToken);

        log.info("Access token rafraîchi avec succès pour l'utilisateur : {}", user.getEmail());

        return AuthResponseDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .message("Token rafraîchi avec succès")
                .accountVerified(user.isEnabled())
                .build();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Déconnexion de l'utilisateur");

        // Récupérer le refresh token
        String refreshTokenValue = extractRefreshTokenFromCookies(request);

        if (refreshTokenValue != null) {
            // Revoquer le refresh token
            try {
                refreshTokenService.revokeRefreshToken(refreshTokenValue);
            } catch (Exception e) {
                log.warn("Erreur lors de la révocation du refresh token : {}", e.getMessage());
            }
        }

        // Supprimer les cookies
        clearAuthCookies(response);

        log.info("Déconnexion réussie");
    }


    // ========== MÉTHODES UTILITAIRES ==========
    private void addTokensToCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        addAccessTokenToCookie(response, accessToken);
        addRefreshTokenToCookie(response, refreshToken);
    }

    private void addAccessTokenToCookie(HttpServletResponse response, String accessToken) {
        Cookie accessTokenCookie = new Cookie(accessTokenCookieName, accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(Boolean.parseBoolean(cookieSecure));
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(parseInt(accessTokenExpiration) / 1000);
        accessTokenCookie.setAttribute("SameSite", "Lax");

        response.addCookie(accessTokenCookie);
    }

    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie(refreshTokenCookieName, refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(Boolean.parseBoolean(cookieSecure));
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (parseInt(refreshTokenExpiration) / 1000));
        refreshTokenCookie.setAttribute("SameSite", "Lax");

        response.addCookie(refreshTokenCookie);
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> refreshTokenCookieName.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie(accessTokenCookieName, null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(Boolean.parseBoolean(cookieSecure));
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        Cookie refreshTokenCookie = new Cookie(refreshTokenCookieName, null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(Boolean.parseBoolean(cookieSecure));
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }
}
