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
import com.tfg.dashboard.model.ArubaDashboardMetrics;
import com.tfg.dashboard.model.ArubaSummary;
import com.tfg.dashboard.model.ArubaSwitch;
import com.tfg.dashboard.model.ArubaSwitchClientUsage;
import com.tfg.dashboard.model.ArubaSwitchInterfaceUsageHistory;
import com.tfg.dashboard.repository.AccessPointRepository;
import com.tfg.dashboard.repository.ArubaDashboardMetricsRepository;
import com.tfg.dashboard.repository.ArubaSwitchClientUsageRepository;
import com.tfg.dashboard.repository.ArubaSwitchInterfaceUsageHistoryRepository;
import com.tfg.dashboard.repository.ArubaSwitchRepository;

@Service
public class ArubaService {

    private static final Logger log =
            LoggerFactory.getLogger(ArubaService.class);

    private static final int UNDERUSED_SWITCH_DOWN_INTERFACE_LIMIT = 17;

    private static final int UNDERUSED_SWITCH_DAYS = 30;

    private static final long METRICS_ID = 1L;

    private static final int ARUBA_FRESHNESS_MINUTES = 10;

    private final ArubaApiClient client;

    private final AccessPointRepository
            accessPointRepository;

    private final ArubaSwitchRepository
            arubaSwitchRepository;

    private final ArubaSwitchClientUsageRepository
            switchClientUsageRepository;

    private final ArubaSwitchInterfaceUsageHistoryRepository
            switchInterfaceUsageHistoryRepository;

    private final ArubaDashboardMetricsRepository
            dashboardMetricsRepository;

    public ArubaService(
            ArubaApiClient client,
            AccessPointRepository accessPointRepository,
            ArubaSwitchRepository arubaSwitchRepository,
            ArubaSwitchClientUsageRepository switchClientUsageRepository,
            ArubaSwitchInterfaceUsageHistoryRepository
                    switchInterfaceUsageHistoryRepository,
            ArubaDashboardMetricsRepository dashboardMetricsRepository
    ) {

        this.client = client;
        this.accessPointRepository =
                accessPointRepository;
        this.arubaSwitchRepository =
                arubaSwitchRepository;
        this.switchClientUsageRepository =
                switchClientUsageRepository;
        this.switchInterfaceUsageHistoryRepository =
                switchInterfaceUsageHistoryRepository;
        this.dashboardMetricsRepository =
                dashboardMetricsRepository;
    }

    // =========================================
    // RESUMEN GENERAL
    // =========================================

    public ArubaSummary getSummary() {

        return getStoredSummary();
    }

