package com.startup.marketplace.dto;

import com.startup.marketplace.model.Producto;
import com.startup.marketplace.model.Stand;

import java.time.Instant;
import java.util.List;

/**
 * DTO de respuesta publica del Stand.
 *
 * Omite intencionalmente:
 * - whatsappUrl  → solo accesible via /redirect/whatsapp
 * - instagramUrl → solo accesible via /redirect/instagram
 *
 * El frontend nunca recibe las URLs reales — solo las consume
 * a traves del endpoint de tracking que retorna HTTP 302.
 */
public class StandDto {

    public String         id;
    public String         userId;
    public String         slug;
    public String         nombre;
    public String         descripcion;
    public String         categoria;
    public String         ciudad;
    public String         bannerUrl;
    public String         logoUrl;
    public long           scoreVisitas;
    public long           scoreClics;
    public long           scoreSemanal;
    public List<Producto> productos;
    public boolean        activo;
    public Instant        createdAt;
    public Instant        updatedAt;

    // Indica al frontend que plataformas tiene disponibles
    // sin revelar la URL real
    public boolean tieneWhatsapp;
    public boolean tieneInstagram;

    public static StandDto from(Stand stand) {
        StandDto dto       = new StandDto();
        dto.id             = stand.getId();
        dto.userId         = stand.getUserId();
        dto.slug           = stand.getSlug();
        dto.nombre         = stand.getNombre();
        dto.descripcion    = stand.getDescripcion();
        dto.categoria      = stand.getCategoria();
        dto.ciudad         = stand.getCiudad();
        dto.bannerUrl      = stand.getBannerUrl();
        dto.logoUrl        = stand.getLogoUrl();
        dto.scoreVisitas   = stand.getScoreVisitas();
        dto.scoreClics     = stand.getScoreClics();
        dto.scoreSemanal   = stand.getScoreSemanal();
        dto.productos      = stand.getProductos();
        dto.activo         = stand.isActivo();
        dto.createdAt      = stand.getCreatedAt();
        dto.updatedAt      = stand.getUpdatedAt();
        dto.tieneWhatsapp  = stand.getWhatsappUrl() != null
                             && !stand.getWhatsappUrl().isBlank();
        dto.tieneInstagram = stand.getInstagramUrl() != null
                             && !stand.getInstagramUrl().isBlank();
        return dto;
    }
}
