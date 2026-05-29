package com.tfg.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.dto.summary.CitrixSummary;
import com.tfg.dashboard.service.CitrixService;

/**
 * Endpoint REST de Citrix.
 *
 * Citrix se modela como fuente simulada persistida en MySQL. El endpoint de
 * resumen lee el último snapshot para que la página Citrix y el dashboard
 * principal trabajen con la misma captura.
 */
@RestController
@RequestMapping("/citrix")
public class CitrixController {

    private final CitrixService citrixService;

    public CitrixController(CitrixService citrixService) {
        this.citrixService = citrixService;
    }

    /**
     * Devuelve sesiones, Delivery Controllers, logon duration, carga de
     * servidores, errores de inicio e índice de salud Citrix.
     */
    @GetMapping("/summary")
    public CitrixSummary getSummary() {
        return citrixService.getSummary();
    }
}
