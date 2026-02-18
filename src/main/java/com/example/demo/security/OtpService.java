package com.example.demo.security;

import com.example.demo.entites.User;
import com.example.demo.entites.VerificationToken;
import com.example.demo.enums.TokenType;

public interface OtpService {

    String generateOtp();

    VerificationToken createVerificationToken(User user, TokenType tokenType);

    boolean validateOtp(String email, String otp, TokenType tokenType);

    void deleteUsedTokens(User user);

    void cleanupExpiredTokens();
}
