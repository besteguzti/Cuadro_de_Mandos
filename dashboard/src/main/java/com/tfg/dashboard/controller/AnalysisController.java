package com.tfg.dashboard.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.dto.AnalyticsComparePoint;
import com.tfg.dashboard.dto.OperationalImpactAnalysisResponse;
import com.tfg.dashboard.dto.TechnicalTimelinePointDto;
import com.tfg.dashboard.model.AnalysisSnapshot;
import com.tfg.dashboard.service.TransversalKpiAnalyticsService;

/**
 * Endpoints del panel "Análisis exploratorio de KPIs transversales".
 *
 * El panel compara presión operativa GLPI con afección técnica de Aruba, Citrix Microsoft 365.
 * Las respuestas proceden de datos de mysql y, si no hay histórico suficiente, genera escenarios de desarrollo marcados.
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final TransversalKpiAnalyticsService analyticsService;

    public AnalysisController(TransversalKpiAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    //Devuelve las capturas históricas que sirven de base para las gráficas.

    @GetMapping("/snapshots")
    public List<AnalysisSnapshot> snapshots(@RequestParam(defaultValue = "30d") String period) {
        return analyticsService.getAnalysisSnapshots(period);
    }

    //Respuesta principal del panel: relación GLPI-plataforma, co-ocurrencia, impacto técnico-operativo y evolución temporal.
     
    @GetMapping("/glpi-platform-relation")
    public OperationalImpactAnalysisResponse glpiPlatformRelation(
            @RequestParam(defaultValue = "aruba") String platform,
            @RequestParam(defaultValue = "30d") String period) {

        return analyticsService.getGlpiPlatformRelation(platform, period);
    }

    /**
     * Devuelve puntos de comparación entre número de tickets GLPI y número de incidencias técnicas detectadas en la plataforma,
     * para evaluar si hay casos de degradación técnica que no se reflejan en tickets o viceversa.
     */
    @GetMapping("/technical-degradation-impact")
    public List<AnalyticsComparePoint> technicalDegradationImpact(
            @RequestParam(defaultValue = "30d") String period) {

        return analyticsService.getTechnicalDegradationImpact(period);
    }

    //Devuelve la evolución temporal conjunta de afección Aruba, Citrix y Microsoft 365.
    
    @GetMapping("/platform-evolution")
    public List<TechnicalTimelinePointDto> platformEvolution(
            @RequestParam(defaultValue = "30d") String period) {

        return analyticsService.getPlatformEvolution(period);
    }
}
