package com.startup.marketplace.service;

import com.startup.marketplace.dto.ProfileDtos.ChangePasswordRequest;
import com.startup.marketplace.dto.ProfileDtos.ProfileResponse;
import com.startup.marketplace.dto.ProfileDtos.UpdateProfileRequest;
import com.startup.marketplace.model.User;
import com.startup.marketplace.repository.StandRepository;
import com.startup.marketplace.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository  userRepository;
    private final StandRepository standRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository,
                          StandRepository standRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.standRepository = standRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ProfileResponse getProfile(String email) {
        User user = resolveUser(email);
        boolean tieneStand = standRepository.findByUserId(user.getId()).isPresent();
        return toResponse(user, tieneStand);
    }

    public ProfileResponse updateProfile(String email, UpdateProfileRequest req) {
        User user = resolveUser(email);
        user.setNombre(req.nombre());
        user.setApellido(req.apellido());
        user.setFotoPerfil(req.fotoPerfil());
        userRepository.save(user);
        boolean tieneStand = standRepository.findByUserId(user.getId()).isPresent();
        return toResponse(user, tieneStand);
    }

    public void changePassword(String email, ChangePasswordRequest req) {
        User user = resolveUser(email);
        if (!passwordEncoder.matches(req.passwordActual(), user.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        user.setPasswordHash(passwordEncoder.encode(req.passwordNueva()));
        userRepository.save(user);
    }

    public void deleteAccount(String email) {
        User user = resolveUser(email);
        standRepository.findByUserId(user.getId()).ifPresent(stand -> {
            stand.setActivo(false);
            standRepository.save(stand);
        });
        userRepository.delete(user);
    }

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private ProfileResponse toResponse(User user, boolean tieneStand) {
        return new ProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getNombre(),
            user.getApellido(),
            user.getFotoPerfil(),
            user.getRoles().iterator().next().name(),
            tieneStand
        );
    }
}
