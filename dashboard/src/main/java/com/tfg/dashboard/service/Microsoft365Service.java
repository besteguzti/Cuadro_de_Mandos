package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.KpiResultDto;
import com.tfg.dashboard.dto.KpiStatus;
import com.tfg.dashboard.dto.Microsoft365HealthStatusDto;
import com.tfg.dashboard.dto.Microsoft365IndicatorStatusDto;
import com.tfg.dashboard.model.Microsoft365MetricsHistory;
import com.tfg.dashboard.dto.summary.Microsoft365Summary;
import com.tfg.dashboard.repository.Microsoft365MetricsHistoryRepository;

/**
 * Servicio de Microsoft 365 simulado.
 *
 * Genera métricas dinámicas, lee snapshots persistidos y calcula el índice de
 * salud Microsoft 365 a partir de capacidad, identidad, seguridad y
 * dispositivos.
 */
@Service
public class Microsoft365Service {

        private final Random random = new Random();

        private static final String GREEN = "GREEN";

        private static final String YELLOW = "YELLOW";

        private static final String RED = "RED";

        private final Microsoft365MetricsHistoryRepository metricsHistoryRepository;
        private final GlpiPlatformTicketService glpiPlatformTicketService;
        private final KpiProperties kpiProperties;

        public Microsoft365Service(
                        Microsoft365MetricsHistoryRepository metricsHistoryRepository,
                        GlpiPlatformTicketService glpiPlatformTicketService,
                        KpiProperties kpiProperties) {

                this.metricsHistoryRepository = metricsHistoryRepository;
                this.glpiPlatformTicketService = glpiPlatformTicketService;
                this.kpiProperties = kpiProperties;
        }

        /**
         * Devuelve el último snapshot Microsoft 365 almacenado en MySQL o
         * NO_DATA si aún no existe histórico.
         */
        public Microsoft365Summary getSummary() {

                return metricsHistoryRepository
                                .findTopByOrderByCollectedAtDesc()
                                .map(this::mapHistoryToSummary)
                                .orElseGet(this::noDataSummary);
        }

        /**
         * Genera KPIs simulados de uso, licencias, servicios, seguridad,
         * aplicaciones e Intune para persistirlos como snapshot.
         */
        public Microsoft365Summary generateSimulatedSummary() {

                Microsoft365Summary summary = new Microsoft365Summary();

                int activeUsers = 1200 + random.nextInt(500);

                int unassignedLicenses = 20 + random.nextInt(120);

                String outlookStatus = randomServiceStatus();

                String teamsStatus = randomServiceStatus();

                String sharePointStatus = randomServiceStatus();

                int nearlyFullMailboxes = random.nextInt(40);

                int emailsQuarantined = 20 + random.nextInt(120);

                int sharePointStoragePercent = 45 + random.nextInt(50);

                int riskyUsers = random.nextInt(12);

                int failedSignIns = 50 + random.nextInt(600);

                int usersWithoutMfa = 10 + random.nextInt(80);

                int appsSecretsExpiringSoon = random.nextInt(15);

                int unusedApplications = random.nextInt(25);

                int highPrivilegeApplications = random.nextInt(12);

                int nonCompliantDevices = random.nextInt(80);

                int outdatedWindowsDevices = random.nextInt(60);

                int devicesWithoutEncryption = 0;

                int staleDevices = random.nextInt(50);

                Microsoft365HealthStatusDto healthDetails = calculateHealthDetails(
                                sharePointStoragePercent,
                                usersWithoutMfa,
                                appsSecretsExpiringSoon,
                                nonCompliantDevices,
                                outdatedWindowsDevices,
                                devicesWithoutEncryption);

                summary.setActiveUsers(
                                activeUsers);

                summary.setUnassignedLicenses(
                                unassignedLicenses);

                summary.setOutlookStatus(
                                outlookStatus);

                summary.setTeamsStatus(
                                teamsStatus);

                summary.setSharePointStatus(
                                sharePointStatus);

                summary.setNearlyFullMailboxes(
                                nearlyFullMailboxes);

                summary.setEmailsQuarantined(
                                emailsQuarantined);

                summary.setSharePointStoragePercent(
                                sharePointStoragePercent);

                summary.setRiskyUsers(
                                riskyUsers);

                summary.setFailedSignIns(
                                failedSignIns);

                summary.setUsersWithoutMfa(
                                usersWithoutMfa);

                summary.setAppsSecretsExpiringSoon(
                                appsSecretsExpiringSoon);

                summary.setUnusedApplications(
                                unusedApplications);

                summary.setHighPrivilegeApplications(
                                highPrivilegeApplications);

                summary.setNonCompliantDevices(
                                nonCompliantDevices);
                summary.setMicrosoft365OpenTickets(
                                glpiPlatformTicketService.getMicrosoft365OpenTickets());

                summary.setOutdatedWindowsDevices(
                                outdatedWindowsDevices);

                summary.setDevicesWithoutEncryption(
                                devicesWithoutEncryption);

                summary.setStaleDevices(
                                staleDevices);
                summary.setMicrosoft365Health(
                                healthDetails.getColor());
                summary.setMicrosoft365HealthDetails(
                                healthDetails);
                summary.setMicrosoft365HealthKpi(
                                buildMicrosoft365HealthKpi(
                                                healthDetails,
                                                LocalDateTime.now(),
                                                "SIMULATED"));

                return summary;
        }

