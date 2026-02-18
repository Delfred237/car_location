package com.example.demo.security;

import com.example.demo.Repository.RefreshTokenRepository;
import com.example.demo.entites.RefreshToken;
import com.example.demo.entites.User;
import com.example.demo.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService{

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;


    @Override
    public RefreshToken createRefreshToken(User user) {
        log.info("Création d'un refresh token pour l'utilisateur : {}", user.getEmail());

        // Revoquer les anciens refresh tokens
        revokeAllUserTokens(user);

        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .expiryDate(expiryDate)
                .user(user)
                .revoked(false)
                .build();

        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token créé avec succès (expire à {})", expiryDate);

        return savedRefreshToken;
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {
        log.info("Vérification du refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Refresh token invalide"));

        if (refreshToken.isRevoked()) {
            log.warn("Tentative d'utilisation d'un refresh token révoqué");
            throw new BusinessException("Ce refresh token a été révoqué");
        }

        if (refreshToken.isExpired()) {
            log.warn("Le refresh token a expiré");
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new BusinessException("Le refresh token a expiré. Veuillez vous reconnecter.");
        }

        log.info("Refresh token valide pour l'utilisateur : {}", refreshToken.getUser().getEmail());
        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {
        log.info("Révocation du refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Refresh token non trouvé"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token révoqué avec succès");
    }

    @Override
    public void revokeAllUserTokens(User user) {
        log.info("Révocation de tous les refresh tokens de l'utilisateur : {}", user.getEmail());
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public void cleanupExpiredTokens() {
        log.info("Nettoyage des refresh tokens expirés et révoqués");
        refreshTokenRepository.deleteExpiredAndRevokedTokens();
    }
}
