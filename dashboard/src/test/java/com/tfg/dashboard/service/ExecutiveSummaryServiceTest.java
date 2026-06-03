package com.tfg.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.ExecutiveSummaryDto;
import com.tfg.dashboard.dto.KpiResultDto;
import com.tfg.dashboard.dto.KpiStatus;
import com.tfg.dashboard.model.AnalysisSnapshot;
import com.tfg.dashboard.model.CitrixMetricsHistory;
import com.tfg.dashboard.dto.summary.MainDashboardSummary;
import com.tfg.dashboard.repository.AnalysisSnapshotRepository;
import com.tfg.dashboard.repository.CitrixMetricsHistoryRepository;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;
import com.tfg.dashboard.repository.Microsoft365MetricsHistoryRepository;

@ExtendWith(MockitoExtension.class)
class ExecutiveSummaryServiceTest {

    @Mock
    private MainDashboardService mainDashboardService;

    @Mock
    private ArubaService arubaService;

    @Mock
    private CitrixMetricsHistoryRepository citrixRepository;

    @Mock
    private Microsoft365MetricsHistoryRepository microsoft365Repository;

    @Mock
    private GlpiMetricsHistoryRepository glpiRepository;

    @Mock
    private AnalysisSnapshotRepository analysisSnapshotRepository;

    private ExecutiveSummaryService service;
    private KpiProperties kpiProperties;

    @BeforeEach
    void setUp() {

        kpiProperties =
                new KpiProperties();

        service = new ExecutiveSummaryService(
                mainDashboardService,
                arubaService,
                citrixRepository,
                microsoft365Repository,
                glpiRepository,
                analysisSnapshotRepository,
                kpiProperties
        );
    }

    @Test
    void executiveSummaryDetectsMainPlatformPriorityAndTrend() {

        when(mainDashboardService.getSummary())
                .thenReturn(summaryWithPlatformComponents());

        CitrixMetricsHistory citrix =
                new CitrixMetricsHistory();
        citrix.setActiveSessions(120);

        when(citrixRepository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(citrix));

        when(analysisSnapshotRepository.findTop5ByOrderByTimestampDesc())
                .thenReturn(List.of(snapshot(20), snapshot(30), snapshot(40)));

        ExecutiveSummaryDto executiveSummary =
                service.getExecutiveSummary();

        assertThat(executiveSummary.getMainAffectedPlatform())
                .isEqualTo("Citrix");
        assertThat(executiveSummary.getPriority())
                .isEqualTo("HIGH");
        assertThat(executiveSummary.getTrend())
                .isEqualTo("WORSENING");
        assertThat(executiveSummary.getEstimatedAffectedUsers())
                .isEqualTo("120 sesiones activas observadas");
        assertThat(executiveSummary.getAffectedServices())
                .contains("Acceso a aplicaciones corporativas");
    }

    @Test
    void trendIsImprovingWhenCurrentGlobalStatusDropsByAtLeastTenPoints() {

        when(mainDashboardService.getSummary())
                .thenReturn(summaryWithGlobalStatus(20));
        when(analysisSnapshotRepository.findTop5ByOrderByTimestampDesc())
                .thenReturn(List.of(snapshot(45), snapshot(40), snapshot(35)));

        ExecutiveSummaryDto executiveSummary =
                service.getExecutiveSummary();

        assertThat(executiveSummary.getTrend())
                .isEqualTo("IMPROVING");
    }

    @Test
    void trendIsStableWhenCurrentGlobalStatusChangesLessThanTenPoints() {

        when(mainDashboardService.getSummary())
                .thenReturn(summaryWithGlobalStatus(44));
        when(analysisSnapshotRepository.findTop5ByOrderByTimestampDesc())
                .thenReturn(List.of(snapshot(40), snapshot(45), snapshot(42)));

        ExecutiveSummaryDto executiveSummary =
                service.getExecutiveSummary();

        assertThat(executiveSummary.getTrend())
                .isEqualTo("STABLE");
    }

