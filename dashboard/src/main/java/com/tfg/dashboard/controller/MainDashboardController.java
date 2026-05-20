package com.tfg.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.model.MainDashboardSummary;
import com.tfg.dashboard.service.MainDashboardService;

@RestController
@RequestMapping("/dashboard")
public class MainDashboardController {

    // =========================
    // Dashboard principal
    // =========================
    //
    // Este controlador alimenta la
    // vista Principal del dashboard
    // con KPIs transversales.
    //

    private final MainDashboardService mainDashboardService;

    public MainDashboardController(
            MainDashboardService mainDashboardService
    ) {

        this.mainDashboardService =
                mainDashboardService;
    }

    // =========================
    // Resumen transversal
    // =========================
    //
    // GET /dashboard/summary
    //

    @GetMapping("/summary")
    public MainDashboardSummary getSummary() {

        return mainDashboardService.getSummary();
    }
}
