package com.startup.marketplace.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Documento EMBEBIDO dentro de Stand.productos[].
 *
 * No tiene @Document propio — se serializa directamente dentro del documento
 * del Stand en MongoDB, garantizando lecturas de un solo viaje (no $lookup).
 *
 * Limite: Stand.MAX_PRODUCTOS (20 items) para mantener documentos livianos.
 */
@Data
public class Producto {

    private String nombre;

    private String descripcion;

    // BigDecimal para precision monetaria (precios en USD — El Salvador)
    private BigDecimal precio;

    private String imagenUrl;

    private boolean disponible = true;
}