    @Test
    void trendIsWorseningWhenCurrentGlobalStatusRisesByAtLeastTenPoints() {

        when(mainDashboardService.getSummary())
                .thenReturn(summaryWithGlobalStatus(70));
        when(analysisSnapshotRepository.findTop5ByOrderByTimestampDesc())
                .thenReturn(List.of(snapshot(45), snapshot(50), snapshot(55)));

        ExecutiveSummaryDto executiveSummary =
                service.getExecutiveSummary();

        assertThat(executiveSummary.getTrend())
                .isEqualTo("WORSENING");
    }

    @Test
    void trendIsControlledWhenThereIsNoHistoricalData() {

        when(mainDashboardService.getSummary())
                .thenReturn(summaryWithGlobalStatus(70));
        when(analysisSnapshotRepository.findTop5ByOrderByTimestampDesc())
                .thenReturn(List.of());

        ExecutiveSummaryDto executiveSummary =
                service.getExecutiveSummary();

        assertThat(executiveSummary.getTrend())
                .isEqualTo("STABLE");
    }

    @Test
    void scenarioWithCitrixRedKeepsOperationalPriorityAtLeastMedium() {

        ExecutiveSummaryDto summary =
                service.buildScenarioSummary(
                        summaryWithPlatformScores("GREEN", 30, 50, 0, 100, 0, 0));

        assertThat(List.of(summary.getMainAffectedPlatform(), summary.getProbableOrigin()))
                .contains("Citrix");
        assertThat(summary.getAffectedServices())
                .contains("Acceso a aplicaciones corporativas");
        assertThat(summary.getPriority())
                .isEqualTo("MEDIUM");
        assertThat(summary.getFirstAction())
                .contains("Delivery Controllers")
                .contains("logon duration")
                .contains("errores de inicio");
        assertThat(summary.getSummaryText())
                .contains("Citrix")
                .contains("critico");
    }

    @Test
    void scenarioWithMicrosoft365RedKeepsOperationalPriorityAtLeastMedium() {

        ExecutiveSummaryDto summary =
                service.buildScenarioSummary(
                        summaryWithPlatformScores("GREEN", 20, 50, 0, 0, 100, 0));

        assertThat(List.of(summary.getMainAffectedPlatform(), summary.getProbableOrigin()))
                .contains("Microsoft 365");
        assertThat(summary.getAffectedServices())
                .contains("Servicios cloud / identidad / colaboracion");
        assertThat(summary.getPriority())
                .isEqualTo("MEDIUM");
        assertThat(summary.getFirstAction())
                .contains("usuarios sin MFA")
                .contains("SharePoint")
                .contains("cifrado");
        assertThat(summary.getSummaryText())
                .contains("Microsoft 365")
                .contains("critico");
    }

    @Test
    void scenarioWithCitrixAndMicrosoft365RedIsHighPriority() {

        ExecutiveSummaryDto summary =
                service.buildScenarioSummary(
                        summaryWithPlatformScores("YELLOW", 50, 50, 0, 100, 100, 0));

        assertThat(summary.getPriority())
                .isEqualTo("HIGH");
        assertThat(summary.getImpactLevel())
                .isEqualTo("HIGH");
        assertThat(summary.getAffectedServices())
                .contains(
                        "Acceso a aplicaciones corporativas",
                        "Servicios cloud / identidad / colaboracion");
        assertThat(summary.getSummaryText())
                .contains("varias plataformas")
                .contains("Citrix")
                .contains("Microsoft 365");
    }

    @Test
    void scenarioWithGreenWeightedGlobalButRedPlatformDoesNotHideCriticalPlatform() {

        ExecutiveSummaryDto summary =
                service.buildScenarioSummary(
                        summaryWithPlatformScores("GREEN", 30, 50, 0, 100, 0, 0));

        assertThat(summary.getGlobalStatus())
                .isEqualTo("GREEN");
        assertThat(summary.getPriority())
                .isNotEqualTo("LOW");
        assertThat(summary.getSummaryText())
                .contains("aunque")
                .contains("Citrix")
                .contains("critico");
    }

