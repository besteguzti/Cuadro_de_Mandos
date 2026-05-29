package com.tfg.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.dto.ExecutiveSummaryDto;
import com.tfg.dashboard.service.ExecutiveSummaryService;

/**
 * Publica endpoints del dashboard principal bajo el prefijo /api.
 *
 * Actualmente expone el diagnóstico ejecutivo, una lectura orientada a
 * responsables IT construida a partir de los KPIs ya calculados por backend.
 */
@RestController
@RequestMapping("/api/dashboard")
public class ApiDashboardController {

    private final ExecutiveSummaryService executiveSummaryService;

    public ApiDashboardController(ExecutiveSummaryService executiveSummaryService) {
        this.executiveSummaryService = executiveSummaryService;
    }

    
    //Devuelve servicio afectado, plataforma principal, prioridad, tendencia y primera acción recomendada. 
     
    @GetMapping("/executive-summary")
    public ExecutiveSummaryDto getExecutiveSummary() {
        return executiveSummaryService.getExecutiveSummary();
    }
}
