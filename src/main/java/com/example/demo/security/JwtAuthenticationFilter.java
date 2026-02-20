package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${cookie.access-token.name}")
    private String accessTokenCookieName;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Extraire le jwt depuis les cookies
            String jwt = extractJwtFromCookies(request);

            // Si pas de token, continuer sans authentification
            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Extraire le username (email) du token
            String userEmail = jwtService.extractUsername(jwt);

            // Si l'utilisateur n'est pas deja authentifie
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Changer les details de l'utilisateur
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Valider le token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Creer l'objet d'authentification
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Mettre a jour le contexte de securite
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Utilisateur authentifié : {}", userEmail);
                } else {
                    log.warn("Token JWT invalide pour l'utilisateur: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification JWT : {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }


    private String extractJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> accessTokenCookieName.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
