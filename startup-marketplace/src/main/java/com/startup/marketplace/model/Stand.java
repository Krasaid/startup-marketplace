package com.startup.marketplace.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Coleccion: stands
 *
 * Documento central de StartUP!. Diseno optimizado para la filosofia
 * "Disco Lento, RAM Rapida":
 *
 * - Productos EMBEBIDOS (no en coleccion separada) -> lectura O(1) sin $lookup
 * - scoreSemanal indexado -> ordenamiento del feed sin aggregation pipeline
 * - URLs de contacto guardadas en backend (NO expuestas en frontend) -> tracking
 *
 * Regla de negocio: max MAX_PRODUCTOS productos por stand.
 */
@Data
@Document(collection = "stands")
public class Stand {

    public static final int MAX_PRODUCTOS = 20;

    @Id
    private String id;

    // Referencia al dueno (ID del documento en coleccion users)
    private String userId;

    // URL amigable unica: santa-ana-postres-abuela-rosa
    @Indexed(unique = true)
    private String slug;

    // -------------------------------------------------------------------
    // Identidad del negocio (storytelling del emprendedor)
    // -------------------------------------------------------------------
    private String nombre;
    private String descripcion;     // Historia del emprendedor / pitch
    private String categoria;       // ej: "Gastronomia", "Moda Alternativa", "Artesanias"
    private String ciudad;          // ej: "Santa Ana"
    private String bannerUrl;
    private String logoUrl;

    // -------------------------------------------------------------------
    // Contacto — NUNCA se exponen directamente en el frontend Vue.js.
    // Solo se usan en el endpoint de redireccion /redirect/{platform}
    // que registra el clic antes del HTTP 302.
    // -------------------------------------------------------------------
    private String whatsappUrl;
    private String instagramUrl;

    // -------------------------------------------------------------------
    // Metricas de Ranking Semanal
    // Actualizadas de forma asincrona para no bloquear las respuestas.
    // scoreSemanal es reseteado cada lunes (Fase 2 con @Scheduled).
    // -------------------------------------------------------------------
    private long scoreVisitas  = 0;   // +1 por cada GET /stands/{slug}
    private long scoreClics    = 0;   // +1 por cada GET /stands/{id}/redirect/{platform}
    private long scoreSemanal  = 0;   // scoreVisitas + scoreClics (ventana semanal)

    // -------------------------------------------------------------------
    // Productos embebidos (max MAX_PRODUCTOS)
    // -------------------------------------------------------------------
    private List<Producto> productos = new ArrayList<>();

    // -------------------------------------------------------------------
    // Metadatos
    // -------------------------------------------------------------------
    private boolean activo     = true;
    private Instant createdAt  = Instant.now();
    private Instant updatedAt  = Instant.now();
}