        private Microsoft365Summary mapHistoryToSummary(
                        Microsoft365MetricsHistory history) {

                Microsoft365Summary summary = new Microsoft365Summary();

                summary.setActiveUsers(history.getActiveUsers());
                summary.setUnassignedLicenses(history.getUnassignedLicenses());
                summary.setOutlookStatus(history.getOutlookStatus());
                summary.setTeamsStatus(history.getTeamsStatus());
                summary.setSharePointStatus(history.getSharePointStatus());
                summary.setNearlyFullMailboxes(history.getNearlyFullMailboxes());
                summary.setEmailsQuarantined(history.getEmailsQuarantined());
                summary.setSharePointStoragePercent(
                                history.getSharePointStoragePercent());
                summary.setRiskyUsers(history.getRiskyUsers());
                summary.setFailedSignIns(history.getFailedSignIns());
                summary.setUsersWithoutMfa(history.getUsersWithoutMfa());
                summary.setAppsSecretsExpiringSoon(
                                history.getAppsSecretsExpiringSoon());
                summary.setUnusedApplications(history.getUnusedApplications());
                summary.setHighPrivilegeApplications(
                                history.getHighPrivilegeApplications());
                summary.setNonCompliantDevices(history.getNonCompliantDevices());
                summary.setMicrosoft365OpenTickets(glpiPlatformTicketService.getMicrosoft365OpenTickets());
                summary.setOutdatedWindowsDevices(
                                history.getOutdatedWindowsDevices());
                summary.setDevicesWithoutEncryption(
                                history.getDevicesWithoutEncryption());
                summary.setStaleDevices(history.getStaleDevices());
                Microsoft365HealthStatusDto healthDetails = calculateHealthDetails(
                                history.getSharePointStoragePercent(),
                                history.getUsersWithoutMfa(),
                                history.getAppsSecretsExpiringSoon(),
                                history.getNonCompliantDevices(),
                                history.getOutdatedWindowsDevices(),
                                history.getDevicesWithoutEncryption());

                summary.setMicrosoft365Health(healthDetails.getColor());
                summary.setMicrosoft365HealthDetails(healthDetails);
                summary.setLastUpdated(history.getCollectedAt());
                summary.setDataStatus(
                                calculateDataStatus(history.getCollectedAt()));
                summary.setMicrosoft365HealthKpi(
                                buildMicrosoft365HealthKpi(
                                                healthDetails,
                                                history.getCollectedAt(),
                                                summary.getDataStatus()));

                return summary;
        }

        private Microsoft365Summary noDataSummary() {

                Microsoft365Summary summary = new Microsoft365Summary();

                summary.setOutlookStatus("NO_DATA");
                summary.setTeamsStatus("NO_DATA");
                summary.setSharePointStatus("NO_DATA");
                summary.setMicrosoft365Health("NO_DATA");
                summary.setDataStatus("NO_DATA");
                summary.setMicrosoft365HealthDetails(noDataHealthDetails());
                summary.setMicrosoft365HealthKpi(
                                buildMicrosoft365HealthKpi(
                                                summary.getMicrosoft365HealthDetails(),
                                                null,
                                                summary.getDataStatus()));

                return summary;
        }

        private String calculateDataStatus(
                        LocalDateTime collectedAt) {

                // OK: snapshot reciente.
                // STALE: existe, pero supera
                // el margen esperado.
                // NO_DATA: no hay snapshot.

                if (collectedAt == null) {

                        return "NO_DATA";
                }

                if (collectedAt.isAfter(
                                LocalDateTime.now().minusMinutes(2))) {

                        return "OK";
                }

                return "STALE";
        }

        /**
         * Genera estados simulados de servicio con predominio de HEALTHY y
         * pequeñas probabilidades de degradación o incidencia.
         */
        private String randomServiceStatus() {

                int value = random.nextInt(100);

                if (value < 80) {

                        return "HEALTHY";
                }

                if (value < 95) {

                        return "DEGRADED";
                }

                return "INCIDENT";
        }

