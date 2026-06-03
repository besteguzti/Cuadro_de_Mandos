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
 * El panel compara relaciónes aparentes entre plataformas, evolución temporal
 * y relaciónes específicas entre indicadores. Las respuestas proceden de
 * snapshots persistidos y no demuestran causalidad directa.
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final TransversalKpiAnalyticsService analyticsService;

    public AnalysisController(TransversalKpiAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/snapshots")
    public List<AnalysisSnapshot> snapshots(@RequestParam(defaultValue = "30d") String period) {
        return analyticsService.getAnalysisSnapshots(period);
    }

    /**
     * Respuesta principal del panel actual. El parametro platform queda como
     * opcional por compatibilidad con versiones anteriores, pero ya no modifica
     * el contenido devuelto.
     */
    @GetMapping("/glpi-platform-relation")
    public OperationalImpactAnalysisResponse glpiPlatformRelation(
            @RequestParam(required = false) String platform,
            @RequestParam(defaultValue = "30d") String period) {

        return analyticsService.getGlpiPlatformRelation(period);
    }

    /**
     * Devuelve puntos de degradación técnica frente a impacto en usuarios para
     * consultas auxiliares del módulo de análisis.
     */
    @GetMapping("/technical-degradation-impact")
    public List<AnalyticsComparePoint> technicalDegradationImpact(
            @RequestParam(defaultValue = "30d") String period) {

        return analyticsService.getTechnicalDegradationImpact(period);
    }

    @GetMapping("/platform-evolution")
    public List<TechnicalTimelinePointDto> platformEvolution(
            @RequestParam(defaultValue = "30d") String period) {

        return analyticsService.getPlatformEvolution(period);
    }
}
