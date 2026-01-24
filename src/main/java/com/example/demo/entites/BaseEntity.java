package com.example.demo.entites;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    protected LocalDateTime updatedDate;

    /**
     * Detecte les conflits si deux utilisateurs modifient la meme entite
     *  Leve une exception
     *  Protege les donnees
     */
    @Version
    @Column(nullable = false)
    protected Long version;

    /**
     * Deux entités sont égales si :
     * - elles sont de la même classe
     * - elles ont un id non null identique
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    /**
     * HashCode stable basé sur la classe
     * (recommandé par Hibernate)
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Indique si l'entité a déjà été persistée
     */
    public boolean isPersisted() {
        return id != null;
    }
}
