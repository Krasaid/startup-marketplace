package com.startup.marketplace.service;

import com.startup.marketplace.dto.AuthDtos.JwtResponse;
import com.startup.marketplace.dto.AuthDtos.LoginRequest;
import com.startup.marketplace.dto.AuthDtos.RegisterRequest;
import com.startup.marketplace.model.User;
import com.startup.marketplace.repository.UserRepository;
import com.startup.marketplace.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil              jwtUtil;
    private final UserDetailsService   userDetailsService;

    // -------------------------------------------------------------------
    // Registro
    // -------------------------------------------------------------------
    public JwtResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalStateException("El email ya esta registrado: " + req.email());
        }

        var user = new User();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRoles(Set.of(req.rol() != null ? req.rol() : User.Role.COMPRADOR));
        userRepository.save(user);

        var userDetails = userDetailsService.loadUserByUsername(req.email());
        String token   = jwtUtil.generateToken(userDetails);

        return new JwtResponse(token, req.email(), user.getRoles().iterator().next().name());
    }

    // -------------------------------------------------------------------
    // Login — AuthenticationManager valida la contrasena via BCrypt
    // -------------------------------------------------------------------
    public JwtResponse login(LoginRequest req) {
        // Lanza AuthenticationException si las credenciales son incorrectas
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        var userDetails = userDetailsService.loadUserByUsername(req.email());
        String token    = jwtUtil.generateToken(userDetails);

        var user = userRepository.findByEmail(req.email()).orElseThrow();
        return new JwtResponse(token, req.email(), user.getRoles().iterator().next().name());
    }
}
