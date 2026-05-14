package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tfg.dashboard.client.ArubaApiClient;
import com.tfg.dashboard.dto.ApInfo;
import com.tfg.dashboard.model.AccessPointHistory;
import com.tfg.dashboard.model.ArubaSummary;
import com.tfg.dashboard.repository.AccessPointHistoryRepository;

@Service
public class ArubaService {

    private final ArubaApiClient client;

    private final AccessPointHistoryRepository
            accessPointHistoryRepository;

    public ArubaService(
            ArubaApiClient client,
            AccessPointHistoryRepository accessPointHistoryRepository
    ) {

        this.client = client;
        this.accessPointHistoryRepository =
                accessPointHistoryRepository;
    }

    // =========================================
    // RESUMEN GENERAL
    // =========================================

    public ArubaSummary getSummary() {

        List<ApInfo> aps =
                client.getApsList();


        
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
                accessPointHistoryRepository
                        .countInactiveSince(
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

    // =========================================
    // SNAPSHOT HISTÓRICO
    // =========================================

    public void saveAccessPointSnapshot() {

        List<ApInfo> aps =
                client.getApsList();

        for (ApInfo ap : aps) {

            AccessPointHistory entity =
                    new AccessPointHistory();

            entity.setName(
                    ap.getName()
            );

            entity.setStatus(
                    ap.getStatus()
            );

            entity.setSite(
                    ap.getSite()
            );

            entity.setSwarmName(
                    ap.getSwarmName()
            );

            entity.setCollectedAt(
                    LocalDateTime.now()
            );

            accessPointHistoryRepository
                    .save(entity);
        }

    }
}