package com.tfg.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.model.GlpiSummary;
import com.tfg.dashboard.service.GlpiService;

@RestController
@RequestMapping("/glpi")
public class GlpiController {

    // =========================
    // Servicio GLPI
    // =========================
    //
    // Contiene la lógica encargada
    // de construir los KPIs
    // relacionados con operación,
    // tickets y actividad.
    //
    private final GlpiService glpiService;

    // =========================
    // Constructor
    // =========================
    //
    // Spring inyecta automáticamente
    // el servicio GLPI.
    //
    public GlpiController(
            GlpiService glpiService
    ) {

        this.glpiService =
                glpiService;
    }

    // =========================
    // Endpoint resumen GLPI
    // =========================
    //
    // Expone el conjunto de KPIs:
    //
    // - Tickets abiertos
    // - Tickets críticos
    // - SLA incumplidos
    // - Actividad diaria
    // - Actividad semanal
    // - Backlog operativo
    //
    // Devuelve el último snapshot
    // guardado en MySQL.
    //
    // Futuro:
    //
    // Sincronización simulada
    // ↓
    // GlpiService
    // ↓
    // GlpiSummary
    //
    // URL:
    //
    // GET /glpi/summary
    //
    @GetMapping("/summary")
    public GlpiSummary getSummary() {

        return glpiService
                .getSummary();
    }

}
