package com.tfg.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.ArubaNetworkStatusDto;
import com.tfg.dashboard.dto.summary.ArubaSummary;
import com.tfg.dashboard.model.CitrixMetricsHistory;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.model.Microsoft365MetricsHistory;

class GlobalKpiCalculationServiceTest {

    private KpiScoringService scoringService;
    private GlobalKpiCalculationService service;
    private KpiProperties kpiProperties;

    @BeforeEach
    void setUp() {

        kpiProperties =
                new KpiProperties();
        scoringService =
                new KpiScoringService(kpiProperties);
        service =
                new GlobalKpiCalculationService(scoringService, kpiProperties);
    }

    @Test
    void globalStatusUsesConfiguredPlatformWeights() {

        int globalStatus =
                service.weightedAverage(
                        100, 40,
                        50, 30,
                        0, 20,
                        0, 10
                );

        assertThat(globalStatus)
                .isEqualTo(55);
        assertThat(scoringService.statusFromAffection(globalStatus))
                .isEqualTo("YELLOW");
    }

    @Test
    void affectionBoundariesUseGreenYellowAndRedScale() {

        assertThat(scoringService.statusFromAffection(0))
                .isEqualTo("GREEN");
        assertThat(scoringService.statusFromAffection(33))
                .isEqualTo("GREEN");
        assertThat(scoringService.statusFromAffection(34))
                .isEqualTo("YELLOW");
        assertThat(scoringService.statusFromAffection(66))
                .isEqualTo("YELLOW");
        assertThat(scoringService.statusFromAffection(67))
                .isEqualTo("RED");
        assertThat(scoringService.statusFromAffection(100))
                .isEqualTo("RED");
    }

    @Test
    void platformHealthIndexesAreCalculatedFromRepresentativeSignals() {

        ArubaSummary aruba =
                aruba();
        CitrixMetricsHistory citrix =
                citrix();
        Microsoft365MetricsHistory microsoft365 =
                microsoft365();
        GlpiMetricsHistory glpi =
                glpi();

        assertThat(service.calculateArubaNetworkAffection(aruba))
                .isEqualTo(75);
        assertThat(service.calculateCitrixHealthAffection(citrix))
                .isEqualTo(40);
        assertThat(service.calculateMicrosoft365HealthAffection(microsoft365))
                .isEqualTo(50);
        assertThat(service.calculateGlpiHealthAffection(glpi))
                .isEqualTo(37);
    }

    @Test
    void transversalKpisAreCalculatedWithCurrentRules() {

        ArubaSummary aruba =
                aruba();
        CitrixMetricsHistory citrix =
                citrix();
        Microsoft365MetricsHistory microsoft365 =
                microsoft365();
        GlpiMetricsHistory glpi =
                glpi();

        int globalStatus =
                service.weightedAverage(
                        service.calculateArubaNetworkAffection(aruba), 40,
                        service.calculateCitrixHealthAffection(citrix), 30,
                        service.calculateMicrosoft365HealthAffection(microsoft365), 20,
                        service.calculateGlpiHealthAffection(glpi), 10
                );

        assertThat(globalStatus)
                .isEqualTo(55);
        assertThat(service.calculateGlobalCriticality(aruba,citrix,microsoft365,glpi))
                .isEqualTo(37);
        assertThat(service.calculateGlobalAvailability(aruba,citrix,microsoft365,glpi))
                .isEqualTo(24);
        assertThat(service.calculateOperationalPressure(aruba,citrix,microsoft365,glpi))
                .isEqualTo(45);
        assertThat(service.calculateTechnicalDegradation(aruba,citrix,microsoft365,glpi))
                .isEqualTo(51);
        assertThat(service.calculateSlaRisk(aruba,citrix,microsoft365,glpi))
                .isEqualTo(44);
        assertThat(service.calculateOperationalBacklog(aruba,citrix,microsoft365,glpi))
                .isEqualTo(38);
        assertThat(service.calculateUserImpact(aruba,citrix,microsoft365,glpi))
                .isEqualTo(33);
        assertThat(service.calculateAffectedServicesPercent(75,40,50,37))
                .isEqualTo(100);
        assertThat(service.calculateAffectedPlatformCount(75,40,50,37))
                .isEqualTo(4);
        assertThat(service.calculateItemsRequiringAction(aruba,citrix,microsoft365,glpi))
                .isEqualTo(76);
    }

    private ArubaSummary aruba() {

        ArubaNetworkStatusDto networkStatus =
                new ArubaNetworkStatusDto();
        networkStatus.setPercentage(75);
        networkStatus.setTechnicalDegradationValue(75);
        networkStatus.setColor("RED");

        ArubaSummary summary =
                new ArubaSummary();
        summary.setTotalAps(10);
        summary.setDownAps(1);
        summary.setInactiveAps(2);
        summary.setFirmwareOutdated(1);
        summary.setTotalSwitches(5);
        summary.setDownSwitches(2);
        summary.setSwitchesFirmwareUpgradeRequired(1);
        summary.setTotalWifiClients(20);
        summary.setMutualiaApsClients(5);
        summary.setMutualiaWifiClients(15);
        summary.setNetworkStatusDetails(networkStatus);

        return summary;
    }

    private CitrixMetricsHistory citrix() {

        CitrixMetricsHistory history =
                new CitrixMetricsHistory();
        history.setActiveSessions(100);
        history.setAvailableDeliveryControllers(2);
        history.setTotalDeliveryControllers(4);
        history.setAverageLogonDurationSeconds(25);
        history.setServerLoadPercent(70);
        history.setFailedLogons(15);
        history.setCitrixHealth("GREEN");

        return history;
    }

    private Microsoft365MetricsHistory microsoft365() {

        Microsoft365MetricsHistory history =
                new Microsoft365MetricsHistory();
        history.setSharePointStoragePercent(85);
        history.setUsersWithoutMfa(4);
        history.setAppsSecretsExpiringSoon(1);
        history.setNonCompliantDevices(60);
        history.setOutdatedWindowsDevices(3);
        history.setDevicesWithoutEncryption(2);
        history.setStaleDevices(8);
        history.setMicrosoft365Health("GREEN");

        return history;
    }

    private GlpiMetricsHistory glpi() {

        GlpiMetricsHistory history =
                new GlpiMetricsHistory();
        history.setOpenTickets(150);
        history.setCriticalOpenTickets(5);
        history.setSlaBreachedTickets(3);
        history.setCreatedToday(10);
        history.setClosedToday(4);
        history.setCreatedThisWeek(20);
        history.setClosedThisWeek(15);

        return history;
    }
}
