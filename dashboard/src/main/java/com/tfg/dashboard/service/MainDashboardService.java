package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

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

        summary.setGlobalHealth(
                calculateGlobalHealth(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi,
                        dataStatus
                )
        );
        summary.setGlobalOperationalRisk(
                calculateGlobalOperationalRisk(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                )
        );
        summary.setServicesWithAlerts(
                calculateServicesWithAlerts(aruba, citrix, microsoft365, glpi)
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
                calculateCapacityPressure(citrix, microsoft365, glpi)
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
                clamp(
                        aruba.getDownAps() * 10
                                + aruba.getFirmwareOutdated() * 5
                                + aruba.getDownSwitches() * 15
                                + aruba.getSwitchesFirmwareUpgradeRequired()
                                * 5
                );

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
