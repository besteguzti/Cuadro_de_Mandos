package com.tfg.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.dto.ArubaNetworkStatusDto;
import com.tfg.dashboard.service.ArubaService;

@RestController
@RequestMapping("/api/aruba")
public class ApiArubaController {

    private final ArubaService service;

    public ApiArubaController(ArubaService service) {
        this.service = service;
    }

    @GetMapping("/network-status")
    public ArubaNetworkStatusDto getNetworkStatus() {

        return service.getNetworkStatus();
    }
}
