package com.tfg.dashboard.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.dto.ArubaApInfo;
import com.tfg.dashboard.dto.ArubaSwitchInfo;
import com.tfg.dashboard.dto.ArubaWifiClientInfo;
import com.tfg.dashboard.model.AccessPoint;
import com.tfg.dashboard.model.ArubaSummary;
import com.tfg.dashboard.model.ArubaSwitch;
import com.tfg.dashboard.model.ArubaSwitchClientUsage;
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
    public List<ArubaApInfo> getAps() {

        return service.getApsList();
    }

    @GetMapping("/stored-aps")
    public List<AccessPoint> getStoredAps() {

        return service.getStoredAccessPoints();
    }

    @GetMapping("/switches")
    public List<ArubaSwitchInfo> getSwitches() {

        return service.getSwitchesList();
    }

    @GetMapping("/stored-switches")
    public List<ArubaSwitch> getStoredSwitches() {

        return service.getStoredSwitches();
    }

    @GetMapping("/switch-client-usage")
    public List<ArubaSwitchClientUsage> getSwitchClientUsage() {

        return service.getSwitchClientUsage();
    }

    @GetMapping("/wifi-clients")
    public List<ArubaWifiClientInfo> getWifiClients() {

        return service.getWifiClientsList();
    }

    @GetMapping("/wired-clients")
    public List<ArubaWifiClientInfo> getWiredClients() {

        return service.getWiredClientsList();
    }

    @GetMapping("/wifi-clients/diagnostics")
    public Map<String, Object> getWifiClientDiagnostics() {

        return service.getWifiClientsDiagnostics();
    }

    @PostMapping("/sync-aps")
        public String syncAps() {

            service.syncAccessPoints();

            return "APs sincronizados";
    }

    @PostMapping("/sync-switches")
        public String syncSwitches() {

            service.syncSwitches();

            return "Switches sincronizados";
    }

    @PostMapping("/sync-switch-client-usage")
        public String syncSwitchClientUsage() {

            service.syncSwitchClientUsage();

            return "Uso de clientes cableados sincronizado";
    }
}
