package com.startup.marketplace.dto;

import com.startup.marketplace.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTOs de autenticacion — Java Records (Java 21).
 * Inmutables, sin boilerplate, con validacion de Bean Validation.
 */
public class AuthDtos {

    /**
     * POST /api/auth/register
     * El campo "rol" es opcional; por defecto se asigna COMPRADOR.
     */
    public record RegisterRequest(
            @NotBlank(message = "El email es obligatorio")
            @Email(message = "Formato de email invalido")
            String email,

            @NotBlank(message = "La contrasena es obligatoria")
            @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
            String password,

            // null = COMPRADOR por defecto
            User.Role rol
    ) {}

    /**
     * POST /api/auth/login
     */
    public record LoginRequest(
            @NotBlank(message = "El email es obligatorio")
            @Email(message = "Formato de email invalido")
            String email,

            @NotBlank(message = "La contrasena es obligatoria")
            String password
    ) {}

    /**
     * Respuesta unificada para register y login.
     */
    public record JwtResponse(
            String token,
            String email,
            String rol,
            String nombre,
            String apellido,
            String fotoPerfil
    ) {}
}
