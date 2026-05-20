package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.model.Microsoft365MetricsHistory;
import com.tfg.dashboard.model.Microsoft365Summary;
import com.tfg.dashboard.repository.Microsoft365MetricsHistoryRepository;

@Service
public class Microsoft365Service {

    // =========================
    // Generador temporal MOCK
    // =========================
    //
    // En esta fase no se conecta
    // con Microsoft Graph.
    //
    // Se generan datos dinámicos
    // para validar la arquitectura
    // del dashboard multiproveedor.
    //
    private final Random random =
            new Random();

    private final Microsoft365MetricsHistoryRepository metricsHistoryRepository;

    public Microsoft365Service(
            Microsoft365MetricsHistoryRepository metricsHistoryRepository
    ) {

        this.metricsHistoryRepository =
                metricsHistoryRepository;
    }

    // =========================
    // Resumen persistido M365
    // =========================
    //
    // El endpoint /microsoft365/summary
    // devuelve el último snapshot
    // almacenado en MySQL.
    //
    public Microsoft365Summary getSummary() {

        return metricsHistoryRepository
                .findTopByOrderByCollectedAtDesc()
                .map(this::mapHistoryToSummary)
                .orElseGet(this::noDataSummary);
    }

    // =========================
    // Generación resumen M365
    // =========================
    //
    // Devuelve KPIs simulados
    // relacionados con uso,
    // licencias, servicios,
    // seguridad, aplicaciones
    // e Intune.
    //
    public Microsoft365Summary generateSimulatedSummary() {

        Microsoft365Summary summary =
                new Microsoft365Summary();

        // =========================
        // Uso y licenciamiento
        // =========================

        int activeUsers =
                1200 + random.nextInt(500);

        int unassignedLicenses =
                20 + random.nextInt(120);

        // =========================
        // Estado servicios M365
        // =========================

        String outlookStatus =
                randomServiceStatus();

        String teamsStatus =
                randomServiceStatus();

        String sharePointStatus =
                randomServiceStatus();

        // =========================
        // Exchange / SharePoint
        // =========================

        int nearlyFullMailboxes =
                random.nextInt(40);

        int emailsQuarantined =
                20 + random.nextInt(120);

        int sharePointStoragePercent =
                45 + random.nextInt(50);

        // =========================
        // Seguridad e identidad
        // =========================

        int riskyUsers =
                random.nextInt(12);

        int failedSignIns =
                50 + random.nextInt(600);

        int usersWithoutMfa =
                10 + random.nextInt(80);

        // =========================
        // Aplicaciones empresariales
        // =========================

        int appsSecretsExpiringSoon =
                random.nextInt(15);

        int unusedApplications =
                random.nextInt(25);

        int highPrivilegeApplications =
                random.nextInt(12);

        // =========================
        // Intune / Endpoint Manager
        // =========================

        int nonCompliantDevices =
                random.nextInt(80);

        int outdatedWindowsDevices =
                random.nextInt(60);

        int devicesWithoutEncryption =
                0;

        int staleDevices =
                random.nextInt(50);

        // =========================
        // KPIs compuestos
        // =========================

        int operationalRisk =
                calculateOperationalRisk(
                        outlookStatus,
                        teamsStatus,
                        sharePointStatus,
                        riskyUsers,
                        failedSignIns,
                        usersWithoutMfa,
                        appsSecretsExpiringSoon,
                        highPrivilegeApplications,
                        nonCompliantDevices,
                        outdatedWindowsDevices,
                        staleDevices,
                        sharePointStoragePercent
                );

        String health =
                calculateHealth(
                        operationalRisk
                );

        // =========================
        // Construcción respuesta
        // =========================

        summary.setActiveUsers(
                activeUsers
        );

        summary.setUnassignedLicenses(
                unassignedLicenses
        );

        summary.setOutlookStatus(
                outlookStatus
        );

        summary.setTeamsStatus(
                teamsStatus
        );

        summary.setSharePointStatus(
                sharePointStatus
        );

        summary.setNearlyFullMailboxes(
                nearlyFullMailboxes
        );

        summary.setEmailsQuarantined(
                emailsQuarantined
        );

        summary.setSharePointStoragePercent(
                sharePointStoragePercent
        );

        summary.setRiskyUsers(
                riskyUsers
        );

        summary.setFailedSignIns(
                failedSignIns
        );

        summary.setUsersWithoutMfa(
                usersWithoutMfa
        );

        summary.setAppsSecretsExpiringSoon(
                appsSecretsExpiringSoon
        );

        summary.setUnusedApplications(
                unusedApplications
        );

        summary.setHighPrivilegeApplications(
                highPrivilegeApplications
        );

        summary.setNonCompliantDevices(
                nonCompliantDevices
        );

        summary.setOutdatedWindowsDevices(
                outdatedWindowsDevices
        );

        summary.setDevicesWithoutEncryption(
                devicesWithoutEncryption
        );

        summary.setStaleDevices(
                staleDevices
        );

        summary.setMicrosoft365OperationalRisk(
                operationalRisk
        );

        summary.setMicrosoft365Health(
                health
        );

        return summary;
    }

