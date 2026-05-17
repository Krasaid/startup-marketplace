package com.startup.marketplace.service;

import com.startup.marketplace.model.Stand;
import com.startup.marketplace.repository.StandRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * Motor de Redireccion — Fase 2.
 *
 * Flujo por peticion:
 *   1. resolve()    — sincrono: busca el stand y retorna la URI de destino.
 *   2. trackClick() — asincrono (@Async): incrementa scoreClics + scoreSemanal
 *                     en MongoDB SIN bloquear la respuesta HTTP 302.
 *
 * La separacion en dos metodos garantiza que el proxy de Spring intercepte
 * trackClick() correctamente (llamado desde el controller, no desde esta clase).
 */
@Service
public class RedirectService {

    private final StandRepository standRepository;

    public RedirectService(StandRepository standRepository) {
        this.standRepository = standRepository;
    }

    /**
     * Resuelve la URL real de la plataforma solicitada.
     *
     * @param standId  ID del stand en MongoDB
     * @param platform "whatsapp" | "instagram"
     * @return URI hacia la que se emitira el HTTP 302
     * @throws IllegalArgumentException si el stand no existe, la plataforma
     *         no esta soportada, o la URL no esta configurada en el stand
     */
    public URI resolve(String standId, String platform) {
        Stand stand = standRepository.findById(standId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Stand no encontrado: " + standId));

        String url = switch (platform.toLowerCase()) {
            case "whatsapp"  -> stand.getWhatsappUrl();
            case "instagram" -> stand.getInstagramUrl();
            default -> throw new IllegalArgumentException(
                    "Plataforma no soportada: " + platform);
        };

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "El stand no tiene configurada la plataforma: " + platform);
        }

        return URI.create(url);
    }

    /**
     * Fire-and-forget: incrementa scoreClics y scoreSemanal via $inc atomico.
     * Ejecutado en un Virtual Thread (AsyncConfig) — no bloquea la respuesta.
     */
    @Async("asyncExecutor")
    public void trackClick(String standId) {
        standRepository.incrementClic(standId);
    }
}