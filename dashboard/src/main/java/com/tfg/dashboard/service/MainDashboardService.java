package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.dto.ArubaNetworkStatusDto;
import com.tfg.dashboard.model.ArubaSummary;
import com.tfg.dashboard.model.CitrixMetricsHistory;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.model.MainDashboardSummary;
import com.tfg.dashboard.model.Microsoft365MetricsHistory;
import com.tfg.dashboard.repository.CitrixMetricsHistoryRepository;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;
import com.tfg.dashboard.repository.Microsoft365MetricsHistoryRepository;

@Service
public class MainDashboardService {

    // =========================
    // Orígenes dashboard general
    // =========================
    //
    // Aruba se mantiene con su flujo
    // real existente. Citrix,
    // Microsoft 365 y GLPI se leen
    // desde las últimas muestras
    // guardadas en MySQL.
    //

    private final ArubaService arubaService;

    private final CitrixMetricsHistoryRepository citrixRepository;

    private final Microsoft365MetricsHistoryRepository microsoft365Repository;

    private final GlpiMetricsHistoryRepository glpiRepository;

    public MainDashboardService(
            ArubaService arubaService,
            CitrixMetricsHistoryRepository citrixRepository,
            Microsoft365MetricsHistoryRepository microsoft365Repository,
            GlpiMetricsHistoryRepository glpiRepository
    ) {

        this.arubaService = arubaService;
        this.citrixRepository = citrixRepository;
        this.microsoft365Repository = microsoft365Repository;
        this.glpiRepository = glpiRepository;
    }