    @Test
    void scenarioWithArubaRedKeepsNetworkActionAndAffectedService() {

        ExecutiveSummaryDto summary =
                service.buildScenarioSummary(
                        summaryWithPlatformScores("RED", 40, 50, 100, 0, 0, 0));

        assertThat(summary.getMainAffectedPlatform())
                .isEqualTo("Aruba");
        assertThat(summary.getAffectedServices())
                .contains("Red corporativa / conectividad");
        assertThat(summary.getPriority())
                .isEqualTo("MEDIUM");
        assertThat(summary.getFirstAction())
                .contains("APs")
                .contains("switches")
                .contains("clientes WiFi");
    }

    private MainDashboardSummary summaryWithPlatformComponents() {

        MainDashboardSummary summary =
                new MainDashboardSummary();

        summary.setGlobalHealth("YELLOW");
        summary.setGlobalHealthPercentage(60);
        summary.setUserImpact(70);
        summary.setSlaRisk(20);
        summary.setGlobalCriticality(20);
        summary.setKpis(List.of(new KpiResultDto(
                "global_status",
                "Estado global",
                60,
                KpiStatus.YELLOW,
                null,
                null,
                LocalDateTime.now(),
                "OK",
                null,
                List.of(
                        component("aruba_network_affectation", 50),
                        component("citrix_health", 100),
                        component("microsoft365_health", 20),
                        component("glpi_health", 20)
                )
        )));

        return summary;
    }

    private MainDashboardSummary summaryWithGlobalStatus(int globalStatus) {

        MainDashboardSummary summary =
                new MainDashboardSummary();

        summary.setGlobalHealth("YELLOW");
        summary.setGlobalHealthPercentage(globalStatus);
        summary.setUserImpact(20);
        summary.setSlaRisk(20);
        summary.setGlobalCriticality(20);
        summary.setKpis(List.of(new KpiResultDto(
                "global_status",
                "Estado global",
                globalStatus,
                KpiStatus.YELLOW,
                null,
                null,
                LocalDateTime.now(),
                "OK",
                null,
                List.of(
                        component("aruba_network_affectation", 0),
                        component("citrix_health", 0),
                        component("microsoft365_health", 0),
                        component("glpi_health", 0)
                )
        )));

        return summary;
    }

    private MainDashboardSummary summaryWithPlatformScores(
            String globalStatus,
            int globalPercentage,
            int userImpact,
            int arubaScore,
            int citrixScore,
            int microsoft365Score,
            int glpiScore
    ) {

        MainDashboardSummary summary =
                new MainDashboardSummary();

        summary.setGlobalHealth(globalStatus);
        summary.setGlobalHealthPercentage(globalPercentage);
        summary.setUserImpact(userImpact);
        summary.setSlaRisk(20);
        summary.setGlobalCriticality(20);
        summary.setKpis(List.of(new KpiResultDto(
                "global_status",
                "Estado global",
                globalPercentage,
                KpiStatus.YELLOW,
                null,
                null,
                LocalDateTime.now(),
                "OK",
                null,
                List.of(
                        component("aruba_network_affectation", arubaScore),
                        component("citrix_health", citrixScore),
                        component("microsoft365_health", microsoft365Score),
                        component("glpi_health", glpiScore)
                )
        )));

        return summary;
    }

    private KpiResultDto component(String id, int score) {

        return new KpiResultDto(
                id,
                id,
                score,
                KpiStatus.YELLOW,
                null,
                null,
                null,
                null,
                score,
                List.of()
        );
    }

    private AnalysisSnapshot snapshot(int globalStatus) {

        AnalysisSnapshot snapshot =
                new AnalysisSnapshot();

        snapshot.setTimestamp(LocalDateTime.now());
        snapshot.setGlobalStatus(globalStatus);

        return snapshot;
    }
}