    private Microsoft365Summary mapHistoryToSummary(
            Microsoft365MetricsHistory history
    ) {

        Microsoft365Summary summary =
                new Microsoft365Summary();

        summary.setActiveUsers(history.getActiveUsers());
        summary.setUnassignedLicenses(history.getUnassignedLicenses());
        summary.setOutlookStatus(history.getOutlookStatus());
        summary.setTeamsStatus(history.getTeamsStatus());
        summary.setSharePointStatus(history.getSharePointStatus());
        summary.setNearlyFullMailboxes(history.getNearlyFullMailboxes());
        summary.setEmailsQuarantined(history.getEmailsQuarantined());
        summary.setSharePointStoragePercent(
                history.getSharePointStoragePercent()
        );
        summary.setRiskyUsers(history.getRiskyUsers());
        summary.setFailedSignIns(history.getFailedSignIns());
        summary.setUsersWithoutMfa(history.getUsersWithoutMfa());
        summary.setAppsSecretsExpiringSoon(
                history.getAppsSecretsExpiringSoon()
        );
        summary.setUnusedApplications(history.getUnusedApplications());
        summary.setHighPrivilegeApplications(
                history.getHighPrivilegeApplications()
        );
        summary.setNonCompliantDevices(history.getNonCompliantDevices());
        summary.setOutdatedWindowsDevices(
                history.getOutdatedWindowsDevices()
        );
        summary.setDevicesWithoutEncryption(
                history.getDevicesWithoutEncryption()
        );
        summary.setStaleDevices(history.getStaleDevices());
        summary.setMicrosoft365Health(history.getMicrosoft365Health());
        summary.setMicrosoft365OperationalRisk(
                history.getMicrosoft365OperationalRisk()
        );
        summary.setLastUpdated(history.getCollectedAt());
        summary.setDataStatus(
                calculateDataStatus(history.getCollectedAt())
        );

        return summary;
    }

    private Microsoft365Summary noDataSummary() {

        Microsoft365Summary summary =
                new Microsoft365Summary();

        // Existe campo específico
        // de frescura en el DTO actual.
        // Usamos NO_DATA en estados para
        // no aparentar que todo está OK
        // si MySQL todavía no tiene datos.
        summary.setOutlookStatus("NO_DATA");
        summary.setTeamsStatus("NO_DATA");
        summary.setSharePointStatus("NO_DATA");
        summary.setMicrosoft365Health("NO_DATA");
        summary.setDataStatus("NO_DATA");

        return summary;
    }

    private String calculateDataStatus(
            LocalDateTime collectedAt
    ) {

        // OK: snapshot reciente.
        // STALE: existe, pero supera
        // el margen esperado.
        // NO_DATA: no hay snapshot.

        if (collectedAt == null) {

            return "NO_DATA";
        }

        if (collectedAt.isAfter(
                LocalDateTime.now().minusMinutes(2)
        )) {

            return "OK";
        }

        return "STALE";
    }

