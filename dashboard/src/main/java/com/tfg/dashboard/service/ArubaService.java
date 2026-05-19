package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tfg.dashboard.client.ArubaApiClient;
import com.tfg.dashboard.dto.ArubaApInfo;
import com.tfg.dashboard.dto.ArubaSwitchInfo;
import com.tfg.dashboard.dto.ArubaWifiClientInfo;
import com.tfg.dashboard.model.AccessPoint;
import com.tfg.dashboard.model.ArubaSummary;
import com.tfg.dashboard.model.ArubaSwitch;
import com.tfg.dashboard.model.ArubaSwitchClientUsage;
import com.tfg.dashboard.repository.AccessPointRepository;
import com.tfg.dashboard.repository.ArubaSwitchClientUsageRepository;
import com.tfg.dashboard.repository.ArubaSwitchRepository;

@Service
public class ArubaService {

    private static final Logger log =
            LoggerFactory.getLogger(ArubaService.class);

    private static final int UNDERUSED_SWITCH_CLIENT_LIMIT = 10;

    private final ArubaApiClient client;

    private final AccessPointRepository
            accessPointRepository;

    private final ArubaSwitchRepository
            arubaSwitchRepository;

    private final ArubaSwitchClientUsageRepository
            switchClientUsageRepository;

    public ArubaService(
            ArubaApiClient client,
            AccessPointRepository accessPointRepository,
            ArubaSwitchRepository arubaSwitchRepository,
            ArubaSwitchClientUsageRepository switchClientUsageRepository
    ) {

        this.client = client;
        this.accessPointRepository =
                accessPointRepository;
        this.arubaSwitchRepository =
                arubaSwitchRepository;
        this.switchClientUsageRepository =
                switchClientUsageRepository;
    }

    // =========================================
    // RESUMEN GENERAL
    // =========================================