    private ArubaSummary getStoredSummary() {

        List<AccessPoint> aps =
                accessPointRepository.findAll();

        List<ArubaSwitch> switches =
                arubaSwitchRepository.findAll();

        ArubaDashboardMetrics metrics =
                dashboardMetricsRepository
                        .findById(METRICS_ID)
                        .orElseGet(ArubaDashboardMetrics::new);

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
                        .map(AccessPoint::getSite)
                        .filter(site ->
                                site != null
                                && !site.isBlank())
                        .distinct()
                        .count();

        int totalSwarms =
                (int) aps.stream()
                        .map(AccessPoint::getSwarmName)
                        .filter(swarm ->
                                swarm != null
                                && !swarm.isBlank())
                        .distinct()
                        .count();

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
                        .filter(ArubaSwitch::isUpgradeRequired)
                        .count();

        LocalDateTime limitDate =
                LocalDateTime.now()
                             .minusMonths(3);

        long inactiveAps =
                accessPointRepository
                        .countBySerialIsNotNullAndLastSeenAtBefore(
                                limitDate
                        );

        String networkStatus =
                buildNetworkStatus(
                        downAps,
                        metrics.getFirmwareOutdated(),
                        downSwitches,
                        switchesFirmwareUpgradeRequired
                );

        LocalDateTime lastUpdated =
                resolveArubaLastUpdated();

        String dataStatus =
                calculateDataStatus(lastUpdated);

        if ("NO_DATA".equalsIgnoreCase(dataStatus)) {

            // Si no existe ninguna fecha
            // Aruba persistida, el resumen
            // no debe quedar como GREEN.
            // UNKNOWN evita ocultar que no
            // hay datos suficientes sin
            // modificar los cálculos de KPIs.
            networkStatus = "UNKNOWN";
        }

        ArubaSummary summary =
                new ArubaSummary();

        summary.setTotalAps(totalAps);
        summary.setUpAps(upAps);
        summary.setDownAps(downAps);
        summary.setTotalSites(totalSites);
        summary.setTotalSwarms(totalSwarms);
        summary.setFirmwareOutdated(metrics.getFirmwareOutdated());
        summary.setApsWithoutPublicIp(apsWithoutPublicIp);
        summary.setInactiveAps((int) inactiveAps);
        summary.setNetworkStatus(networkStatus);
        summary.setTotalSwitches(totalSwitches);
        summary.setDownSwitches(downSwitches);
        summary.setSwitchesFirmwareUpgradeRequired(
                switchesFirmwareUpgradeRequired
        );
        summary.setUnderusedSwitches(getUnderusedSwitches());
        summary.setTotalWifiClients(metrics.getTotalWifiClients());
        summary.setMutualiaApsClients(metrics.getMutualiaApsClients());
        summary.setMutualiaWifiClients(metrics.getMutualiaWifiClients());
        summary.setMutualiaLangileakClients(
                metrics.getMutualiaLangileakClients()
        );
        summary.setMutualiaClients(metrics.getMutualiaClients());
        summary.setMutualiaRedInternaClients(
                metrics.getMutualiaRedInternaClients()
        );
        summary.setMutualiaRedExternaClients(
                metrics.getMutualiaRedExternaClients()
        );
        summary.setMutualiaKorporatiboaClients(
                metrics.getMutualiaKorporatiboaClients()
        );
        summary.setWifiPacsClients(metrics.getWifiPacsClients());
        summary.setMutVideoClients(metrics.getMutVideoClients());
        summary.setLastUpdated(lastUpdated);
        summary.setDataStatus(dataStatus);

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

        return client.getMonitoringSwitchesList();
    }

    public List<ArubaSwitch> getStoredSwitches() {

        return arubaSwitchRepository.findAll();
    }

    public List<ArubaSwitchClientUsage> getSwitchClientUsage() {

        return switchClientUsageRepository.findAll();
    }

    public List<ArubaSwitchClientUsage> getUnderusedSwitches() {

        LocalDateTime limitDate =
                LocalDateTime.now()
                             .minusDays(UNDERUSED_SWITCH_DAYS);

        List<String> associatedDevices =
                switchInterfaceUsageHistoryRepository
                        .findDevicesAlwaysOverDownInterfaceLimitSince(
                                "Up",
                                UNDERUSED_SWITCH_DOWN_INTERFACE_LIMIT,
                                limitDate
                        );

        if (associatedDevices.isEmpty()) {

            return List.of();
        }

        return switchClientUsageRepository
                .findByAssociatedDeviceInOrderByDownInterfacesDescAssociatedDeviceAsc(
                        associatedDevices
                );
    }

    public List<ArubaWifiClientInfo> getWifiClientsList() {

        return client.getWifiClientsList();
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

        syncFirmwareMetrics(
                client.getFirmwareSwarms()
        );
    }

    public void syncSwitches() {

        List<ArubaSwitchInfo> switches =
                client.getMonitoringSwitchesList();

        syncSwitches(
                switches
        );

        syncSwitchFirmwareState(
                client.getSwitchesList()
        );
    }

    public void syncSwitchClientUsage() {

        syncSwitchClientUsage(
                client.getMonitoringSwitchesList()
        );
    }

