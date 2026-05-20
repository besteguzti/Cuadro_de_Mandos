package com.tfg.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.model.CitrixSummary;
import com.tfg.dashboard.service.CitrixService;

@RestController
@RequestMapping("/citrix")
public class CitrixController {

    // =========================
    // Servicio Citrix
    // =========================
    //
    // Contiene la lógica de negocio
    // para generar los KPIs Citrix.
    //
    private final CitrixService citrixService;

    // =========================
    // Constructor
    // =========================
    //
    // Spring inyecta automáticamente
    // CitrixService mediante
    // inyección por constructor.
    //
    public CitrixController(
            CitrixService citrixService
    ) {

        this.citrixService =
                citrixService;
    }

    // =========================
    // Endpoint resumen Citrix
    // =========================
    //
    // Expone los KPIs principales
    // del entorno Citrix.
    //
    // Devuelve el último snapshot
    // guardado en MySQL. La generación
    // simulada queda limitada al
    // proceso de sincronización.
    //
    // URL:
    // GET /citrix/summary
    //
    @GetMapping("/summary")
    public CitrixSummary getSummary() {

        return citrixService
                .getSummary();
    }
}
