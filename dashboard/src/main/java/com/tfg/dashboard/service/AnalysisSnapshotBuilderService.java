package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.model.AnalysisSnapshot;
import com.tfg.dashboard.model.CitrixMetricsHistory;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.model.Microsoft365MetricsHistory;
import com.tfg.dashboard.dto.summary.ArubaSummary;
import com.tfg.dashboard.dto.summary.MainDashboardSummary;
import com.tfg.dashboard.repository.CitrixMetricsHistoryRepository;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;
import com.tfg.dashboard.repository.Microsoft365MetricsHistoryRepository;

/**
 * Construye objetos AnalysisSnapshot a partir del estado actual del dashboard.
 */
@Service
public class AnalysisSnapshotBuilderService {

    private final TransversalKpiHistoryService historyService;
    private final MainDashboardService mainDashboardService;
    private final KpiScoringService kpiScoringService;
    private final ArubaService arubaService;
    private final CitrixMetricsHistoryRepository citrixRepository;
    private final Microsoft365MetricsHistoryRepository microsoft365Repository;
    private final GlpiMetricsHistoryRepository glpiRepository;
    private final KpiProperties kpiProperties;

    public AnalysisSnapshotBuilderService(
                    TransversalKpiHistoryService historyService,
                    MainDashboardService mainDashboardService,
                    KpiScoringService kpiScoringService,
                    ArubaService arubaService,
                    CitrixMetricsHistoryRepository citrixRepository,
                    Microsoft365MetricsHistoryRepository microsoft365Repository,
                    GlpiMetricsHistoryRepository glpiRepository,
                    KpiProperties kpiProperties) {

        this.historyService = historyService;
        this.mainDashboardService = mainDashboardService;
        this.kpiScoringService = kpiScoringService;
        this.arubaService = arubaService;
        this.citrixRepository = citrixRepository;
        this.microsoft365Repository = microsoft365Repository;
        this.glpiRepository = glpiRepository;
        this.kpiProperties = kpiProperties;
    }

    public AnalysisSnapshot buildAnalysisSnapshot(LocalDateTime collectedAt, boolean generatedScenario) {

        Map<String, Double> values = historyService.calculateCurrentValues();
        MainDashboardSummary summary = mainDashboardService.getSummary();
        ArubaSummary arubaSummary = arubaService.getSummary();
        CitrixMetricsHistory citrixSnapshot =
                        citrixRepository.findTopByOrderByCollectedAtDesc().orElse(null);
        Microsoft365MetricsHistory microsoft365Snapshot =
                        microsoft365Repository.findTopByOrderByCollectedAtDesc().orElse(null);
        GlpiMetricsHistory glpiSnapshot =
                        glpiRepository.findTopByOrderByCollectedAtDesc().orElse(null);
        AnalysisSnapshot snapshot = new AnalysisSnapshot();

        int aruba = safeCurrentScore(values, TransversalKpiHistoryService.ARUBA_NETWORK_AFFECTATION);
        int citrix = safeCurrentScore(values, TransversalKpiHistoryService.CITRIX_TECHNICAL_AFFECTION);
        int microsoft365 = safeCurrentScore(values,
                        TransversalKpiHistoryService.MICROSOFT365_TECHNICAL_AFFECTION);
        int glpi = kpiScoringService.clampToInt(
                        safeCurrentScore(
                                        values,
                                        TransversalKpiHistoryService.GLPI_OPERATIONAL_PRESSURE));

        snapshot.setTimestamp(collectedAt);
        snapshot.setArubaHealth(aruba);
        snapshot.setCitrixHealth(citrix);
        snapshot.setMicrosoft365Health(microsoft365);
        snapshot.setGlpiHealth(glpi);
        snapshot.setGlpiOperationalPressure(
                        safeCurrentScore(
                                        values,
                                        TransversalKpiHistoryService.GLPI_OPERATIONAL_PRESSURE));
        snapshot.setTechnicalDegradation(summary.getTechnicalDegradation());
        snapshot.setUserImpact(summary.getUserImpact());
        snapshot.setGlobalStatus(summary.getGlobalHealthPercentage());
        snapshot.setArubaWifiClients(
                        "NO_DATA".equalsIgnoreCase(arubaSummary.getDataStatus())
                                        ? null
                                        : arubaSummary.getTotalWifiClients());
        snapshot.setCitrixAverageLogonDurationSeconds(
                        citrixSnapshot == null
                                        ? null
                                        : citrixSnapshot.getAverageLogonDurationSeconds());
        snapshot.setCitrixActiveSessions(
                        citrixSnapshot == null ? null : citrixSnapshot.getActiveSessions());
        snapshot.setCitrixFailedLogons(
                        citrixSnapshot == null ? null : citrixSnapshot.getFailedLogons());
        snapshot.setGlpiOpenTickets(
                        glpiSnapshot == null ? null : glpiSnapshot.getOpenTickets());
        snapshot.setArubaOpenTickets(
                        glpiSnapshot == null ? null : glpiSnapshot.getArubaOpenTicketsRaw());
        snapshot.setCitrixOpenTickets(
                        glpiSnapshot == null ? null : glpiSnapshot.getCitrixOpenTicketsRaw());
        snapshot.setMicrosoft365OpenTickets(
                        glpiSnapshot == null ? null : glpiSnapshot.getMicrosoft365OpenTicketsRaw());
        snapshot.setMicrosoft365NonCompliantDevices(
                        microsoft365Snapshot == null
                                        ? null
                                        : microsoft365Snapshot.getNonCompliantDevices());
        snapshot.setAffectedServicesPercent(summary.getAffectedServicesPercent());
        snapshot.setArubaStatus(kpiScoringService.statusFromAffection(aruba));
        snapshot.setCitrixStatus(kpiScoringService.statusFromAffection(citrix));
        snapshot.setMicrosoft365Status(kpiScoringService.statusFromAffection(microsoft365));
        snapshot.setGlpiStatus(kpiScoringService.statusFromAffection(glpi));
        snapshot.setGeneratedScenario(generatedScenario);

        return snapshot;
    }

    private int safeCurrentScore(Map<String, Double> values, String code) {

        Double value = values.get(code);

        return value != null
                        ? kpiScoringService.clampToInt(value)
                        : minimumAffection();
    }

    private int minimumAffection() {

        return kpiProperties.getAffection().getGreen();
    }
}