    public void syncAll() {

        List<ArubaApInfo> aps =
                client.getApsList();

        JsonNode firmwareSwarms =
                client.getFirmwareSwarms();

        List<ArubaSwitchInfo> switches =
                client.getMonitoringSwitchesList();

        List<ArubaSwitchInfo> firmwareSwitches =
                client.getSwitchesList();

        List<ArubaWifiClientInfo> wifiClients =
                client.getWifiClientsList();

        syncAccessPoints(aps);
        syncSwitches(switches);
        syncSwitchFirmwareState(firmwareSwitches);
        syncSwitchClientUsage(switches);
        syncDashboardMetrics(firmwareSwarms, wifiClients);
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

    private void syncSwitchFirmwareState(
            List<ArubaSwitchInfo> firmwareSwitches
    ) {

        for (ArubaSwitchInfo firmwareSwitch : firmwareSwitches) {

            String serial =
                    firmwareSwitch.getSerial();

            if (serial == null
                    || serial.isBlank()) {

                continue;
            }

            arubaSwitchRepository
                    .findBySerial(serial)
                    .ifPresent(entity -> {
                        entity.setUpgradeRequired(
                                firmwareSwitch.isUpgradeRequired()
                        );
                        entity.setStatusState(
                                firmwareSwitch.getStatusState()
                        );
                        arubaSwitchRepository.save(entity);
                    });
        }
    }

    private void syncFirmwareMetrics(JsonNode firmwareSwarms) {

        ArubaDashboardMetrics metrics =
                dashboardMetricsRepository
                        .findById(METRICS_ID)
                        .orElseGet(() -> {
                            ArubaDashboardMetrics newMetrics =
                                    new ArubaDashboardMetrics();
                            newMetrics.setId(METRICS_ID);
                            return newMetrics;
                        });

        metrics.setFirmwareOutdated(
                countFirmwareOutdated(firmwareSwarms)
        );
        metrics.setUpdatedAt(LocalDateTime.now());

        dashboardMetricsRepository.save(metrics);
    }

    private void syncDashboardMetrics(
            JsonNode firmwareSwarms,
            List<ArubaWifiClientInfo> wifiClients
    ) {

        ArubaDashboardMetrics metrics =
                dashboardMetricsRepository
                        .findById(METRICS_ID)
                        .orElseGet(() -> {
                            ArubaDashboardMetrics newMetrics =
                                    new ArubaDashboardMetrics();
                            newMetrics.setId(METRICS_ID);
                            return newMetrics;
                        });

        metrics.setFirmwareOutdated(
                countFirmwareOutdated(firmwareSwarms)
        );
        metrics.setTotalWifiClients(
                wifiClients.size()
        );
        metrics.setMutualiaApsClients(
                countClientsByGroup(wifiClients, "MUTUALIA-APs")
        );
        metrics.setMutualiaWifiClients(
                countClientsByGroup(wifiClients, "MUTUALIA-WIFI")
        );
        metrics.setMutualiaLangileakClients(
                countClientsByWifiNetwork(
                        wifiClients,
                        "MUTUALIA_LANGILEAK"
                )
        );
        metrics.setMutualiaClients(
                countClientsByWifiNetwork(wifiClients, "MUTUALIA")
        );
        metrics.setMutualiaRedInternaClients(
                countClientsByWifiNetwork(
                        wifiClients,
                        "MUTUALIA_RED_INTERNA"
                )
        );
        metrics.setMutualiaRedExternaClients(
                countClientsByWifiNetwork(
                        wifiClients,
                        "MUTUALIA_RED_EXTERNA"
                )
        );
        metrics.setMutualiaKorporatiboaClients(
                countClientsByWifiNetwork(
                        wifiClients,
                        "MUTUALIA_KORPORATIBOA"
                )
        );
        metrics.setWifiPacsClients(
                countClientsByWifiNetwork(wifiClients, "WIFI_PACs")
        );
        metrics.setMutVideoClients(
                countClientsByWifiNetwork(wifiClients, "MUT_VIDEO")
        );
        metrics.setUpdatedAt(LocalDateTime.now());

        dashboardMetricsRepository.save(metrics);

        logWifiClientBreakdown(wifiClients);
    }

    private void syncSwitchClientUsage(
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

            usage.setDeviceStatus(
                    switchInfo.getDeviceStatus()
            );

            usage.setDownInterfaces(
                    client.countSwitchPortsDown(serial)
            );

            usageByDevice.put(serial, usage);
        }

        for (ArubaSwitchClientUsage existing
                : switchClientUsageRepository.findAll()) {

            if (!usageByDevice.containsKey(
                    existing.getAssociatedDevice())) {

                existing.setDownInterfaces(0);
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

            entity.setDeviceStatus(
                    aggregate.getDeviceStatus()
            );

            entity.setDownInterfaces(
                    aggregate.getDownInterfaces()
            );

            entity.setUpdatedAt(now);

            switchClientUsageRepository.save(entity);

            ArubaSwitchInterfaceUsageHistory history =
                    new ArubaSwitchInterfaceUsageHistory();

            history.setAssociatedDevice(
                    aggregate.getAssociatedDevice()
            );

            history.setAssociatedDeviceName(
                    aggregate.getAssociatedDeviceName()
            );

            history.setAssociatedDeviceMac(
                    aggregate.getAssociatedDeviceMac()
            );

            history.setDeviceStatus(
                    aggregate.getDeviceStatus()
            );

            history.setDownInterfaces(
                    aggregate.getDownInterfaces()
            );

            history.setObservedAt(now);

            switchInterfaceUsageHistoryRepository.save(history);
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

    private String buildNetworkStatus(
            int downAps,
            int firmwareOutdated,
            int downSwitches,
            int switchesFirmwareUpgradeRequired
    ) {

        if (downAps > 10
                || firmwareOutdated > 5
                || downSwitches > 0
                || switchesFirmwareUpgradeRequired > 0) {

            return "RED";
        }

        if (downAps > 0
                || firmwareOutdated > 0) {

            return "YELLOW";
        }

        return "GREEN";
    }

    private LocalDateTime resolveArubaLastUpdated() {

        // Se prioriza la tabla agregada
        // del dashboard Aruba porque resume
        // firmware y clientes WiFi. Si aún
        // no existe, se usa la fecha más
        // reciente de los datos Aruba
        // persistidos.

        LocalDateTime latest =
                dashboardMetricsRepository
                        .findById(METRICS_ID)
                        .map(ArubaDashboardMetrics::getUpdatedAt)
                        .orElse(null);

        latest = newer(
                latest,
                accessPointRepository
                        .findTopByLastSeenAtIsNotNullOrderByLastSeenAtDesc()
                        .map(AccessPoint::getLastSeenAt)
                        .orElse(null)
        );

        latest = newer(
                latest,
                arubaSwitchRepository
                        .findTopByLastSeenAtIsNotNullOrderByLastSeenAtDesc()
                        .map(ArubaSwitch::getLastSeenAt)
                        .orElse(null)
        );

        latest = newer(
                latest,
                switchClientUsageRepository
                        .findTopByUpdatedAtIsNotNullOrderByUpdatedAtDesc()
                        .map(ArubaSwitchClientUsage::getUpdatedAt)
                        .orElse(null)
        );

        latest = newer(
                latest,
                switchInterfaceUsageHistoryRepository
                        .findTopByObservedAtIsNotNullOrderByObservedAtDesc()
                        .map(ArubaSwitchInterfaceUsageHistory::getObservedAt)
                        .orElse(null)
        );

        return latest;
    }

    private String calculateDataStatus(
            LocalDateTime lastUpdated
    ) {

        // Aruba usa APIs reales y su
        // sincronización puede ser menos
        // frecuente que el scheduler de
        // datos simulados. Por eso se
        // considera fresco durante 10
        // minutos.

        if (lastUpdated == null) {

            return "NO_DATA";
        }

        if (lastUpdated.isBefore(
                LocalDateTime.now().minusMinutes(ARUBA_FRESHNESS_MINUTES)
        )) {

            return "STALE";
        }

        return "OK";
    }

    private LocalDateTime newer(
            LocalDateTime current,
            LocalDateTime candidate
    ) {

        if (candidate == null) {

            return current;
        }

        if (current == null
                || candidate.isAfter(current)) {

            return candidate;
        }

        return current;
    }

    private int countFirmwareOutdated(JsonNode firmwareSwarms) {

        int firmwareOutdated = 0;

        if (firmwareSwarms == null) {

            return firmwareOutdated;
        }

        JsonNode swarms =
                firmwareSwarms.get("swarms");

        if (swarms == null
                || !swarms.isArray()) {

            return firmwareOutdated;
        }

        for (JsonNode swarm : swarms) {

            String state =
                    swarm.path("status")
                            .path("state")
                            .asText();

            if (state.trim()
                    .equalsIgnoreCase("UPGRADE_REQUIRED")) {

                firmwareOutdated++;
            }
        }

        return firmwareOutdated;
    }

    private String normalize(String value) {

        if (value == null) {

            return "";
        }

        return value.trim().toUpperCase();
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