        /**
         * Normaliza seis indicadores de Microsoft 365 en una escala común de
         * afección 0-100.
         */
        private Microsoft365HealthStatusDto calculateHealthDetails(
                        int sharePointStoragePercent,
                        int usersWithoutMfa,
                        int appsSecretsExpiringSoon,
                        int nonCompliantDevices,
                        int outdatedWindowsDevices,
                        int devicesWithoutEncryption) {

                List<Microsoft365IndicatorStatusDto> indicators = List.of(
                                evaluateSharePointStorage(
                                                sharePointStoragePercent),
                                evaluateUsersWithoutMfa(usersWithoutMfa),
                                evaluateSecretsExpiringSoon(
                                                appsSecretsExpiringSoon),
                                evaluateNonCompliantDevices(
                                                nonCompliantDevices),
                                evaluateOutdatedWindowsDevices(
                                                outdatedWindowsDevices),
                                evaluateDevicesWithoutEncryption(
                                                devicesWithoutEncryption));

                int percentage = (int) Math.round(
                                indicators.stream()
                                                .mapToInt(
                                                                Microsoft365IndicatorStatusDto::getAffectionPercent)
                                                .average()
                                                .orElse(100));

                String color = colorByPercentage(percentage);

                List<String> reasons = indicators.stream()
                                .filter(indicator -> !GREEN.equals(indicator.getColor()))
                                .map(Microsoft365IndicatorStatusDto::getReason)
                                .toList();

                Microsoft365HealthStatusDto details = new Microsoft365HealthStatusDto();

                details.setPercentage(percentage);
                details.setColor(color);
                details.setIndicators(indicators);
                details.setReasons(reasons);
                details.setAffectedService(!GREEN.equals(color));
                details.setCriticalCondition(
                                indicators.stream()
                                                .anyMatch(indicator -> RED.equals(indicator.getColor())));
                details.setTechnicalDegradationValue(percentage);
                details.setTransversalReady(true);

                return details;
        }

        private Microsoft365IndicatorStatusDto evaluateSharePointStorage(
                        int sharePointStoragePercent) {

                if (sharePointStoragePercent > kpiProperties.getMicrosoft365().getSharePointRedAbove()) {

                        return indicator(
                                        "Almacenamiento de SharePoint",
                                        RED,
                                        "SharePoint supera el 90 % de almacenamiento usado");
                }

                if (sharePointStoragePercent >= kpiProperties.getMicrosoft365().getSharePointYellowMin()) {

                        return indicator(
                                        "Almacenamiento de SharePoint",
                                        YELLOW,
                                        "SharePoint esta entre el 80 % y el 90 % de almacenamiento usado");
                }

                return indicator(
                                "Almacenamiento de SharePoint",
                                GREEN,
                                "SharePoint esta por debajo del 80 % de almacenamiento usado");
        }

        private Microsoft365IndicatorStatusDto evaluateUsersWithoutMfa(
                        int usersWithoutMfa) {

                if (usersWithoutMfa > kpiProperties.getMicrosoft365().getUsersWithoutMfaRedAbove()) {

                        return indicator(
                                        "Usuarios sin MFA",
                                        RED,
                                        "Hay mas de 3 usuarios sin MFA");
                }

                if (usersWithoutMfa > kpiProperties.getMicrosoft365().getUsersWithoutMfaYellowAbove()) {

                        return indicator(
                                        "Usuarios sin MFA",
                                        YELLOW,
                                        "Hay entre 1 y 3 usuarios sin MFA");
                }

                return indicator(
                                "Usuarios sin MFA",
                                GREEN,
                                "No hay usuarios sin MFA");
        }

        private Microsoft365IndicatorStatusDto evaluateSecretsExpiringSoon(
                        int appsSecretsExpiringSoon) {

                if (appsSecretsExpiringSoon > kpiProperties.getMicrosoft365().getSecretsYellowAbove()) {

                        return indicator(
                                        "Secretos proximos a caducar",
                                        YELLOW,
                                        "Hay secretos de aplicaciones proximos a caducar");
                }

                return indicator(
                                "Secretos proximos a caducar",
                                GREEN,
                                "No hay secretos proximos a caducar");
        }

