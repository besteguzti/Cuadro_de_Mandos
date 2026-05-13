package com.tfg.dashboard.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tfg.dashboard.service.ArubaService;

@Component
public class ArubaScheduler {

    // Service reutilizado
    private final ArubaService arubaService;

    // Inyección dependencias
    public ArubaScheduler(ArubaService arubaService) {
        this.arubaService = arubaService;
    }

    // Ejecutar automáticamente cada 30 segundos
    @Scheduled(fixedRate = 30000)
    public void collectMetrics() {

        System.out.println("Recogiendo métricas Aruba...");

        arubaService.getSummary();
    }
}