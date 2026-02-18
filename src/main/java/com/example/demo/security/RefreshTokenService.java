package com.example.demo.security;

import com.example.demo.entites.RefreshToken;
import com.example.demo.entites.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllUserTokens(User user);

    void cleanupExpiredTokens();
}