    // =========================
    // Estado servicio simulado
    // =========================
    //
    // Devuelve el estado de un
    // servicio Microsoft 365.
    //
    // La mayoría de veces será OK,
    // pero se permite degradación
    // para simular incidencias.
    //
    private String randomServiceStatus() {

        int value =
                random.nextInt(100);

        if (value < 80) {

            return "HEALTHY";
        }

        if (value < 95) {

            return "DEGRADED";
        }

        return "INCIDENT";
    }

    // =========================
    // Cálculo riesgo operativo
    // =========================
    //
    // Construye un índice 0-100
    // a partir de señales de:
    //
    // - disponibilidad servicios
    // - identidad
    // - seguridad
    // - aplicaciones
    // - Intune
    // - almacenamiento
    //
    private int calculateOperationalRisk(

            String outlookStatus,

            String teamsStatus,

            String sharePointStatus,

            int riskyUsers,

            int failedSignIns,

            int usersWithoutMfa,

            int appsSecretsExpiringSoon,

            int highPrivilegeApplications,

            int nonCompliantDevices,

            int outdatedWindowsDevices,

            int staleDevices,

            int sharePointStoragePercent

    ) {

        int risk =
                0;

        // =========================
        // Riesgo por servicios
        // =========================

        risk += serviceRisk(
                outlookStatus
        );

        risk += serviceRisk(
                teamsStatus
        );

        risk += serviceRisk(
                sharePointStatus
        );

        // =========================
        // Riesgo por identidad
        // =========================

        if (riskyUsers > 5) {

            risk += 15;

        } else if (riskyUsers > 0) {

            risk += 8;
        }

        if (failedSignIns > 400) {

            risk += 10;

        } else if (failedSignIns > 200) {

            risk += 5;
        }

        if (usersWithoutMfa > 50) {

            risk += 15;

        } else if (usersWithoutMfa > 20) {

            risk += 8;
        }

        // =========================
        // Riesgo por aplicaciones
        // =========================

        if (appsSecretsExpiringSoon > 10) {

            risk += 12;

        } else if (appsSecretsExpiringSoon > 0) {

            risk += 6;
        }

        if (highPrivilegeApplications > 8) {

            risk += 10;

        } else if (highPrivilegeApplications > 3) {

            risk += 5;
        }

        // =========================
        // Riesgo por Intune
        // =========================

        if (nonCompliantDevices > 50) {

            risk += 15;

        } else if (nonCompliantDevices > 20) {

            risk += 8;
        }

        if (outdatedWindowsDevices > 40) {

            risk += 10;

        } else if (outdatedWindowsDevices > 15) {

            risk += 5;
        }

        if (staleDevices > 30) {

            risk += 10;

        } else if (staleDevices > 10) {

            risk += 5;
        }

        // =========================
        // Riesgo por almacenamiento
        // =========================

        if (sharePointStoragePercent > 90) {

            risk += 10;

        } else if (sharePointStoragePercent > 80) {

            risk += 5;
        }

        // Limitar a 100

        if (risk > 100) {

            return 100;
        }

        return risk;
    }

    // =========================
    // Riesgo por servicio
    // =========================

    private int serviceRisk(
            String status
    ) {

        if ("INCIDENT".equalsIgnoreCase(
                status
        )) {

            return 15;
        }

        if ("DEGRADED".equalsIgnoreCase(
                status
        )) {

            return 8;
        }

        return 0;
    }

    // =========================
    // Índice salud M365
    // =========================
    //
    // Traduce el riesgo numérico
    // a un semáforo de estado.
    //
    private String calculateHealth(
            int operationalRisk
    ) {

        if (operationalRisk >= 60) {

            return "RED";
        }

        if (operationalRisk >= 30) {

            return "YELLOW";
        }

        return "GREEN";
    }
}