        private Microsoft365IndicatorStatusDto evaluateNonCompliantDevices(
                        int nonCompliantDevices) {

                if (nonCompliantDevices > kpiProperties.getMicrosoft365().getNonCompliantDevicesRedAbove()) {

                        return indicator(
                                        "Equipos no conformes",
                                        RED,
                                        "Hay mas de 100 equipos no conformes");
                }

                if (nonCompliantDevices > kpiProperties.getMicrosoft365().getNonCompliantDevicesYellowAbove()) {

                        return indicator(
                                        "Equipos no conformes",
                                        YELLOW,
                                        "Hay entre 51 y 100 equipos no conformes");
                }

                return indicator(
                                "Equipos no conformes",
                                GREEN,
                                "Hay entre 0 y 50 equipos no conformes");
        }

        private Microsoft365IndicatorStatusDto evaluateOutdatedWindowsDevices(
                        int outdatedWindowsDevices) {

                if (outdatedWindowsDevices > kpiProperties.getMicrosoft365().getOutdatedWindowsYellowAbove()) {

                        return indicator(
                                        "Windows desactualizados",
                                        YELLOW,
                                        "Hay equipos con Windows desactualizado");
                }

                return indicator(
                                "Windows desactualizados",
                                GREEN,
                                "No hay equipos con Windows desactualizado");
        }

        private Microsoft365IndicatorStatusDto evaluateDevicesWithoutEncryption(
                        int devicesWithoutEncryption) {

                if (devicesWithoutEncryption > kpiProperties.getMicrosoft365().getDevicesWithoutEncryptionRedAbove()) {

                        return indicator(
                                        "Equipos sin cifrado",
                                        RED,
                                        "Hay mas de 5 equipos sin cifrado");
                }

                return indicator(
                                "Equipos sin cifrado",
                                GREEN,
                                "Hay entre 0 y 5 equipos sin cifrado");
        }

        private Microsoft365IndicatorStatusDto indicator(
                        String name,
                        String color,
                        String reason) {

                Microsoft365IndicatorStatusDto indicator = new Microsoft365IndicatorStatusDto();

                indicator.setName(name);
                indicator.setColor(color);
                indicator.setAffectionPercent(affectionPercent(color));
                indicator.setReason(reason);

                return indicator;
        }

        private int affectionPercent(
                        String color) {

                if (RED.equals(color)) {

                        return kpiProperties.getAffection().getRed();
                }

                if (YELLOW.equals(color)) {

                        return kpiProperties.getAffection().getYellow();
                }

                return kpiProperties.getAffection().getGreen();
        }

        private String colorByPercentage(
                        int percentage) {

                if (percentage >= kpiProperties.getStatus().getRedMin()) {

                        return RED;
                }

                if (percentage >= kpiProperties.getStatus().getYellowMin()) {

                        return YELLOW;
                }

                return GREEN;
        }

        private Microsoft365HealthStatusDto noDataHealthDetails() {

                Microsoft365IndicatorStatusDto noData = indicator(
                                "Datos Microsoft 365",
                                RED,
                                "No hay snapshot Microsoft 365 disponible");

                Microsoft365HealthStatusDto details = new Microsoft365HealthStatusDto();

                details.setPercentage(kpiProperties.getAffection().getRed());
                details.setColor(RED);
                details.setIndicators(List.of(noData));
                details.setReasons(List.of(noData.getReason()));
                details.setAffectedService(true);
                details.setCriticalCondition(true);
                details.setTechnicalDegradationValue(kpiProperties.getAffection().getRed());
                details.setTransversalReady(true);

                return details;
        }

        private KpiResultDto buildMicrosoft365HealthKpi(
                        Microsoft365HealthStatusDto details,
                        LocalDateTime timestamp,
                        String freshness) {

                return new KpiResultDto(
                                "microsoft365_health",
                                "Indice de salud Microsoft 365",
                                details.getPercentage(),
                                KpiStatus.from(details.getColor()),
                                "Afeccion normalizada de Microsoft 365.",
                                "Media uniforme de almacenamiento SharePoint, usuarios sin MFA, secretos proximos a caducar, equipos no conformes, Windows desactualizados y equipos sin cifrado.",
                                timestamp,
                                freshness,
                                details.getPercentage(),
                                details.getIndicators().stream()
                                                .map(indicator -> new KpiResultDto(
                                                                indicatorId(indicator.getName()),
                                                                indicator.getName(),
                                                                indicator.getAffectionPercent(),
                                                                KpiStatus.from(indicator.getColor()),
                                                                indicator.getReason(),
                                                                null,
                                                                timestamp,
                                                                freshness,
                                                                indicator.getAffectionPercent(),
                                                                List.of()))
                                                .toList());
        }

        private String indicatorId(String name) {

                return name.toLowerCase()
                                .replace(" ", "_")
                                .replace("%", "percent");
        }
}
