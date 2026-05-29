package com.tfg.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.dto.summary.GlpiSummary;
import com.tfg.dashboard.service.GlpiService;

/**
 * Endpoint REST de GLPI.
 *
 * GLPI representa la consecuencia operativa del entorno: tickets abiertos,
 * críticos, SLA y capacidad de cierre. Los datos se generan en backend y se
 * consultan desde el último snapshot persistido.
 */
@RestController
@RequestMapping("/glpi")
public class GlpiController {

    private final GlpiService glpiService;

    public GlpiController(GlpiService glpiService) {
        this.glpiService = glpiService;
    }

    /**
     * Devuelve KPIs operativos e índice de salud GLPI normalizado.
     */
    @GetMapping("/summary")
    public GlpiSummary getSummary() {
        return glpiService.getSummary();
    }
}
