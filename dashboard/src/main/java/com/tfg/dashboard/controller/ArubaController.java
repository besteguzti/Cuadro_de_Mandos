package com.tfg.dashboard.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.dto.ArubaApInfo;
import com.tfg.dashboard.dto.ArubaNetworkStatusDto;
import com.tfg.dashboard.dto.ArubaSwitchInfo;
import com.tfg.dashboard.dto.ArubaWifiClientInfo;
import com.tfg.dashboard.model.AccessPoint;
import com.tfg.dashboard.dto.summary.ArubaSummary;
import com.tfg.dashboard.model.ArubaSwitch;
import com.tfg.dashboard.model.ArubaSwitchClientUsage;
import com.tfg.dashboard.service.ArubaService;

/**
 * Controlador de Aruba Central.
 *
 * Indica el estado de red normalizado y endpoints de consulta/sincronización manual. La integración real y la
 * persistencia se mantienen en {@link ArubaService} y servicios especializados.
 */
@RestController
@RequestMapping("/aruba")
public class ArubaController {

    private final ArubaService service;

    public ArubaController(ArubaService service) {
        this.service = service;
    }

    // Devuelve el resumen agregado que consume la vista Aruba.

    @GetMapping("/summary")
    public ArubaSummary getSummary() {
        return service.getSummary();
    }

    // Devuelve el porcentaje de afectación de red Aruba, color global y motivos.
     
    @GetMapping("/network-status")
    public ArubaNetworkStatusDto getNetworkStatus() {
        return service.getNetworkStatus();
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
        return "Uso de interfaces down sincronizado";
    }

    @PostMapping("/sync-all")
    public String syncAll() {
        service.syncAll();
        return "Datos Aruba sincronizados";
    }
}
