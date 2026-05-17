package com.startup.marketplace.service;

import com.startup.marketplace.dto.StandDto;
import com.startup.marketplace.model.Producto;
import com.startup.marketplace.model.Stand;
import com.startup.marketplace.model.User;
import com.startup.marketplace.repository.StandRepository;
import com.startup.marketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StandService {

    private final StandRepository standRepository;
    private final UserRepository  userRepository;

    // -------------------------------------------------------------------
    // LECTURAS — servidas desde cache Caffeine (RAM), MongoDB solo en miss
    // -------------------------------------------------------------------

    public Optional<Stand> getMiStand(String email) {
        User user = resolveUser(email);
        return standRepository.findByUserId(user.getId());
    }

    @Cacheable("feed")
    public List<StandDto> getFeed() {
        return standRepository
                .findByActivoTrue(Sort.by(Sort.Direction.DESC, "scoreSemanal"))
                .stream()
                .map(StandDto::from)
                .toList();
    }

    @Cacheable(value = "stand", key = "#slug")
    public StandDto getBySlug(String slug) {
        return standRepository.findBySlug(slug)
                .map(StandDto::from)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Stand no encontrado: " + slug));
    }

    // -------------------------------------------------------------------
    // ESCRITURAS — van directo a MongoDB e invalidan cache afectada
    // -------------------------------------------------------------------

    @CacheEvict(value = "feed", allEntries = true)
    public Stand createStand(Stand stand, String email) {
        User user = resolveUser(email);

        if (standRepository.existsBySlug(stand.getSlug())) {
            throw new IllegalStateException(
                    "El slug ya esta en uso: " + stand.getSlug());
        }

        stand.setUserId(user.getId());
        stand.setCreatedAt(Instant.now());
        stand.setUpdatedAt(Instant.now());
        return standRepository.save(stand);
    }

    @Caching(evict = {
        @CacheEvict(value = "feed",  allEntries = true),
        @CacheEvict(value = "stand", key = "#result.slug")
    })
    public Stand updateStand(String standId, Stand updates, String email) {
        Stand existing = findOwnedStand(standId, email);

        existing.setNombre(updates.getNombre());
        existing.setDescripcion(updates.getDescripcion());
        existing.setCategoria(updates.getCategoria());
        existing.setBannerUrl(updates.getBannerUrl());
        existing.setLogoUrl(updates.getLogoUrl());
        existing.setWhatsappUrl(updates.getWhatsappUrl());
        existing.setInstagramUrl(updates.getInstagramUrl());
        existing.setUpdatedAt(Instant.now());

        return standRepository.save(existing);
    }

    // -------------------------------------------------------------------
    // Productos embebidos
    // -------------------------------------------------------------------

    @CacheEvict(value = "stand", key = "#result.slug")
    public Stand addProducto(String standId, Producto producto, String email) {
        Stand stand = findOwnedStand(standId, email);

        if (stand.getProductos().size() >= Stand.MAX_PRODUCTOS) {
            throw new IllegalStateException(
                    "Limite de " + Stand.MAX_PRODUCTOS + " productos alcanzado. " +
                    "Elimina uno antes de agregar otro.");
        }

        stand.getProductos().add(producto);
        stand.setUpdatedAt(Instant.now());
        return standRepository.save(stand);
    }

    @CacheEvict(value = "stand", key = "#result.slug")
    public Stand updateProducto(String standId, int index, Producto productoActualizado, String email) {
        Stand stand = findOwnedStand(standId, email);

        if (index < 0 || index >= stand.getProductos().size()) {
            throw new IllegalArgumentException(
                    "Indice de producto invalido: " + index);
        }

        standRepository.updateProductoByIndex(standId, index, productoActualizado);

        // Recargar desde MongoDB para retornar el estado real post-update
        return standRepository.findById(standId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Stand no encontrado: " + standId));
    }

    @CacheEvict(value = "stand", key = "#result.slug")
    public Stand removeProducto(String standId, int index, String email) {
        Stand stand = findOwnedStand(standId, email);

        if (index < 0 || index >= stand.getProductos().size()) {
            throw new IllegalArgumentException(
                    "Indice de producto invalido: " + index);
        }

        stand.getProductos().remove(index);
        stand.setUpdatedAt(Instant.now());
        return standRepository.save(stand);
    }

    // -------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------

    // Llamar siempre desde el CONTROLLER — self-invocation omite el proxy @Async.
    @Async("asyncExecutor")
    public void registrarVisita(String standId) {
        try {
            standRepository.incrementVisita(standId);
        } catch (Exception e) {
            System.err.println("[StandService] Error al registrar visita en stand "
                    + standId + ": " + e.getMessage());
        }
    }

    private Stand findOwnedStand(String standId, String email) {
        User user = resolveUser(email);
        return standRepository.findByIdAndUserId(standId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Stand no encontrado o sin permisos de edicion"));
    }

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado: " + email));
    }
}
