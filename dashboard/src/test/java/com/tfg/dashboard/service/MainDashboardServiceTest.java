package com.tfg.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfg.dashboard.model.ArubaSummary;
import com.tfg.dashboard.model.CitrixMetricsHistory;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.model.MainDashboardSummary;
import com.tfg.dashboard.model.Microsoft365MetricsHistory;
import com.tfg.dashboard.repository.CitrixMetricsHistoryRepository;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;
import com.tfg.dashboard.repository.Microsoft365MetricsHistoryRepository;

@ExtendWith(MockitoExtension.class)
class MainDashboardServiceTest {

    @Mock
    private ArubaService arubaService;

    @Mock
    private CitrixMetricsHistoryRepository citrixRepository;

    @Mock
    private Microsoft365MetricsHistoryRepository microsoft365Repository;

    @Mock
    private GlpiMetricsHistoryRepository glpiRepository;

    private MainDashboardService service;

    @BeforeEach
    void setUp() {

        service = new MainDashboardService(
                arubaService,
                citrixRepository,
                microsoft365Repository,
                glpiRepository
        );
    }

    @Test
    void globalHealthIsRedWhenAnySourceIsRed() {

        baseHealthyData();

        CitrixMetricsHistory citrix =
                healthyCitrix(LocalDateTime.now());
        citrix.setCitrixHealth("RED");

        when(citrixRepository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(citrix));

        MainDashboardSummary summary =
                service.getSummary();

        assertThat(summary.getGlobalHealth()).isEqualTo("RED");
    }

    @Test
    void globalHealthCannotBeGreenWhenAnySourceIsStale() {

        baseHealthyData();

        Microsoft365MetricsHistory microsoft365 =
                healthyMicrosoft365(LocalDateTime.now().minusMinutes(3));

        when(microsoft365Repository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(microsoft365));

        MainDashboardSummary summary =
                service.getSummary();

        assertThat(summary.getDataStatus()).isEqualTo("STALE");
        assertThat(summary.getGlobalHealth()).isNotEqualTo("GREEN");
    }

    @Test
    void globalHealthCannotBeGreenWhenAnySourceHasNoData() {

        baseHealthyData();

        when(glpiRepository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.empty());

        MainDashboardSummary summary =
                service.getSummary();

        assertThat(summary.getDataStatus()).isEqualTo("NO_DATA");
        assertThat(summary.getGlobalHealth()).isNotEqualTo("GREEN");
    }

    @Test
    void servicesWithAlertsIncreasesWhenAPlatformHasAlert() {

        baseHealthyData();

        ArubaSummary aruba =
                healthyAruba();
        aruba.setDownAps(1);

        when(arubaService.getSummary())
                .thenReturn(aruba);

        MainDashboardSummary summary =
                service.getSummary();

        assertThat(summary.getServicesWithAlerts()).isGreaterThan(0);
    }

    @Test
    void capacityPressureIsAlwaysClampedBetweenZeroAndOneHundred() {

        baseHealthyData();

        CitrixMetricsHistory citrix =
                healthyCitrix(LocalDateTime.now());
        citrix.setServerLoadPercent(200);

        Microsoft365MetricsHistory microsoft365 =
                healthyMicrosoft365(LocalDateTime.now());
        microsoft365.setSharePointStoragePercent(200);

        GlpiMetricsHistory glpi =
                healthyGlpi(LocalDateTime.now());
        glpi.setOperationalBacklog(1000);

        when(citrixRepository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(citrix));
        when(microsoft365Repository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(microsoft365));
        when(glpiRepository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(glpi));

        MainDashboardSummary summary =
                service.getSummary();

        assertThat(summary.getCapacityPressure())
                .isBetween(0, 100);
    }

    @Test
    void itemsRequiringActionSumsExpectedSources() {

        baseHealthyData();

        ArubaSummary aruba =
                healthyAruba();
        aruba.setFirmwareOutdated(2);
        aruba.setSwitchesFirmwareUpgradeRequired(3);

        CitrixMetricsHistory citrix =
                healthyCitrix(LocalDateTime.now());
        citrix.setTotalDeliveryControllers(4);
        citrix.setAvailableDeliveryControllers(2);

        Microsoft365MetricsHistory microsoft365 =
                healthyMicrosoft365(LocalDateTime.now());
        microsoft365.setAppsSecretsExpiringSoon(4);
        microsoft365.setNonCompliantDevices(5);
        microsoft365.setStaleDevices(6);

        GlpiMetricsHistory glpi =
                healthyGlpi(LocalDateTime.now());
        glpi.setSlaBreachedTickets(7);

        when(arubaService.getSummary())
                .thenReturn(aruba);
        when(citrixRepository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(citrix));
        when(microsoft365Repository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(microsoft365));
        when(glpiRepository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(glpi));

        MainDashboardSummary summary =
                service.getSummary();

        assertThat(summary.getItemsRequiringAction())
                .isEqualTo(29);
    }

    private void baseHealthyData() {

        when(arubaService.getSummary())
                .thenReturn(healthyAruba());
        when(citrixRepository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(healthyCitrix(LocalDateTime.now())));
        when(microsoft365Repository.findTopByOrderByCollectedAtDesc())
                .thenReturn(
                        Optional.of(healthyMicrosoft365(LocalDateTime.now()))
                );
        when(glpiRepository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(healthyGlpi(LocalDateTime.now())));
    }

    private ArubaSummary healthyAruba() {

        ArubaSummary summary =
                new ArubaSummary();

        summary.setNetworkStatus("GREEN");
        summary.setDataStatus("OK");
        summary.setLastUpdated(LocalDateTime.now());

        return summary;
    }

    private CitrixMetricsHistory healthyCitrix(
            LocalDateTime collectedAt
    ) {

        CitrixMetricsHistory history =
                new CitrixMetricsHistory();

        history.setCitrixHealth("GREEN");
        history.setTotalDeliveryControllers(4);
        history.setAvailableDeliveryControllers(4);
        history.setCollectedAt(collectedAt);

        return history;
    }

    private Microsoft365MetricsHistory healthyMicrosoft365(
            LocalDateTime collectedAt
    ) {

        Microsoft365MetricsHistory history =
                new Microsoft365MetricsHistory();

        history.setMicrosoft365Health("GREEN");
        history.setOutlookStatus("HEALTHY");
        history.setTeamsStatus("HEALTHY");
        history.setSharePointStatus("HEALTHY");
        history.setCollectedAt(collectedAt);

        return history;
    }

    private GlpiMetricsHistory healthyGlpi(
            LocalDateTime collectedAt
    ) {

        GlpiMetricsHistory history =
                new GlpiMetricsHistory();

        history.setCollectedAt(collectedAt);

        return history;
    }
}
