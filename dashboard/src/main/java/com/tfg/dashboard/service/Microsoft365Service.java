package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.dto.Microsoft365HealthStatusDto;
import com.tfg.dashboard.dto.Microsoft365IndicatorStatusDto;
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

    private static final String GREEN = "GREEN";

    private static final String YELLOW = "YELLOW";

    private static final String RED = "RED";

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

        Microsoft365HealthStatusDto healthDetails =
                calculateHealthDetails(
                        sharePointStoragePercent,
                        usersWithoutMfa,
                        appsSecretsExpiringSoon,
                        nonCompliantDevices,
                        outdatedWindowsDevices,
                        devicesWithoutEncryption
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
                healthDetails.getColor()
        );
        summary.setMicrosoft365HealthDetails(
                healthDetails
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
        Microsoft365HealthStatusDto healthDetails =
                calculateHealthDetails(
                        history.getSharePointStoragePercent(),
                        history.getUsersWithoutMfa(),
                        history.getAppsSecretsExpiringSoon(),
                        history.getNonCompliantDevices(),
                        history.getOutdatedWindowsDevices(),
                        history.getDevicesWithoutEncryption()
                );

        summary.setMicrosoft365Health(healthDetails.getColor());
        summary.setMicrosoft365HealthDetails(healthDetails);
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
        summary.setMicrosoft365HealthDetails(noDataHealthDetails());

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

    // =========================
    // Indice salud Microsoft 365
    // =========================
    //
    // Normaliza seis indicadores
    // de seguridad, capacidad e
    // Intune en una escala comun
    // de afeccion 0-100.
    //

    private Microsoft365HealthStatusDto calculateHealthDetails(
            int sharePointStoragePercent,
            int usersWithoutMfa,
            int appsSecretsExpiringSoon,
            int nonCompliantDevices,
            int outdatedWindowsDevices,
            int devicesWithoutEncryption
    ) {

        List<Microsoft365IndicatorStatusDto> indicators =
                List.of(
                        evaluateSharePointStorage(
                                sharePointStoragePercent
                        ),
                        evaluateUsersWithoutMfa(usersWithoutMfa),
                        evaluateSecretsExpiringSoon(
                                appsSecretsExpiringSoon
                        ),
                        evaluateNonCompliantDevices(
                                nonCompliantDevices
                        ),
                        evaluateOutdatedWindowsDevices(
                                outdatedWindowsDevices
                        ),
                        evaluateDevicesWithoutEncryption(
                                devicesWithoutEncryption
                        )
                );

        int percentage =
                (int) Math.round(
                        indicators.stream()
                                .mapToInt(
                                        Microsoft365IndicatorStatusDto
                                                ::getAffectionPercent
                                )
                                .average()
                                .orElse(100)
                );

        String color =
                colorByPercentage(percentage);

        List<String> reasons =
                indicators.stream()
                        .filter(indicator ->
                                !GREEN.equals(indicator.getColor()))
                        .map(Microsoft365IndicatorStatusDto::getReason)
                        .toList();

        Microsoft365HealthStatusDto details =
                new Microsoft365HealthStatusDto();

        details.setPercentage(percentage);
        details.setColor(color);
        details.setIndicators(indicators);
        details.setReasons(reasons);
        details.setAffectedService(!GREEN.equals(color));
        details.setCriticalCondition(
                indicators.stream()
                        .anyMatch(indicator ->
                                RED.equals(indicator.getColor()))
        );
        details.setTechnicalDegradationValue(percentage);
        details.setTransversalReady(true);

        return details;
    }

    private Microsoft365IndicatorStatusDto evaluateSharePointStorage(
            int sharePointStoragePercent
    ) {

        if (sharePointStoragePercent > 90) {

            return indicator(
                    "Almacenamiento de SharePoint",
                    RED,
                    "SharePoint supera el 90 % de almacenamiento usado"
            );
        }

        if (sharePointStoragePercent >= 80) {

            return indicator(
                    "Almacenamiento de SharePoint",
                    YELLOW,
                    "SharePoint esta entre el 80 % y el 90 % de almacenamiento usado"
            );
        }

        return indicator(
                "Almacenamiento de SharePoint",
                GREEN,
                "SharePoint esta por debajo del 80 % de almacenamiento usado"
        );
    }

    private Microsoft365IndicatorStatusDto evaluateUsersWithoutMfa(
            int usersWithoutMfa
    ) {

        if (usersWithoutMfa > 3) {

            return indicator(
                    "Usuarios sin MFA",
                    RED,
                    "Hay mas de 3 usuarios sin MFA"
            );
        }

        if (usersWithoutMfa > 0) {

            return indicator(
                    "Usuarios sin MFA",
                    YELLOW,
                    "Hay entre 1 y 3 usuarios sin MFA"
            );
        }

        return indicator(
                "Usuarios sin MFA",
                GREEN,
                "No hay usuarios sin MFA"
        );
    }

    private Microsoft365IndicatorStatusDto evaluateSecretsExpiringSoon(
            int appsSecretsExpiringSoon
    ) {

        if (appsSecretsExpiringSoon > 0) {

            return indicator(
                    "Secretos proximos a caducar",
                    YELLOW,
                    "Hay secretos de aplicaciones proximos a caducar"
            );
        }

        return indicator(
                "Secretos proximos a caducar",
                GREEN,
                "No hay secretos proximos a caducar"
        );
    }

    private Microsoft365IndicatorStatusDto evaluateNonCompliantDevices(
            int nonCompliantDevices
    ) {

        if (nonCompliantDevices > 100) {

            return indicator(
                    "Equipos no conformes",
                    RED,
                    "Hay mas de 100 equipos no conformes"
            );
        }

        if (nonCompliantDevices > 50) {

            return indicator(
                    "Equipos no conformes",
                    YELLOW,
                    "Hay entre 51 y 100 equipos no conformes"
            );
        }

        return indicator(
                "Equipos no conformes",
                GREEN,
                "Hay entre 0 y 50 equipos no conformes"
        );
    }

    private Microsoft365IndicatorStatusDto evaluateOutdatedWindowsDevices(
            int outdatedWindowsDevices
    ) {

        if (outdatedWindowsDevices > 0) {

            return indicator(
                    "Windows desactualizados",
                    YELLOW,
                    "Hay equipos con Windows desactualizado"
            );
        }

        return indicator(
                "Windows desactualizados",
                GREEN,
                "No hay equipos con Windows desactualizado"
        );
    }

    private Microsoft365IndicatorStatusDto evaluateDevicesWithoutEncryption(
            int devicesWithoutEncryption
    ) {

        if (devicesWithoutEncryption > 5) {

            return indicator(
                    "Equipos sin cifrado",
                    RED,
                    "Hay mas de 5 equipos sin cifrado"
            );
        }

        return indicator(
                "Equipos sin cifrado",
                GREEN,
                "Hay entre 0 y 5 equipos sin cifrado"
        );
    }

    private Microsoft365IndicatorStatusDto indicator(
            String name,
            String color,
            String reason
    ) {

        Microsoft365IndicatorStatusDto indicator =
                new Microsoft365IndicatorStatusDto();

        indicator.setName(name);
        indicator.setColor(color);
        indicator.setAffectionPercent(affectionPercent(color));
        indicator.setReason(reason);

        return indicator;
    }

    private int affectionPercent(
            String color
    ) {

        if (RED.equals(color)) {

            return 100;
        }

        if (YELLOW.equals(color)) {

            return 50;
        }

        return 0;
    }

    private String colorByPercentage(
            int percentage
    ) {

        if (percentage >= 67) {

            return RED;
        }

        if (percentage >= 34) {

            return YELLOW;
        }

        return GREEN;
    }

    private Microsoft365HealthStatusDto noDataHealthDetails() {

        Microsoft365IndicatorStatusDto noData =
                indicator(
                        "Datos Microsoft 365",
                        RED,
                        "No hay snapshot Microsoft 365 disponible"
                );

        Microsoft365HealthStatusDto details =
                new Microsoft365HealthStatusDto();

        details.setPercentage(100);
        details.setColor(RED);
        details.setIndicators(List.of(noData));
        details.setReasons(List.of(noData.getReason()));
        details.setAffectedService(true);
        details.setCriticalCondition(true);
        details.setTechnicalDegradationValue(100);
        details.setTransversalReady(true);

        return details;
    }
}
