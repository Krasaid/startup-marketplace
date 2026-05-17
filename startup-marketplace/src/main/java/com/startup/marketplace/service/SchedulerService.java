package com.startup.marketplace.service;

import com.startup.marketplace.repository.StandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Tareas programadas del ranking semanal.
 *
 * Reset cada lunes a medianoche (00:00:00 zona horaria del servidor).
 * El cron "0 0 0 * * MON" se lee: seg=0 min=0 hora=0 dia=* mes=* diasemana=MON.
 *
 * @EnableScheduling esta en StartupApplication — no duplicar aqui.
 */
@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    private final StandRepository standRepository;

    public SchedulerService(StandRepository standRepository) {
        this.standRepository = standRepository;
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void resetScoresSemanal() {
        log.info("Iniciando reset semanal de scores...");
        standRepository.resetScoresSemanal();
        log.info("Reset semanal de scores completado.");
    }
}