    public ArubaSummary getSummary() {

        List<ArubaApInfo> aps =
                client.getApsList();

        syncAccessPoints(
                aps
        );

        List<ArubaSwitchInfo> switches =
                client.getSwitchesList();

        syncSwitches(
                switches
        );

        List<ArubaWifiClientInfo> wifiClients =
                client.getWifiClientsList();

        List<ArubaWifiClientInfo> wiredClients =
                client.getWiredClientsList();

        syncSwitchClientUsage(
                wiredClients,
                switches
        );

        List<ArubaSwitchClientUsage> underusedSwitches =
                getUnderusedSwitches();

        logWifiClientBreakdown(
                wifiClients
        );

        
        // =========================
        // KPIs básicos
        // =========================

        int totalAps =
                aps.size();

        int upAps =
                (int) aps.stream()

                .filter(ap ->
                        ap.getStatus() != null
                        && ap.getStatus()
                                .equalsIgnoreCase("Up"))

                .count();

        int downAps =
                totalAps - upAps;



        int totalSites =
                (int) aps.stream()

                .map(ArubaApInfo::getSite)

                .filter(site ->
                        site != null
                        && !site.isBlank())

                .distinct()

                .count();

        int totalSwarms =
                (int) aps.stream()

                .map(ArubaApInfo::getSwarmName)

                .filter(sw ->
                        sw != null
                        && !sw.isBlank())

                .distinct()

                .count();

        // =========================
        // Firmware swarms
        // =========================

        JsonNode firmwareSwarms = client.getFirmwareSwarms();

        int firmwareOutdated = 0;

        if (firmwareSwarms != null) {

            JsonNode swarms =
                    firmwareSwarms.get("swarms");

            if (swarms != null
                    && swarms.isArray()) {

                for (JsonNode swarm : swarms) {

                    String swarmName =
                            swarm.path("swarm_name")
                                 .asText();

                    String state =
                            swarm.path("status")
                                 .path("state")
                                 .asText();

                    

                    boolean upgradeRequired =
                            state.trim()
                                 .equalsIgnoreCase(
                                        "UPGRADE_REQUIRED"
                                 );

                    if (upgradeRequired) {

                        firmwareOutdated++;
                    }
                }

               
            }
        }

        // =========================
        // APs sin IP pública
        // =========================

        int apsWithoutPublicIp =
                (int) aps.stream()

                .filter(ap ->
                        ap.getPublicIpAddress() == null
                        || ap.getPublicIpAddress()
                                .isBlank())

                .count();

        int totalSwitches =
                switches.size();

        int downSwitches =
                (int) switches.stream()
                        .filter(switchInfo ->
                                switchInfo.getDeviceStatus() == null
                                || !switchInfo.getDeviceStatus()
                                        .equalsIgnoreCase("Up"))
                        .count();

        int switchesFirmwareUpgradeRequired =
                (int) switches.stream()
                        .filter(ArubaSwitchInfo::isUpgradeRequired)
                        .count();

        int totalWifiClients =
                wifiClients.size();

        int mutualiaApsClients =
                countClientsByGroup(
                        wifiClients,
                        "MUTUALIA-APs"
                );

        int mutualiaWifiClients =
                countClientsByGroup(
                        wifiClients,
                        "MUTUALIA-WIFI"
                );

        int mutualiaLangileakClients =
                countClientsByWifiNetwork(
                        wifiClients,
                        "MUTUALIA_LANGILEAK"
                );

        int mutualiaClients =
                countClientsByWifiNetwork(
                        wifiClients,
                        "MUTUALIA"
                );

        int mutualiaRedInternaClients =
                countClientsByWifiNetwork(
                        wifiClients,
                        "MUTUALIA_RED_INTERNA"
                );

        int mutualiaRedExternaClients =
                countClientsByWifiNetwork(
                        wifiClients,
                        "MUTUALIA_RED_EXTERNA"
                );

        int mutualiaKorporatiboaClients =
                countClientsByWifiNetwork(
                        wifiClients,
                        "MUTUALIA_KORPORATIBOA"
                );

        int wifiPacsClients =
                countClientsByWifiNetwork(
                        wifiClients,
                        "WIFI_PACs"
                );

        int mutVideoClients =
                countClientsByWifiNetwork(
                        wifiClients,
                        "MUT_VIDEO"
                );

        // =========================
        // Estado global red
        // =========================

        String networkStatus = "GREEN";

        if (downAps > 10
                || firmwareOutdated > 5
                || downSwitches > 0
                || switchesFirmwareUpgradeRequired > 0) {

            networkStatus = "RED";

        } else if (downAps > 0
                || firmwareOutdated > 0) {

            networkStatus = "YELLOW";
        }

        // =========================
        // APs inactivos 3 meses
        // =========================

        LocalDateTime limitDate =
                LocalDateTime.now()
                             .minusMonths(3);

        long inactiveAps =
                accessPointRepository
                        .countBySerialIsNotNullAndLastSeenAtBefore(
                                limitDate
                        );

        // =========================
        // Construcción summary
        // =========================

        ArubaSummary summary =
                new ArubaSummary();

        summary.setTotalAps(totalAps);

        summary.setUpAps(upAps);

        summary.setDownAps(downAps);

        summary.setTotalSites(totalSites);

        summary.setTotalSwarms(totalSwarms);

        summary.setFirmwareOutdated(
                firmwareOutdated
        );

        summary.setApsWithoutPublicIp(
                apsWithoutPublicIp
        );

        summary.setDownAps(
                (int) downAps
        );

        summary.setInactiveAps(
                (int) inactiveAps
        );

        summary.setNetworkStatus(
                networkStatus
        );

        summary.setTotalSwitches(
                totalSwitches
        );

        summary.setDownSwitches(
                downSwitches
        );

        summary.setSwitchesFirmwareUpgradeRequired(
                switchesFirmwareUpgradeRequired
        );

        summary.setUnderusedSwitches(
                underusedSwitches
        );

        summary.setTotalWifiClients(
                totalWifiClients
        );

        summary.setMutualiaApsClients(
                mutualiaApsClients
        );

        summary.setMutualiaWifiClients(
                mutualiaWifiClients
        );

        summary.setMutualiaLangileakClients(
                mutualiaLangileakClients
        );

        summary.setMutualiaClients(
                mutualiaClients
        );

        summary.setMutualiaRedInternaClients(
                mutualiaRedInternaClients
        );

        summary.setMutualiaRedExternaClients(
                mutualiaRedExternaClients
        );

        summary.setMutualiaKorporatiboaClients(
                mutualiaKorporatiboaClients
        );

        summary.setWifiPacsClients(
                wifiPacsClients
        );

        summary.setMutVideoClients(
                mutVideoClients
        );

        return summary;
    }

    // =========================================
    // LISTADO APs
    // =========================================

    public List<ArubaApInfo> getApsList() {

        return client.getApsList();
    }

    public List<AccessPoint> getStoredAccessPoints() {

        return accessPointRepository.findAll();
    }

    public List<ArubaSwitchInfo> getSwitchesList() {

        return client.getSwitchesList();
    }

    public List<ArubaSwitch> getStoredSwitches() {

        return arubaSwitchRepository.findAll();
    }

    public List<ArubaSwitchClientUsage> getSwitchClientUsage() {

        return switchClientUsageRepository.findAll();
    }

