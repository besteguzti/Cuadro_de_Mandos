package com.tfg.dashboard.controller;


import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.tfg.dashboard.entity.KpiEntity;
import com.tfg.dashboard.service.KpiService;
import com.tfg.dashboard.model.KpiTrend;
import com.tfg.dashboard.model.KpiAnomaly;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class KpiController {

    // Service KPI
    private final KpiService kpiService;

    // Inyección dependencias
    public KpiController(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    // Endpoint REST:
    // GET /kpis
    @GetMapping("/kpis")
    public List<KpiEntity> getAllKpis() {

        return kpiService.getAllKpis();
    }

    @GetMapping("/kpis/latest")
    public List<KpiEntity> getLatestKpis() {

        return kpiService.getLatestKpis();
    }

    @GetMapping("/kpis/name/{name}")
    public List<KpiEntity> getKpisByName(@PathVariable String name) {
        return kpiService.getKpisByName(name);
    }
    @GetMapping("/kpis/trend/{name}")
    public KpiTrend getTrend(@PathVariable String name) {
        return kpiService.getTrend(name);
    }
    @GetMapping("/kpis/anomalies")
    public List<KpiAnomaly> getAnomalies() {
        return kpiService.getAnomalies();
    }

}