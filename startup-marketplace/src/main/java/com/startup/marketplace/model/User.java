package com.startup.marketplace.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

/**
 * Coleccion: users
 *
 * Gestiona identidad y autorizacion.
 * Separado de Stand para mantener el principio de responsabilidad unica
 * y permitir que un mismo usuario tenga multiples stands (futuro).
 */
@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private Set<Role> roles;

    private String nombre;
    private String apellido;
    private String fotoPerfil;

    private boolean activo = true;

    private Instant createdAt = Instant.now();

    // -------------------------------------------------------------------
    // Roles del sistema
    // -------------------------------------------------------------------
    public enum Role {
        EMPRENDEDOR,  // Puede crear/editar su stand y productos
        COMPRADOR,    // Solo vitrinea — rol por defecto al registrarse
        ADMIN         // Acceso total
    }
}
