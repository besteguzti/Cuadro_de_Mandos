package com.tfg.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.model.Microsoft365Summary;
import com.tfg.dashboard.service.Microsoft365Service;

@RestController
@RequestMapping("/microsoft365")
public class Microsoft365Controller {

    // =========================
    // Servicio Microsoft 365
    // =========================
    //
    // Contiene la lógica de negocio
    // para generar los KPIs de
    // Microsoft 365.
    //
    private final Microsoft365Service
            microsoft365Service;

    // =========================
    // Constructor
    // =========================
    //
    // Spring inyecta automáticamente
    // Microsoft365Service mediante
    // inyección por constructor.
    //
    public Microsoft365Controller(
            Microsoft365Service microsoft365Service
    ) {

        this.microsoft365Service =
                microsoft365Service;
    }

    // =========================
    // Endpoint resumen Microsoft 365
    // =========================
    //
    // Expone los KPIs principales:
    //
    // - usuarios activos
    // - licencias no asignadas
    // - estado servicios
    // - seguridad identidad
    // - aplicaciones
    // - dispositivos Intune
    // - riesgo operativo
    //
    // Devuelve el último snapshot
    // guardado en MySQL. La generación
    // simulada queda limitada al
    // proceso de sincronización.
    //
    // URL:
    // GET /microsoft365/summary
    //
    @GetMapping("/summary")
    public Microsoft365Summary getSummary() {

        return microsoft365Service
                .getSummary();
    }
}
