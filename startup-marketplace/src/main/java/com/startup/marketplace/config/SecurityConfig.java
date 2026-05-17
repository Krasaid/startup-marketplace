package com.startup.marketplace.config;

import com.startup.marketplace.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion de Spring Security — Stateless / JWT.
 *
 * Reglas de acceso:
 * - /api/auth/**           -> publico (registro + login)
 * - GET /api/stands/**     -> publico (feed + detalle de stand)
 * - POST/PUT /api/stands** -> requiere EMPRENDEDOR o ADMIN
 * - Todo lo demas          -> autenticado
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${startup.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // Auth publica
                .requestMatchers("/api/auth/**").permitAll()

                // Imagenes subidas — acceso publico para que el SPA las muestre
                .requestMatchers("/uploads/**").permitAll()

                // Feed publico y detalle de stand (vitrineo)
                .requestMatchers(HttpMethod.GET, "/api/stands/**").permitAll()

                // Motor de redireccion/tracking publico — 302 hacia WhatsApp/IG
                .requestMatchers(HttpMethod.GET, "/api/stands/*/redirect/*").permitAll()

                // Escrituras: solo EMPRENDEDOR o ADMIN
                .requestMatchers(HttpMethod.POST,   "/api/stands/**")
                    .hasAnyRole("EMPRENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/stands/**")
                    .hasAnyRole("EMPRENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/stands/**")
                    .hasAnyRole("EMPRENDEDOR", "ADMIN")

                // Todo lo demas requiere autenticacion
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