    public MainDashboardSummary getSummary() {

        ArubaSummary aruba =
                arubaService.getSummary();

        Optional<CitrixMetricsHistory> citrixSnapshot =
                citrixRepository.findTopByOrderByCollectedAtDesc();

        Optional<Microsoft365MetricsHistory> microsoft365Snapshot =
                microsoft365Repository.findTopByOrderByCollectedAtDesc();

        Optional<GlpiMetricsHistory> glpiSnapshot =
                glpiRepository.findTopByOrderByCollectedAtDesc();

        CitrixMetricsHistory citrix =
                citrixSnapshot.orElseGet(CitrixMetricsHistory::new);

        Microsoft365MetricsHistory microsoft365 =
                microsoft365Snapshot.orElseGet(
                        Microsoft365MetricsHistory::new
                );

        GlpiMetricsHistory glpi =
                glpiSnapshot.orElseGet(GlpiMetricsHistory::new);

        String citrixDataStatus =
                calculateDataStatus(citrixSnapshot);

        String arubaDataStatus =
                normalizeDataStatus(aruba.getDataStatus());

        String microsoft365DataStatus =
                calculateDataStatus(microsoft365Snapshot);

        String glpiDataStatus =
                calculateDataStatus(glpiSnapshot);

        String dataStatus =
                calculateGlobalDataStatus(
                        arubaDataStatus,
                        citrixDataStatus,
                        microsoft365DataStatus,
                        glpiDataStatus
                );

        MainDashboardSummary summary =
                new MainDashboardSummary();

        int arubaHealthIndex =
                calculateArubaNetworkAffection(aruba);

        int citrixHealthIndex =
                calculateCitrixHealthAffection(citrix);

        int microsoft365HealthIndex =
                calculateMicrosoft365HealthAffection(microsoft365);

        int glpiHealthIndex =
                calculateGlpiHealthAffection(glpi);

        int globalHealthPercentage =
                weightedAverage(
                        arubaHealthIndex, 40,
                        citrixHealthIndex, 30,
                        microsoft365HealthIndex, 20,
                        glpiHealthIndex, 10
                );

        int globalCriticality =
                calculateGlobalCriticality(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int globalAvailability =
                calculateGlobalAvailability(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int operationalPressure =
                calculateOperationalPressure(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int technicalDegradation =
                calculateTechnicalDegradation(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int slaRisk =
                calculateSlaRisk(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int operationalBacklog =
                calculateOperationalBacklog(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int userImpact =
                calculateUserImpact(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int affectedServicesPercent =
                calculateAffectedServicesPercent(
                        arubaHealthIndex,
                        citrixHealthIndex,
                        microsoft365HealthIndex,
                        glpiHealthIndex
                );

        summary.setGlobalHealth(
                applyFreshnessToColor(
                        colorByPercentage(globalHealthPercentage),
                        dataStatus
                )
        );
        summary.setGlobalHealthPercentage(globalHealthPercentage);
        summary.setGlobalCriticality(globalCriticality);
        summary.setGlobalAvailability(globalAvailability);
        summary.setUserImpact(userImpact);
        summary.setAffectedServicesPercent(affectedServicesPercent);
        summary.setTechnicalDegradation(technicalDegradation);
        summary.setOperationalPressure(operationalPressure);
        summary.setOperationalBacklog(operationalBacklog);
        summary.setSlaRisk(slaRisk);

        // Se conservan campos historicos
        // del JSON para no romper vistas o
        // consumidores existentes. Desde
        // ahora se alimentan con los nuevos
        // indices transversales normalizados.
        summary.setGlobalOperationalRisk(globalCriticality);
        summary.setServicesWithAlerts(
                calculateAffectedPlatformCount(
                        arubaHealthIndex,
                        citrixHealthIndex,
                        microsoft365HealthIndex,
                        glpiHealthIndex
                )
        );

        // Este KPI representa actividad
        // agregada observada, no usuarios
        // únicos reales.
        summary.setTotalActiveUsers(
                citrix.getActiveSessions()
                        + microsoft365.getActiveUsers()
                        + aruba.getTotalWifiClients()
        );

        summary.setItemsRequiringAction(
                calculateItemsRequiringAction(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                )
        );
        summary.setCriticalOpenTickets(
                glpi.getCriticalOpenTickets()
        );
        summary.setSecurityRiskItems(
                microsoft365.getRiskyUsers()
                        + microsoft365.getUsersWithoutMfa()
                        + microsoft365.getHighPrivilegeApplications()
                        + microsoft365.getAppsSecretsExpiringSoon()
        );
        summary.setCapacityPressure(
                operationalPressure
        );
        summary.setLastUpdated(
                latestCollectedAt(
                        aruba.getLastUpdated(),
                        citrixSnapshot,
                        microsoft365Snapshot,
                        glpiSnapshot
                )
        );
        summary.setDataStatus(dataStatus);
        summary.setArubaDataStatus(arubaDataStatus);
        summary.setCitrixDataStatus(citrixDataStatus);
        summary.setMicrosoft365DataStatus(microsoft365DataStatus);
        summary.setGlpiDataStatus(glpiDataStatus);

        return summary;
    }

    private String calculateGlobalHealth(
            ArubaSummary aruba,
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi,
            String dataStatus
    ) {

        if (isRed(aruba.getNetworkStatus())
                || isRed(citrix.getCitrixHealth())
                || isRed(microsoft365.getMicrosoft365Health())
                || glpi.getCriticalOpenTickets() > 0
                || glpi.getSlaBreachedTickets() > 15) {

            return "RED";
        }

        if (!"OK".equalsIgnoreCase(dataStatus)
                || isYellow(aruba.getNetworkStatus())
                || isYellow(citrix.getCitrixHealth())
                || isYellow(microsoft365.getMicrosoft365Health())
                || hasMicrosoftServiceAlert(microsoft365)
                || glpi.getSlaBreachedTickets() > 0
                || glpi.getOperationalBacklog() > 150) {

            return "YELLOW";
        }

        return "GREEN";
    }

    private int calculateGlobalOperationalRisk(
            ArubaSummary aruba,
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi
    ) {

        int microsoftRisk =
                microsoft365.getMicrosoft365OperationalRisk();

        int glpiRisk =
                clamp(
                        glpi.getCriticalOpenTickets() * 10
                                + glpi.getSlaBreachedTickets() * 3
                                + glpi.getOperationalBacklog() / 2
                );

        int citrixRisk =
                clamp(
                        citrix.getServerLoadPercent()
                                + citrix.getAverageLogonDurationSeconds()
                                + citrix.getFailedLogons() * 2
                );

        int arubaRisk =
                calculateArubaRisk(aruba);

        return clamp(
                microsoftRisk * 35 / 100
                        + glpiRisk * 25 / 100
                        + citrixRisk * 25 / 100
                        + arubaRisk * 15 / 100
        );
    }

    private int calculateServicesWithAlerts(
            ArubaSummary aruba,
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi
    ) {

        int alerts =
                0;

        if (!isGreen(aruba.getNetworkStatus())
                || !"OK".equalsIgnoreCase(
                        normalizeDataStatus(aruba.getDataStatus())
                )
                || aruba.getDownAps() > 0
                || aruba.getDownSwitches() > 0
                || aruba.getFirmwareOutdated() > 0
                || aruba.getSwitchesFirmwareUpgradeRequired() > 0) {

            alerts++;
        }

        if (!isGreen(citrix.getCitrixHealth())
                || !"OK".equalsIgnoreCase(
                        calculateDataStatus(citrix.getCollectedAt())
                )) {

            alerts++;
        }

        if (!isGreen(microsoft365.getMicrosoft365Health())
                || hasMicrosoftServiceAlert(microsoft365)
                || !"OK".equalsIgnoreCase(
                        calculateDataStatus(microsoft365.getCollectedAt())
                )) {

            alerts++;
        }

        if (glpi.getCriticalOpenTickets() > 0
                || glpi.getSlaBreachedTickets() > 0
                || !"OK".equalsIgnoreCase(
                        calculateDataStatus(glpi.getCollectedAt())
                )) {

            alerts++;
        }

        return alerts;
    }

    private int calculateItemsRequiringAction(
            ArubaSummary aruba,
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi
    ) {

        int unavailableDeliveryControllers =
                Math.max(
                        0,
                        citrix.getTotalDeliveryControllers()
                                - citrix.getAvailableDeliveryControllers()
                );

        return aruba.getFirmwareOutdated()
                + aruba.getSwitchesFirmwareUpgradeRequired()
                + microsoft365.getAppsSecretsExpiringSoon()
                + microsoft365.getNonCompliantDevices()
                + microsoft365.getStaleDevices()
                + glpi.getSlaBreachedTickets()
                + unavailableDeliveryControllers;
    }

    private int calculateArubaRisk(
            ArubaSummary aruba
    ) {

        ArubaNetworkStatusDto networkStatusDetails =
                aruba.getNetworkStatusDetails();

        if (networkStatusDetails != null) {

            return clamp(networkStatusDetails.getTechnicalDegradationValue());
        }

        return clamp(
                aruba.getDownAps() * 10
                        + aruba.getFirmwareOutdated() * 5
                        + aruba.getDownSwitches() * 15
                        + aruba.getSwitchesFirmwareUpgradeRequired() * 5
        );
    }

    private int calculateCapacityPressure(
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi
    ) {

        int backlogPressure =
                glpi.getOperationalBacklog() >= 200
                        ? 100
                        : glpi.getOperationalBacklog() * 100 / 200;

        return clamp(
                (
                        citrix.getServerLoadPercent()
                                + microsoft365.getSharePointStoragePercent()
                                + backlogPressure
                ) / 3
        );
    }

    private int calculateArubaNetworkAffection(
            ArubaSummary aruba
    ) {

        ArubaNetworkStatusDto details =
                aruba.getNetworkStatusDetails();

        if (details != null) {

            return clamp(details.getPercentage());
        }

        return Math.max(
                colorToAffection(aruba.getNetworkStatus()),
                calculateArubaRisk(aruba)
        );
    }

    private int calculateCitrixHealthAffection(
            CitrixMetricsHistory citrix
    ) {

        int calculated =
                average(
                        activeSessionsIndicator(citrix),
                        deliveryControllersIndicator(citrix),
                        logonDurationIndicator(citrix),
                        serverLoadIndicator(citrix),
                        failedLogonsIndicator(citrix)
                );

        return Math.max(
                calculated,
                colorToAffection(citrix.getCitrixHealth())
        );
    }

    private int calculateMicrosoft365HealthAffection(
            Microsoft365MetricsHistory microsoft365
    ) {

        int calculated =
                average(
                        sharePointStorageIndicator(microsoft365),
                        usersWithoutMfaIndicator(microsoft365),
                        secretsIndicator(microsoft365),
                        nonCompliantDevicesIndicator(microsoft365),
                        outdatedWindowsIndicator(microsoft365),
                        devicesWithoutEncryptionIndicator(microsoft365)
                );

        return Math.max(
                calculated,
                colorToAffection(microsoft365.getMicrosoft365Health())
        );
    }

    private int calculateGlpiHealthAffection(
            GlpiMetricsHistory glpi
    ) {

        return average(
                openTicketsIndicator(glpi),
                criticalTicketsIndicator(glpi),
                closedPercentageIndicator(
                        glpi.getCreatedToday(),
                        glpi.getClosedToday()
                ),
                closedPercentageIndicator(
                        glpi.getCreatedThisWeek(),
                        glpi.getClosedThisWeek()
                )
        );
    }

    private int calculateGlobalCriticality(
            ArubaSummary aruba,
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi
    ) {

        return average(
                allApsDownIndicator(aruba),
                allSwitchesDownIndicator(aruba),
                noClientsIndicator(aruba.getTotalWifiClients()),
                noClientsIndicator(aruba.getMutualiaApsClients()),
                noClientsIndicator(aruba.getMutualiaWifiClients()),
                activeSessionsIndicator(citrix),
                deliveryControllersIndicator(citrix),
                logonDurationIndicator(citrix),
                serverLoadIndicator(citrix),
                failedLogonsIndicator(citrix),
                sharePointStorageIndicator(microsoft365),
                usersWithoutMfaIndicator(microsoft365),
                nonCompliantDevicesIndicator(microsoft365),
                devicesWithoutEncryptionIndicator(microsoft365),
                openTicketsIndicator(glpi),
                criticalTicketsIndicator(glpi)
        );
    }

    private int calculateGlobalAvailability(
            ArubaSummary aruba,
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi
    ) {

        int arubaAvailability =
                average(
                        apAvailabilityIndicator(aruba),
                        switchAvailabilityIndicator(aruba),
                        noClientsIndicator(aruba.getTotalWifiClients())
                );

        int citrixAvailability =
                average(
                        activeSessionsIndicator(citrix),
                        deliveryControllersIndicator(citrix)
                );

        int microsoftAvailability =
                average(
                        sharePointStorageIndicator(microsoft365),
                        secretsIndicator(microsoft365)
                );

        int glpiAvailability =
                calculateGlpiHealthAffection(glpi);

        return weightedAverage(
                arubaAvailability, 45,
                citrixAvailability, 35,
                microsoftAvailability, 15,
                glpiAvailability, 5
        );
    }

    private int calculateOperationalPressure(
            ArubaSummary aruba,
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi
    ) {

        int glpiPressure =
                calculateGlpiHealthAffection(glpi);

        int citrixPressure =
                average(
                        failedLogonsIndicator(citrix),
                        serverLoadIndicator(citrix)
                );

        int microsoftPressure =
                average(
                        nonCompliantDevicesIndicator(microsoft365),
                        outdatedWindowsIndicator(microsoft365),
                        devicesWithoutEncryptionIndicator(microsoft365)
                );

        int arubaPressure =
                average(
                        countIndicator(aruba.getInactiveAps()),
                        countIndicator(aruba.getFirmwareOutdated()),
                        countIndicator(
                                aruba.getSwitchesFirmwareUpgradeRequired()
                        )
                );

        return weightedAverage(
                glpiPressure, 50,
                citrixPressure, 20,
                microsoftPressure, 20,
                arubaPressure, 10
        );
    }

    private int calculateTechnicalDegradation(
            ArubaSummary aruba,
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi
    ) {

        int arubaDegradation =
                average(
                        countIndicator(aruba.getFirmwareOutdated()),
                        countIndicator(aruba.getInactiveAps()),
                        partialSwitchesDownIndicator(aruba)
                );

        int citrixDegradation =
                average(
                        logonDurationIndicator(citrix),
                        serverLoadIndicator(citrix),
                        failedLogonsIndicator(citrix)
                );

        int microsoftDegradation =
                average(
                        sharePointStorageIndicator(microsoft365),
                        secretsIndicator(microsoft365),
                        outdatedWindowsIndicator(microsoft365),
                        nonCompliantDevicesIndicator(microsoft365),
                        devicesWithoutEncryptionIndicator(microsoft365)
                );

        int glpiDegradation =
                average(
                        openTicketsIndicator(glpi),
                        criticalTicketsIndicator(glpi)
                );

        return weightedAverage(
                arubaDegradation, 30,
                citrixDegradation, 30,
                microsoftDegradation, 30,
                glpiDegradation, 10
        );
    }

    private int calculateSlaRisk(
            ArubaSummary aruba,
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi
    ) {

        int citrixSlaRisk =
                average(
                        logonDurationIndicator(citrix),
                        activeSessionsIndicator(citrix),
                        deliveryControllersIndicator(citrix),
                        failedLogonsIndicator(citrix)
                );

        int arubaSlaRisk =
                calculateArubaNetworkAffection(aruba);

        int glpiSlaRisk =
                average(
                        criticalTicketsIndicator(glpi),
                        closedPercentageIndicator(
                                glpi.getCreatedToday(),
                                glpi.getClosedToday()
                        ),
                        closedPercentageIndicator(
                                glpi.getCreatedThisWeek(),
                                glpi.getClosedThisWeek()
                        )
                );

        int microsoftSlaRisk =
                average(
                        sharePointStorageIndicator(microsoft365),
                        secretsIndicator(microsoft365),
                        nonCompliantDevicesIndicator(microsoft365)
                );

        return weightedAverage(
                citrixSlaRisk, 35,
                arubaSlaRisk, 30,
                glpiSlaRisk, 25,
                microsoftSlaRisk, 10
        );
    }

    private int calculateOperationalBacklog(
            ArubaSummary aruba,
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi
    ) {

        int glpiBacklog =
                calculateGlpiHealthAffection(glpi);

        int microsoftBacklog =
                average(
                        nonCompliantDevicesIndicator(microsoft365),
                        outdatedWindowsIndicator(microsoft365),
                        devicesWithoutEncryptionIndicator(microsoft365)
                );

        int arubaBacklog =
                average(
                        countIndicator(aruba.getFirmwareOutdated()),
                        countIndicator(
                                aruba.getSwitchesFirmwareUpgradeRequired()
                        )
                );

        int citrixBacklog =
                failedLogonsIndicator(citrix);

        return weightedAverage(
                glpiBacklog, 70,
                microsoftBacklog, 15,
                arubaBacklog, 10,
                citrixBacklog, 5
        );
    }

    private int calculateUserImpact(
            ArubaSummary aruba,
            CitrixMetricsHistory citrix,
            Microsoft365MetricsHistory microsoft365,
            GlpiMetricsHistory glpi
    ) {

        int arubaImpact =
                average(
                        noClientsIndicator(aruba.getTotalWifiClients()),
                        noClientsIndicator(aruba.getMutualiaApsClients()),
                        noClientsIndicator(aruba.getMutualiaWifiClients()),
                        apAvailabilityIndicator(aruba),
                        switchAvailabilityIndicator(aruba)
                );

        int citrixImpact =
                average(
                        activeSessionsIndicator(citrix),
                        logonDurationIndicator(citrix),
                        failedLogonsIndicator(citrix),
                        deliveryControllersIndicator(citrix)
                );

        int microsoftImpact =
                average(
                        sharePointStorageIndicator(microsoft365),
                        usersWithoutMfaIndicator(microsoft365),
                        nonCompliantDevicesIndicator(microsoft365)
                );

        int glpiImpact =
                average(
                        criticalTicketsIndicator(glpi),
                        openTicketsIndicator(glpi)
                );

        return weightedAverage(
                citrixImpact, 35,
                arubaImpact, 35,
                microsoftImpact, 20,
                glpiImpact, 10
        );
    }

    private int calculateAffectedServicesPercent(
            int arubaHealthIndex,
            int citrixHealthIndex,
            int microsoft365HealthIndex,
            int glpiHealthIndex
    ) {

        return calculateAffectedPlatformCount(
                arubaHealthIndex,
                citrixHealthIndex,
                microsoft365HealthIndex,
                glpiHealthIndex
        ) * 25;
    }

    private int calculateAffectedPlatformCount(
            int arubaHealthIndex,
            int citrixHealthIndex,
            int microsoft365HealthIndex,
            int glpiHealthIndex
    ) {

        int affected =
                0;

        if (!isGreen(colorByPercentage(arubaHealthIndex))) {

            affected++;
        }

        if (!isGreen(colorByPercentage(citrixHealthIndex))) {

            affected++;
        }

        if (!isGreen(colorByPercentage(microsoft365HealthIndex))) {

            affected++;
        }

        if (!isGreen(colorByPercentage(glpiHealthIndex))) {

            affected++;
        }

        return affected;
    }

    private int apAvailabilityIndicator(
            ArubaSummary aruba
    ) {

        if (aruba.getTotalAps() <= 0
                || aruba.getDownAps() >= aruba.getTotalAps()) {

            return 100;
        }

        if (aruba.getDownAps() > 0) {

            return 50;
        }

        return 0;
    }

    private int switchAvailabilityIndicator(
            ArubaSummary aruba
    ) {

        if (aruba.getTotalSwitches() <= 0
                || aruba.getDownSwitches() >= aruba.getTotalSwitches()) {

            return 100;
        }

        if (aruba.getDownSwitches() > 0) {

            return 50;
        }

        return 0;
    }

    private int allApsDownIndicator(
            ArubaSummary aruba
    ) {

        if (aruba.getTotalAps() <= 0) {

            return 100;
        }

        if (aruba.getDownAps() >= aruba.getTotalAps()) {

            return 100;
        }

        return aruba.getDownAps() > 0 ? 50 : 0;
    }

    private int allSwitchesDownIndicator(
            ArubaSummary aruba
    ) {

        if (aruba.getTotalSwitches() <= 0) {

            return 100;
        }

        if (aruba.getDownSwitches() >= aruba.getTotalSwitches()) {

            return 100;
        }

        return aruba.getDownSwitches() > 0 ? 50 : 0;
    }

    private int partialSwitchesDownIndicator(
            ArubaSummary aruba
    ) {

        if (aruba.getTotalSwitches() <= 0) {

            return 100;
        }

        if (aruba.getDownSwitches() >= aruba.getTotalSwitches()) {

            return 100;
        }

        return aruba.getDownSwitches() > 0 ? 50 : 0;
    }

    private int noClientsIndicator(int clients) {

        return clients <= 0 ? 100 : 0;
    }

    private int activeSessionsIndicator(
            CitrixMetricsHistory citrix
    ) {

        return citrix.getActiveSessions() <= 0 ? 100 : 0;
    }

    private int deliveryControllersIndicator(
            CitrixMetricsHistory citrix
    ) {

        if (citrix.getTotalDeliveryControllers() <= 0
                || citrix.getAvailableDeliveryControllers() <= 0) {

            return 100;
        }

        int availablePercent =
                citrix.getAvailableDeliveryControllers() * 100
                        / citrix.getTotalDeliveryControllers();

        return availablePercent < 50 ? 50 : 0;
    }

    private int logonDurationIndicator(
            CitrixMetricsHistory citrix
    ) {

        if (citrix.getAverageLogonDurationSeconds() > 60) {

            return 100;
        }

        if (citrix.getAverageLogonDurationSeconds() > 20) {

            return 50;
        }

        return 0;
    }

    private int serverLoadIndicator(
            CitrixMetricsHistory citrix
    ) {

        if (citrix.getServerLoadPercent() >= 67) {

            return 100;
        }

        if (citrix.getServerLoadPercent() >= 34) {

            return 50;
        }

        return 0;
    }

    private int failedLogonsIndicator(
            CitrixMetricsHistory citrix
    ) {

        if (citrix.getFailedLogons() > 30) {

            return 100;
        }

        if (citrix.getFailedLogons() >= 11) {

            return 50;
        }

        return 0;
    }

    private int sharePointStorageIndicator(
            Microsoft365MetricsHistory microsoft365
    ) {

        if (microsoft365.getSharePointStoragePercent() > 90) {

            return 100;
        }

        if (microsoft365.getSharePointStoragePercent() >= 80) {

            return 50;
        }

        return 0;
    }

    private int usersWithoutMfaIndicator(
            Microsoft365MetricsHistory microsoft365
    ) {

        if (microsoft365.getUsersWithoutMfa() > 3) {

            return 100;
        }

        if (microsoft365.getUsersWithoutMfa() >= 1) {

            return 50;
        }

        return 0;
    }

    private int secretsIndicator(
            Microsoft365MetricsHistory microsoft365
    ) {

        return microsoft365.getAppsSecretsExpiringSoon() > 0 ? 50 : 0;
    }

    private int nonCompliantDevicesIndicator(
            Microsoft365MetricsHistory microsoft365
    ) {

        if (microsoft365.getNonCompliantDevices() > 100) {

            return 100;
        }

        if (microsoft365.getNonCompliantDevices() >= 51) {

            return 50;
        }

        return 0;
    }

    private int outdatedWindowsIndicator(
            Microsoft365MetricsHistory microsoft365
    ) {

        return microsoft365.getOutdatedWindowsDevices() > 0 ? 50 : 0;
    }

    private int devicesWithoutEncryptionIndicator(
            Microsoft365MetricsHistory microsoft365
    ) {

        return microsoft365.getDevicesWithoutEncryption() > 5 ? 100 : 0;
    }

    private int openTicketsIndicator(
            GlpiMetricsHistory glpi
    ) {

        if (glpi.getOpenTickets() >= 201) {

            return 100;
        }

        if (glpi.getOpenTickets() >= 101) {

            return 50;
        }

        return 0;
    }

    private int criticalTicketsIndicator(
            GlpiMetricsHistory glpi
    ) {

        if (glpi.getCriticalOpenTickets() > 10) {

            return 100;
        }

        if (glpi.getCriticalOpenTickets() >= 1) {

            return 50;
        }

        return 0;
    }

    private int closedPercentageIndicator(
            int created,
            int closed
    ) {

        if (created <= 0) {

            return 0;
        }

        int closedPercent =
                closed * 100 / created;

        return closedPercent >= 50 ? 0 : 50;
    }

    private int countIndicator(int value) {

        return value > 0 ? 50 : 0;
    }

    private int colorToAffection(String status) {

        if (isRed(status)) {

            return 100;
        }

        if (isYellow(status)) {

            return 50;
        }

        return 0;
    }

    private String colorByPercentage(int percentage) {

        if (percentage >= 67) {

            return "RED";
        }

        if (percentage >= 34) {

            return "YELLOW";
        }

        return "GREEN";
    }

    private String applyFreshnessToColor(
            String color,
            String dataStatus
    ) {

        if ("OK".equalsIgnoreCase(dataStatus)) {

            return color;
        }

        if (isRed(color)) {

            return color;
        }

        return "YELLOW";
    }

    private int weightedAverage(
            int firstValue,
            int firstWeight,
            int secondValue,
            int secondWeight,
            int thirdValue,
            int thirdWeight,
            int fourthValue,
            int fourthWeight
    ) {

        int totalWeight =
                firstWeight + secondWeight + thirdWeight + fourthWeight;

        return clamp(
                (
                        firstValue * firstWeight
                                + secondValue * secondWeight
                                + thirdValue * thirdWeight
                                + fourthValue * fourthWeight
                ) / totalWeight
        );
    }

    private int average(int... values) {

        if (values.length == 0) {

            return 0;
        }

        int total =
                0;

        for (int value : values) {

            total += value;
        }

        return clamp(total / values.length);
    }

    private boolean hasMicrosoftServiceAlert(
            Microsoft365MetricsHistory microsoft365
    ) {

        return isMicrosoftServiceAlert(microsoft365.getOutlookStatus())
                || isMicrosoftServiceAlert(microsoft365.getTeamsStatus())
                || isMicrosoftServiceAlert(
                        microsoft365.getSharePointStatus()
                );
    }

    private boolean isMicrosoftServiceAlert(String status) {

        return "DEGRADED".equalsIgnoreCase(status)
                || "INCIDENT".equalsIgnoreCase(status);
    }

    private boolean isGreen(String status) {

        // Un estado null, vacío o
        // desconocido no debe tratarse
        // como correcto porque podría
        // ocultar falta de datos.

        return "GREEN".equalsIgnoreCase(status);
    }

    private boolean isYellow(String status) {

        return "YELLOW".equalsIgnoreCase(status);
    }

    private boolean isRed(String status) {

        return "RED".equalsIgnoreCase(status);
    }

    private String calculateDataStatus(
            Optional<? extends Object> snapshot
    ) {

        if (snapshot.isEmpty()) {

            return "NO_DATA";
        }

        if (snapshot.get() instanceof CitrixMetricsHistory citrix) {

            return calculateDataStatus(citrix.getCollectedAt());
        }

        if (snapshot.get() instanceof Microsoft365MetricsHistory microsoft365) {

            return calculateDataStatus(microsoft365.getCollectedAt());
        }

        if (snapshot.get() instanceof GlpiMetricsHistory glpi) {

            return calculateDataStatus(glpi.getCollectedAt());
        }

        return "NO_DATA";
    }

    private String calculateDataStatus(
            LocalDateTime collectedAt
    ) {

        // dataStatus se calcula con
        // el margen del scheduler:
        // OK menos de 2 minutos,
        // STALE más de 2 minutos,
        // NO_DATA sin snapshot.

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

    private String calculateGlobalDataStatus(
            String arubaDataStatus,
            String citrixDataStatus,
            String microsoft365DataStatus,
            String glpiDataStatus
    ) {

        if ("NO_DATA".equalsIgnoreCase(arubaDataStatus)
                || "NO_DATA".equalsIgnoreCase(citrixDataStatus)
                || "NO_DATA".equalsIgnoreCase(microsoft365DataStatus)
                || "NO_DATA".equalsIgnoreCase(glpiDataStatus)) {

            return "NO_DATA";
        }

        if ("STALE".equalsIgnoreCase(arubaDataStatus)
                || "STALE".equalsIgnoreCase(citrixDataStatus)
                || "STALE".equalsIgnoreCase(microsoft365DataStatus)
                || "STALE".equalsIgnoreCase(glpiDataStatus)) {

            return "STALE";
        }

        return "OK";
    }

    private LocalDateTime latestCollectedAt(
            LocalDateTime arubaLastUpdated,
            Optional<CitrixMetricsHistory> citrixSnapshot,
            Optional<Microsoft365MetricsHistory> microsoft365Snapshot,
            Optional<GlpiMetricsHistory> glpiSnapshot
    ) {

        LocalDateTime latest =
                arubaLastUpdated;

        if (citrixSnapshot.isPresent()) {

            latest = newer(latest, citrixSnapshot.get().getCollectedAt());
        }

        if (microsoft365Snapshot.isPresent()) {

            latest = newer(
                    latest,
                    microsoft365Snapshot.get().getCollectedAt()
            );
        }

        if (glpiSnapshot.isPresent()) {

            latest = newer(latest, glpiSnapshot.get().getCollectedAt());
        }

        return latest;
    }

    private String normalizeDataStatus(String dataStatus) {

        // Un estado vacío no debe tratarse
        // como fresco. Ante la duda se
        // considera ausencia de datos.

        if (dataStatus == null
                || dataStatus.isBlank()) {

            return "NO_DATA";
        }

        return dataStatus;
    }

    private LocalDateTime newer(
            LocalDateTime current,
            LocalDateTime candidate
    ) {

        if (candidate == null) {

            return current;
        }

        if (current == null || candidate.isAfter(current)) {

            return candidate;
        }

        return current;
    }

    private int clamp(int value) {

        if (value < 0) {

            return 0;
        }

        if (value > 100) {

            return 100;
        }

        return value;
    }
}
