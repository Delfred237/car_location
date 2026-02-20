package com.example.demo.security;

import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Chargement de l'utilisateur : {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé : {}", email);
                    return new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email);
                });
    }
}
