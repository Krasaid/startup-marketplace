package com.startup.marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

/**
 * Habilita @Async y expone un executor de Virtual Threads (Java 21).
 *
 * SimpleAsyncTaskExecutor con virtualThreads=true crea un nuevo Virtual Thread
 * por cada tarea, sin pool — comportamiento idoneo para I/O-bound corto como
 * el incremento de scoreClics en MongoDB.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("vt-async-");
        executor.setVirtualThreads(true);
        return executor;
    }
}