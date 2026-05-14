package com.tfg.dashboard.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.dto.ApInfo;
import com.tfg.dashboard.model.ArubaSummary;
import com.tfg.dashboard.service.ArubaService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/aruba")
public class ArubaController {

    private final ArubaService service;

    public ArubaController(ArubaService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public ArubaSummary getSummary() {

        return service.getSummary();
    }

    @GetMapping("/aps")
    public List<ApInfo> getAps() {

        return service.getApsList();
    }

    @GetMapping("/save-history")
        public String saveHistory() {

            service.saveAccessPointSnapshot();

            return "Histórico guardado";
    }
}