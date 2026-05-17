package com.startup.marketplace.controller;

import com.startup.marketplace.dto.AuthDtos.JwtResponse;
import com.startup.marketplace.dto.AuthDtos.LoginRequest;
import com.startup.marketplace.dto.AuthDtos.RegisterRequest;
import com.startup.marketplace.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/auth/register  -> Registro (COMPRADOR por defecto, o EMPRENDEDOR si se indica)
 * POST /api/auth/login     -> Login — devuelve JWT
 *
 * Ambos endpoints son publicos (configurado en SecurityConfig).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
