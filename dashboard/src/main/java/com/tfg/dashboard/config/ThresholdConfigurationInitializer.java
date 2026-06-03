package com.tfg.dashboard.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.tfg.dashboard.service.KpiConfigurationService;

/**
 * Inicializa la configuración editable de KPIs al arrancar Spring Boot.
 *
 * La inicializacion es idempotente: crea los umbrales y pesos que falten, pero
 * no sobrescribe valores personalizados por el usuario en el panel de
 * configuración.
 */
@Component
public class ThresholdConfigurationInitializer implements ApplicationRunner {

    private final KpiConfigurationService kpiConfigurationService;

    public ThresholdConfigurationInitializer(KpiConfigurationService kpiConfigurationService) {
        this.kpiConfigurationService = kpiConfigurationService;
    }

    @Override
    public void run(ApplicationArguments args) {
        kpiConfigurationService.ensureDefaultConfigurationExists();
    }
}