    public List<ArubaSwitchClientUsage> getUnderusedSwitches() {

        return switchClientUsageRepository
                .findByWiredClientsLessThanOrderByWiredClientsAscAssociatedDeviceAsc(
                        UNDERUSED_SWITCH_CLIENT_LIMIT
                );
    }

    public List<ArubaWifiClientInfo> getWifiClientsList() {

        return client.getWifiClientsList();
    }

    public List<ArubaWifiClientInfo> getWiredClientsList() {

        return client.getWiredClientsList();
    }

    public Map<String, Object> getWifiClientsDiagnostics() {

        List<ArubaWifiClientInfo> clients =
                client.getWifiClientsList();

        Map<String, Object> diagnostics =
                new LinkedHashMap<>();

        diagnostics.put(
                "total",
                clients.size()
        );

        diagnostics.put(
                "groups",
                countByGroup(clients)
        );

        diagnostics.put(
                "mutualiaWifiNetworks",
                countByMutualiaWifiNetwork(clients)
        );

        diagnostics.put(
                "sample",
                clients.stream()
                        .limit(5)
                        .toList()
        );

        return diagnostics;
    }

    // =========================================
    // SINCRONIZAR APS
    // =========================================

    public void syncAccessPoints() {

        List<ArubaApInfo> aps =
                client.getApsList();

        syncAccessPoints(
                aps
        );
    }

    public void syncSwitches() {

        List<ArubaSwitchInfo> switches =
                client.getSwitchesList();

        syncSwitches(
                switches
        );
    }

    public void syncSwitchClientUsage() {

        List<ArubaWifiClientInfo> wiredClients =
                client.getWiredClientsList();

        syncSwitchClientUsage(
                wiredClients,
                client.getSwitchesList()
        );
    }

    private void syncAccessPoints(
            List<ArubaApInfo> aps
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        for (ArubaApInfo ap : aps) {

            String serial =
                    ap.getSerial();

            if (serial == null
                    || serial.isBlank()) {

                continue;
            }

            AccessPoint entity =
                    accessPointRepository
                            .findBySerial(serial)
                            .orElseGet(AccessPoint::new);

            entity.setName(
                    ap.getName()
            );

            entity.setStatus(
                    ap.getStatus()
            );

            entity.setIpAddress(
                    ap.getIpAddress()
            );

            entity.setPublicIpAddress(
                    ap.getPublicIpAddress()
            );

            entity.setSerial(
                    serial
            );

            entity.setSite(
                    ap.getSite()
            );

            entity.setFirmwareVersion(
                    ap.getFirmwareVersion()
            );

            entity.setMacaddr(
                    ap.getMacaddr()
            );

            entity.setSwarmName(
                    ap.getSwarmName()
            );

            if (entity.getFirstSeenAt() == null) {

                entity.setFirstSeenAt(
                        now
                );
            }

            entity.setLastSeenAt(
                    now
            );

            accessPointRepository
                    .save(entity);
        }
    }

    private void syncSwitches(
            List<ArubaSwitchInfo> switches
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        for (ArubaSwitchInfo switchInfo : switches) {

            String serial =
                    switchInfo.getSerial();

            if (serial == null
                    || serial.isBlank()) {

                continue;
            }

            ArubaSwitch entity =
                    arubaSwitchRepository
                            .findBySerial(serial)
                            .orElseGet(ArubaSwitch::new);

            entity.setSerial(
                    serial
            );

            entity.setMacAddress(
                    switchInfo.getMacAddress()
            );

            entity.setHostname(
                    switchInfo.getHostname()
            );

            entity.setModel(
                    switchInfo.getModel()
            );

            entity.setDeviceStatus(
                    switchInfo.getDeviceStatus()
            );

            entity.setUpgradeRequired(
                    switchInfo.isUpgradeRequired()
            );

            entity.setStatusState(
                    switchInfo.getStatusState()
            );

            if (entity.getFirstSeenAt() == null) {

                entity.setFirstSeenAt(
                        now
                );
            }

            entity.setLastSeenAt(
                    now
            );

            arubaSwitchRepository
                    .save(entity);
        }
    }

    private void syncSwitchClientUsage(
            List<ArubaWifiClientInfo> wiredClients
    ) {

        syncSwitchClientUsage(
                wiredClients,
                List.of()
        );
    }

