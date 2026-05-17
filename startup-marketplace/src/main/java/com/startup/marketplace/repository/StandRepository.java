package com.startup.marketplace.repository;

import com.startup.marketplace.model.Producto;
import com.startup.marketplace.model.Stand;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StandRepository extends MongoRepository<Stand, String> {

    // Feed principal — usamos Sort dinamico en lugar de derived name
    // para mayor claridad y evitar naming ambiguity con campos tipo "scoreSemanal"
    List<Stand> findByActivoTrue(Sort sort);

    Optional<Stand> findBySlug(String slug);

    // Verificacion de ownership: solo el dueno puede editar su stand
    Optional<Stand> findByIdAndUserId(String id, String userId);

    // Lookup por dueno — usado en GET /api/stands/mio
    Optional<Stand> findByUserId(String userId);

    boolean existsBySlug(String slug);

    // Atomic increment de contadores — evita read-modify-write race conditions.
    // Usando @Query + @Update de Spring Data MongoDB 4.x
    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'scoreVisitas': 1, 'scoreSemanal': 1 } }")
    void incrementVisita(String standId);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'scoreClics': 1, 'scoreSemanal': 1 } }")
    void incrementClic(String standId);

    // Actualiza un producto especifico por su indice en el array embebido.
    // 'productos.?1' → Spring sustituye ?1 por el entero antes de parsear el JSON,
    // produciendo e.g. 'productos.0', que es notacion valida de MongoDB para arrays.
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'productos.?1': ?2 } }")
    void updateProductoByIndex(String standId, int index, Producto producto);

    // Reset semanal de scores (llamado por @Scheduled en Fase 2)
    @Query("{ 'activo': true }")
    @Update("{ '$set': { 'scoreSemanal': 0 } }")
    void resetScoresSemanal();
}
