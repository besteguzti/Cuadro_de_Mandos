package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.client.ArubaApiClient;
import com.tfg.dashboard.entity.KpiEntity;
import com.tfg.dashboard.entity.PlatformEntity;
import com.tfg.dashboard.model.ArubaSummary;
import com.tfg.dashboard.repository.KpiRepository;
import com.tfg.dashboard.repository.PlatformRepository;

@Service
public class ArubaService {

    // Cliente Aruba (mock actualmente)
    private final ArubaApiClient client;

    // Repository plataformas
    private final PlatformRepository platformRepository;

    // Repository KPIs
    private final KpiRepository kpiRepository;

    // Inyección dependencias
    public ArubaService(
            ArubaApiClient client,
            PlatformRepository platformRepository,
            KpiRepository kpiRepository
    ) {
        this.client = client;
        this.platformRepository = platformRepository;
        this.kpiRepository = kpiRepository;
    }

    public ArubaSummary getSummary() {

        // =========================
        // Obtención métricas Aruba
        // =========================

        int wifiUsers = client.getWifiUsers();

        int remoteUsers = client.getRemoteUsers();

        int apsDegraded = client.getApsDegraded();

        int apsSaturated = client.getApsSaturated();

        int vpnApsActive = client.getVpnApsActive();

        int downAps = client.getDownAps();

        int networkTraffic = client.getNetworkTraffic();

        // =========================
        // Cálculo estado global
        // =========================

        String status = calculateStatus(
                apsDegraded,
                apsSaturated,
                downAps
        );

        // =========================
        // Buscar o crear plataforma
        // =========================

        Optional<PlatformEntity> optionalPlatform =
                platformRepository.findByName("Aruba");

        PlatformEntity platform;

        // Si Aruba no existe en BD -> crearla
        if (optionalPlatform.isEmpty()) {

            platform = new PlatformEntity(
                    "Aruba",
                    "Network",
                    "Plataforma de monitorización Aruba"
            );
            platformRepository.save(platform);

        } else {

            // Reutilizar plataforma existente
            platform = optionalPlatform.get();
        }

        // =========================
        // Persistencia KPIs
        // =========================

        saveKpi("wifiUsers", String.valueOf(wifiUsers), platform);

        saveKpi("remoteUsers", String.valueOf(remoteUsers), platform);

        saveKpi("apsDegraded", String.valueOf(apsDegraded), platform);

        saveKpi("apsSaturated", String.valueOf(apsSaturated), platform);

        saveKpi("vpnApsActive", String.valueOf(vpnApsActive), platform);

        saveKpi("downAps", String.valueOf(downAps), platform);

        saveKpi("networkTraffic", String.valueOf(networkTraffic), platform);

        saveKpi("networkStatus", status, platform);

        // =========================
        // Respuesta API REST
        // =========================

        return new ArubaSummary(
                wifiUsers,
                remoteUsers,
                apsDegraded,
                apsSaturated,
                vpnApsActive,
                downAps,
                networkTraffic,
                status
        );
    }

    // Método auxiliar reutilizable para guardar KPIs
    private void saveKpi(
            String name,
            String value,
            PlatformEntity platform
    ) {

        KpiEntity kpi = new KpiEntity(
                name,
                value,
                LocalDateTime.now(),
                platform
        );

        kpiRepository.save(kpi);
    }

    // Cálculo estado general red
    private String calculateStatus(
            int degraded,
            int saturated,
            int downAps
    ) {

        // Estado crítico
        if (downAps > 2 || degraded > 8 || saturated > 6) {
            return "RED";
        }

        // Estado degradado
        else if (downAps > 0 || degraded > 3 || saturated > 3) {
            return "YELLOW";
        }

        // Estado correcto
        else {
            return "GREEN";
        }
    }
}