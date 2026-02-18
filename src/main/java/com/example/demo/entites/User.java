package com.example.demo.entites;

import com.example.demo.enums.PieceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "users")
@Entity
public class User extends BaseEntity implements UserDetails {

    @NotBlank(message = "Le nom complet est obligatoire")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(nullable = false)
    private String password;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // ========== CHAMPS DE SÉCURITÉ ==========

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = false; // Compte non vérifié par défaut

    @Column(name = "is_account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "is_account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Column(name = "is_credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
