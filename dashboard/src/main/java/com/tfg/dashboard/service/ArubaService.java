package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tfg.dashboard.client.ArubaApiClient;
import com.tfg.dashboard.dto.ApInfo;
import com.tfg.dashboard.model.AccessPoint;
import com.tfg.dashboard.model.ArubaSummary;
import com.tfg.dashboard.repository.AccessPointRepository;

@Service
public class ArubaService {

    private final ArubaApiClient client;

    private final AccessPointRepository
            accessPointRepository;

    public ArubaService(
            ArubaApiClient client,
            AccessPointRepository accessPointRepository
    ) {

        this.client = client;
        this.accessPointRepository =
                accessPointRepository;
    }

    // =========================================
    // RESUMEN GENERAL
    // =========================================

    public ArubaSummary getSummary() {

        List<ApInfo> aps =
                client.getApsList();

        syncAccessPoints(
                aps
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

                .map(ApInfo::getSite)

                .filter(site ->
                        site != null
                        && !site.isBlank())

                .distinct()

                .count();

        int totalSwarms =
                (int) aps.stream()

                .map(ApInfo::getSwarmName)

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

        // =========================
        // Estado global red
        // =========================

        String networkStatus = "GREEN";

        if (downAps > 10
                || firmwareOutdated > 5) {

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

        return summary;
    }

    // =========================================
    // LISTADO APs
    // =========================================

    public List<ApInfo> getApsList() {

        return client.getApsList();
    }

    public List<AccessPoint> getStoredAccessPoints() {

        return accessPointRepository.findAll();
    }

    // =========================================
    // SINCRONIZAR APS
    // =========================================

    public void syncAccessPoints() {

        List<ApInfo> aps =
                client.getApsList();

        syncAccessPoints(
                aps
        );
    }

    private void syncAccessPoints(
            List<ApInfo> aps
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        for (ApInfo ap : aps) {

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
}
