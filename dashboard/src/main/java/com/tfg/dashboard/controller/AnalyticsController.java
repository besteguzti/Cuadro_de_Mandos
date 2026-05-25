package com.tfg.dashboard.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.dto.AnalyticsCompareResponse;
import com.tfg.dashboard.dto.TransversalKpiCatalogItem;
import com.tfg.dashboard.service.TransversalKpiAnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final TransversalKpiAnalyticsService analyticsService;

    public AnalyticsController(
            TransversalKpiAnalyticsService analyticsService
    ) {

        this.analyticsService =
                analyticsService;
    }

    // =========================
    // Catalogo KPIs transversales
    // =========================
    //
    // Alimenta la pagina Analisis
    // con indicadores disponibles,
    // valores actuales y relaciones
    // recomendadas.
    //

    @GetMapping("/transversal-kpis")
    public List<TransversalKpiCatalogItem> getTransversalKpis() {

        return analyticsService.getTransversalKpis();
    }

    // =========================
    // Comparacion exploratoria
    // =========================
    //
    // Devuelve puntos X/Y para una
    // grafica de dispersion entre
    // dos KPIs transversales.
    //

    @GetMapping("/compare")
    public AnalyticsCompareResponse compare(
            @RequestParam String kpiX,
            @RequestParam String kpiY,
            @RequestParam(defaultValue = "30d") String period
    ) {

        return analyticsService.compare(kpiX, kpiY, period);
    }
}
