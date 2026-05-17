package com.startup.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileDtos {

    public record UpdateProfileRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        String fotoPerfil
    ) {}

    public record ChangePasswordRequest(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String passwordActual,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 6, message = "Mínimo 6 caracteres")
        String passwordNueva
    ) {}

    public record ProfileResponse(
        String id,
        String email,
        String nombre,
        String apellido,
        String fotoPerfil,
        String rol,
        boolean tieneStand
    ) {}
}
