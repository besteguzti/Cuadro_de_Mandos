package com.tfg.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tfg.dashboard.service.ArubaService;
import com.tfg.dashboard.model.ArubaSummary;

@RestController
public class ArubaController {

    private final ArubaService service;

    public ArubaController(ArubaService service) {
        this.service = service;
    }

    @GetMapping("/aruba/summary")
    public ArubaSummary getSummary() {
        return service.getSummary();
    }
}