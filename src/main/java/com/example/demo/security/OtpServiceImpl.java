package com.example.demo.security;

import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.VerificationTokenRepository;
import com.example.demo.entites.User;
import com.example.demo.entites.VerificationToken;
import com.example.demo.enums.TokenType;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class OtpServiceImpl implements OtpService{

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Value("${otp.expiration}")
    private long otpExpiration;

    @Value("${otp.length}")
    private int otpLength;

    @Value("${otp.max-attemps}")
    private int maxAttempts;


    @Override
    public String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }

    @Override
    public VerificationToken createVerificationToken(User user, TokenType tokenType) {
        log.info("Création d'un token de vérification pour l'utilisateur : {}", user.getEmail());

        // Suppression des anciens tokens non utilises
        tokenRepository.deleteByUser(user);

        String otp = generateOtp();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(otpExpiration / 1000);

        VerificationToken verificationToken = VerificationToken.builder()
                .token(otp)
                .expiryDate(expiryDate)
                .tokenType(tokenType)
                .user(user)
                .attempts(0)
                .build();

        VerificationToken savedToken = tokenRepository.save(verificationToken);
        log.info("Token de vérification créé avec succès : {} (expire à {})", otp, expiryDate);

        return savedToken;
    }

    @Override
    public boolean validateOtp(String email, String otp, TokenType tokenType) {
        log.info("Validation de l'OTP pour l'email : {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        VerificationToken token = tokenRepository.findByUserAndTokenTypeAndUsedFalse(user, tokenType)
                .orElseThrow(() -> new BusinessException("Aucun code de vérification valide trouvé"));

        // Verifier si le code a expire
        if (token.isExpired()) {
            log.warn("Le token OTP a expiré pour l'utilisateur : {}", email);
            throw new BusinessException("Le code de vérification a expiré. Veuillez en demander un nouveau.");
        }

        // Verifier le nombre de tentatives
        if (token.getAttempts() >= maxAttempts) {
            log.warn("Nombre maximum de tentatives atteint pour l'utilisateur : {}", email);
            token.setUsed(true);
            tokenRepository.save(token);
            throw new BusinessException("Trop de tentatives. Veuillez demander un nouveau code.");
        }

        // Incrementer le nombre de tentatives
        token.setAttempts(token.getAttempts() + 1);
        tokenRepository.save(token);

        // Verifier si l'otp est correct
        if (!token.getToken().equals(otp)) {
            log.warn("OTP incorrect pour l'utilisateur : {} (tentative {}/{})",
                    email, token.getAttempts(), maxAttempts);
            throw new BusinessException(
                    String.format("Code incorrect. Il vous reste %d tentative(s).",
                            maxAttempts - token.getAttempts())
            );
        }

        // Marquer le token comme utilise
        token.setUsed(true);
        tokenRepository.save(token);

        log.info("OTP validé avec succès pour l'utilisateur : {}", email);
        return true;
    }

    @Override
    public void deleteUsedTokens(User user) {
        tokenRepository.deleteByUser(user);
    }

    @Override
    public void cleanupExpiredTokens() {
        log.info("Nettoyage des tokens expirés...");
        // Implementer un scheduler plus tard pour le nettoyage automatique
    }
}


