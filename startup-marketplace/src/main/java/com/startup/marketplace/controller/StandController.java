package com.startup.marketplace.controller;

import com.startup.marketplace.dto.StandDto;
import com.startup.marketplace.model.Producto;
import com.startup.marketplace.model.Stand;
import com.startup.marketplace.service.RedirectService;
import com.startup.marketplace.service.StandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Endpoints de Stands.
 *
 * PUBLICOS  (sin JWT):
 *   GET  /api/stands               -> Feed principal ordenado por ranking
 *   GET  /api/stands/{slug}        -> Detalle de un stand (vitrineo)
 *   GET  /api/stands/{id}/redirect/{platform} -> Redireccion HTTP 302 (FASE 2)
 *
 * AUTENTICADOS:
 *   GET  /api/stands/mio           -> Stand del usuario autenticado (200 o 204)
 *
 * PROTEGIDOS (requiere JWT + rol EMPRENDEDOR o ADMIN):
 *   POST   /api/stands             -> Crear stand
 *   PUT    /api/stands/{id}        -> Editar stand
 *   POST   /api/stands/{id}/productos           -> Agregar producto
 *   DELETE /api/stands/{id}/productos/{index}   -> Eliminar producto
 */
@RestController
@RequestMapping("/api/stands")
@RequiredArgsConstructor
public class StandController {

    private final StandService standService;
    private final RedirectService redirectService;

    // -------------------------------------------------------------------
    // LECTURA AUTENTICADA — Stand propio del emprendedor
    // -------------------------------------------------------------------

    // Ruta literal "/mio" — Spring la resuelve antes que "/{slug}" (literal > variable)
    @GetMapping("/mio")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Stand> getMiStand(@AuthenticationPrincipal UserDetails userDetails) {
        var result = standService.getMiStand(userDetails.getUsername());
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------
    // LECTURA PUBLICA — Vitrineo
    // -------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<StandDto>> getFeed() {
        return ResponseEntity.ok(standService.getFeed());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<StandDto> getBySlug(@PathVariable String slug) {
        StandDto dto = standService.getBySlug(slug);
        standService.registrarVisita(dto.id);  // llamada externa → proxy @Async activo
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/redirect/{platform}")
    public ResponseEntity<Void> redirect(
            @PathVariable String id,
            @PathVariable String platform) {

        URI target = redirectService.resolve(id, platform);
        redirectService.trackClick(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(target)
                .build();
    }

    // -------------------------------------------------------------------
    // ESCRITURA PROTEGIDA — Solo EMPRENDEDOR o ADMIN
    // -------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMIN')")
    public ResponseEntity<Stand> createStand(
            @RequestBody Stand stand,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                standService.createStand(stand, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMIN')")
    public ResponseEntity<Stand> updateStand(
            @PathVariable String id,
            @RequestBody Stand updates,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                standService.updateStand(id, updates, userDetails.getUsername()));
    }

    @PostMapping("/{id}/productos")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMIN')")
    public ResponseEntity<Stand> addProducto(
            @PathVariable String id,
            @RequestBody Producto producto,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                standService.addProducto(id, producto, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/productos/{index}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMIN')")
    public ResponseEntity<Stand> updateProducto(
            @PathVariable String id,
            @PathVariable int index,
            @RequestBody Producto producto,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                standService.updateProducto(id, index, producto, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}/productos/{index}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMIN')")
    public ResponseEntity<Stand> removeProducto(
            @PathVariable String id,
            @PathVariable int index,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                standService.removeProducto(id, index, userDetails.getUsername()));
    }
}
