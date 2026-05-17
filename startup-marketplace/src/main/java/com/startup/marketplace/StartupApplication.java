package com.startup.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * StartUP! — Motor de Descubrimiento de Emprendedores Locales
 *
 * Arquitectura: "Disco Lento, RAM Rapida"
 * - Virtual Threads activos via spring.threads.virtual.enabled=true
 * - Feed servido desde cache (Caffeine / Redis en Fase 4)
 * - @EnableScheduling para reset del ranking semanal (Fase 2+)
 */
@SpringBootApplication
@EnableScheduling
public class StartupApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartupApplication.class, args);
    }
}
