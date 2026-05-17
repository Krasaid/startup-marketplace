package com.startup.marketplace.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Capa de cache — Filosofia "Disco Lento, RAM Rapida".
 *
 * "feed"  → Feed principal (GET /api/stands)
 *           TTL 5 min. Invalidado con @CacheEvict al crear o editar un stand.
 *
 * "stand" → Detalle de stand individual (GET /api/stands/{slug})
 *           TTL 10 min. Invalidado con @CacheEvict al editar ese stand.
 *
 * recordStats() activa metricas en logs — util en dev, sin costo en prod.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats());

        manager.setCacheNames(List.of("feed", "stand"));

        return manager;
    }
}
