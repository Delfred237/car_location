package com.example.demo.security;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.AuthResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponseDTO register(RegisterRequestDTO requestDTO);

    AuthResponseDTO login(LoginRequestDTO requestDTO, HttpServletResponse response);

    AuthResponseDTO verifyAccount(VerifyOtpRequestDTO requestDTO, HttpServletResponse response);

    void resendVerificationCode(String email);

    void requestPasswordReset(PasswordResetRequestDTO requestDTO);

    void requestPassword(PasswordResetConfirmDTO requestDTO);

    AuthResponseDTO refreshAccessToken(HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);
}
