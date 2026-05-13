package com.tfg.dashboard.service;

import com.tfg.dashboard.model.KpiTrend;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.tfg.dashboard.entity.KpiEntity;
import com.tfg.dashboard.repository.KpiRepository;
import com.tfg.dashboard.model.KpiAnomaly;

@Service
public class KpiService {

    // Repository acceso BD
    private final KpiRepository kpiRepository;

    // Inyección dependencias
    public KpiService(KpiRepository kpiRepository) {
        this.kpiRepository = kpiRepository;
    }

    // Obtener todos los KPIs almacenados
    public List<KpiEntity> getAllKpis() {

        return kpiRepository.findAll();
    }

    public List<KpiEntity> getLatestKpis() {

        return kpiRepository.findTop100ByOrderByCreatedAtDesc();
    }

    public List<KpiEntity> getKpisByName(String name) {

        return kpiRepository.findByNameOrderByCreatedAtDesc(name);
    }

    public KpiTrend getTrend(String name) {

        // Obtener los 2 últimos registros de la métrica
        List<KpiEntity> kpis =
            kpiRepository.findTop2ByNameOrderByCreatedAtDesc(name);

        // Validación mínima
        if (kpis.size() < 2) {

            return new KpiTrend(
                name,
                0,
                0,
                "NOT_ENOUGH_DATA"
            );
        }

        // KPI más reciente
        KpiEntity current = kpis.get(0);

        // KPI anterior
        KpiEntity previous = kpis.get(1);

        // Convertir valores String -> int
        int currentValue = Integer.parseInt(current.getValue());

        int previousValue = Integer.parseInt(previous.getValue());

        String trend;

        // Comparación tendencia
        if (currentValue > previousValue) {

            trend = "UP";

        } else if (currentValue < previousValue) {

            trend = "DOWN";

        } else {
            trend = "STABLE";
        }

        // Construcción DTO resultado
        return new KpiTrend(
            name,
            currentValue,
            previousValue,
            trend
        );
    }

    public List<KpiAnomaly> getAnomalies() {

        // Obtener últimos KPIs
        List<KpiEntity> latestKpis = kpiRepository.findTop100ByOrderByCreatedAtDesc();

        // Lista anomalías detectadas
        List<KpiAnomaly> anomalies = new ArrayList<>();

        // Analizar cada KPI
        for (KpiEntity kpi : latestKpis) {

            String metric = kpi.getName();

            // Ignorar KPIs no numéricos
            if (metric.equals("networkStatus")) {
                continue;
            }

            int value = Integer.parseInt(kpi.getValue());

            // =========================
            // Reglas detección anomalías
            // =========================

            // APs caídos
            if (metric.equals("downAps") && value > 2) {

                anomalies.add(
                    new KpiAnomaly(
                            metric,
                            value,
                            "HIGH"
                    )
                );
            }

            // APs saturados
            if (metric.equals("apsSaturated") && value > 5) {

                anomalies.add(
                    new KpiAnomaly(
                            metric,
                            value,
                            "MEDIUM"
                    )
                );
            }

            // Tráfico elevado
            if (metric.equals("networkTraffic") && value > 1400) {

                anomalies.add(
                    new KpiAnomaly(
                            metric,
                            value,
                            "MEDIUM"
                    )
                );
            }

            // APs degradados
            if (metric.equals("apsDegraded") && value > 7) {

                anomalies.add(
                    new KpiAnomaly(
                            metric,
                            value,
                            "HIGH"
                    )
                );
            }
        }

        return anomalies;
    }

}