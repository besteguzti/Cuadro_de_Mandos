package com.tfg.dashboard.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tfg.dashboard.service.ArubaService;

@Component
public class ArubaScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(ArubaScheduler.class);

    private final ArubaService arubaService;

    public ArubaScheduler(ArubaService arubaService) {
        this.arubaService = arubaService;
    }

    @Scheduled(
            initialDelayString = "${aruba.sync.initial-delay-ms:60000}",
            fixedRateString = "${aruba.sync.fixed-rate-ms:3600000}"
    )
    public void syncAruba() {

        log.info("Sincronizando datos Aruba en MySQL");

        try {

            arubaService.syncAll();
            log.info("Sincronizacion Aruba finalizada");
        } catch (Exception exception) {

            // El scheduler debe registrar
            // el fallo completo para poder
            // diagnosticar tokens, API o
            // base de datos sin detener la
            // aplicacion.
            log.error("Error sincronizando datos Aruba", exception);
        }
    }
}