    private void syncSwitchClientUsage(
            List<ArubaWifiClientInfo> wiredClients,
            List<ArubaSwitchInfo> switches
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        Map<String, ArubaSwitchClientUsage> usageByDevice =
                new LinkedHashMap<>();

        for (ArubaSwitchInfo switchInfo : switches) {

            String serial =
                    switchInfo.getSerial();

            if (serial == null
                    || serial.isBlank()) {

                continue;
            }

            ArubaSwitchClientUsage usage =
                    new ArubaSwitchClientUsage();

            usage.setAssociatedDevice(serial);
            usage.setAssociatedDeviceName(
                    switchInfo.getHostname()
            );
            usage.setAssociatedDeviceMac(
                    switchInfo.getMacAddress()
            );

            usageByDevice.put(serial, usage);
        }

        for (ArubaWifiClientInfo wiredClient : wiredClients) {

            String associatedDevice =
                    wiredClient.getAssociatedDevice();

            if (associatedDevice == null
                    || associatedDevice.isBlank()) {

                continue;
            }

            ArubaSwitchClientUsage usage =
                    usageByDevice.computeIfAbsent(
                            associatedDevice,
                            key -> {
                                ArubaSwitchClientUsage newUsage =
                                        new ArubaSwitchClientUsage();

                                newUsage.setAssociatedDevice(key);

                                return newUsage;
                            }
                    );

            usage.setAssociatedDeviceName(
                    firstNotBlank(
                            usage.getAssociatedDeviceName(),
                            wiredClient.getAssociatedDeviceName()
                    )
            );

            usage.setAssociatedDeviceMac(
                    firstNotBlank(
                            usage.getAssociatedDeviceMac(),
                            wiredClient.getAssociatedDeviceMac()
                    )
            );

            usage.setWiredClients(
                    usage.getWiredClients() + 1
            );
        }

        for (ArubaSwitchClientUsage existing
                : switchClientUsageRepository.findAll()) {

            if (!usageByDevice.containsKey(
                    existing.getAssociatedDevice())) {

                existing.setWiredClients(0);
                existing.setUpdatedAt(now);

                switchClientUsageRepository.save(existing);
            }
        }

        for (ArubaSwitchClientUsage aggregate
                : usageByDevice.values()) {

            ArubaSwitchClientUsage entity =
                    switchClientUsageRepository
                            .findByAssociatedDevice(
                                    aggregate.getAssociatedDevice())
                            .orElseGet(ArubaSwitchClientUsage::new);

            entity.setAssociatedDevice(
                    aggregate.getAssociatedDevice()
            );

            entity.setAssociatedDeviceName(
                    aggregate.getAssociatedDeviceName()
            );

            entity.setAssociatedDeviceMac(
                    aggregate.getAssociatedDeviceMac()
            );

            entity.setWiredClients(
                    aggregate.getWiredClients()
            );

            entity.setUpdatedAt(now);

            switchClientUsageRepository.save(entity);
        }
    }

    private int countClientsByGroup(
            List<ArubaWifiClientInfo> clients,
            String groupName
    ) {

        return (int) clients.stream()
                .filter(clientInfo ->
                        normalize(groupName).equals(
                                normalize(clientInfo.getGroupName())))
                .count();
    }

    private int countClientsByWifiNetwork(
            List<ArubaWifiClientInfo> clients,
            String network
    ) {

        return (int) clients.stream()
                .filter(clientInfo ->
                        "MUTUALIA-WIFI".equals(
                                normalize(clientInfo.getGroupName())))
                .filter(clientInfo ->
                        normalize(network).equals(
                                normalize(clientInfo.getNetwork())))
                .count();
    }

    private void logWifiClientBreakdown(
            List<ArubaWifiClientInfo> clients
    ) {

        Map<String, Long> groups =
                countByGroup(clients);

        Map<String, Long> networks =
                countByMutualiaWifiNetwork(clients);

        log.info(
                "Clientes WiFi detectados: total={}, grupos={}, redes MUTUALIA-WIFI={}",
                clients.size(),
                groups,
                networks
        );
    }

    private String normalize(String value) {

        if (value == null) {

            return "";
        }

        return value.trim().toUpperCase();
    }

    private String firstNotBlank(
            String currentValue,
            String newValue
    ) {

        if (currentValue != null
                && !currentValue.isBlank()) {

            return currentValue;
        }

        return newValue;
    }

    private Map<String, Long> countByGroup(
            List<ArubaWifiClientInfo> clients
    ) {

        return clients.stream()
                .collect(Collectors.groupingBy(
                        clientInfo -> normalize(
                                clientInfo.getGroupName()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    private Map<String, Long> countByMutualiaWifiNetwork(
            List<ArubaWifiClientInfo> clients
    ) {

        return clients.stream()
                .filter(clientInfo ->
                        "MUTUALIA-WIFI".equals(
                                normalize(clientInfo.getGroupName())))
                .collect(Collectors.groupingBy(
                        clientInfo -> normalize(
                                clientInfo.getNetwork()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }
}
