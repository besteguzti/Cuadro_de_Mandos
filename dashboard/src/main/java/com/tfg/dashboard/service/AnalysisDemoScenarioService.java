package com.tfg.dashboard.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.model.AnalysisSnapshot;
import com.tfg.dashboard.repository.AnalysisSnapshotRepository;

/**
 * Gestiona escenarios demo del panel de análisis.
 *
 * Solo genera snapshots cuando no hay histórico real suficiente y los marca con
 * generatedScenario para que el frontend pueda advertir que son datos de demostración.
 */

@Service
public class AnalysisDemoScenarioService {

        private static final int MINIMUM_REAL_SNAPSHOTS = 2;
        private static final int DEMO_SNAPSHOTS_PER_DAY = 4;
        private static final int DEMO_BUCKET_HOURS = 6;
        private static final double DEMO_WAVE_STEP = 0.9;
        private static final double DEMO_WAVE_AMPLITUDE = 24;
        private static final double DEMO_CITRIX_WAVE_FACTOR = 0.7;
        private static final double DEMO_MICROSOFT365_WAVE_FACTOR = 0.45;
        private static final double DEMO_GLPI_PLATFORM_INFLUENCE = 0.18;
        private static final double DEMO_GLPI_WAVE_FACTOR = 0.35;
        private static final int DEMO_BASE_WIFI_CLIENTS = 230;
        private static final int DEMO_BASE_CITRIX_SESSIONS = 460;
        private static final int DEMO_BASE_NON_COMPLIANT_DEVICES = 20;
        private static final int DEMO_LOGON_BASE_SECONDS = 12;
        private static final int DEMO_ARUBA_CLIENT_LOSS_FACTOR = 2;
        private static final int DEMO_CITRIX_SESSION_LOSS_FACTOR = 2;
        private static final int DEMO_LOGON_AFFECTION_DIVISOR = 2;
        private static final int DEMO_FAILED_LOGON_CITRIX_DIVISOR = 4;
        private static final int DEMO_FAILED_LOGON_GLPI_DIVISOR = 10;
        private static final int DEMO_ARUBA_TICKET_BASE = 15;
        private static final int DEMO_CITRIX_TICKET_BASE = 25;
        private static final int DEMO_MICROSOFT365_TICKET_BASE = 12;
        private static final int DEMO_ARUBA_TICKET_DIVISOR = 2;
        private static final int DEMO_CITRIX_TICKET_DIVISOR = 1;
        private static final int DEMO_MICROSOFT365_TICKET_DIVISOR = 2;

        private final AnalysisSnapshotRepository analysisSnapshotRepository;
        private final AnalysisSnapshotService analysisSnapshotService;
        private final TransversalKpiHistoryService historyService;
        private final KpiScoringService kpiScoringService;
        private final TechnicalImpactAnalysisService technicalImpactAnalysisService;
        private final KpiProperties kpiProperties;

        public AnalysisDemoScenarioService(
                        AnalysisSnapshotRepository analysisSnapshotRepository,
                        AnalysisSnapshotService analysisSnapshotService,
                        TransversalKpiHistoryService historyService,
                        KpiScoringService kpiScoringService,
                        TechnicalImpactAnalysisService technicalImpactAnalysisService,
                        KpiProperties kpiProperties) {

                this.analysisSnapshotRepository = analysisSnapshotRepository;
                this.analysisSnapshotService = analysisSnapshotService;
                this.historyService = historyService;
                this.kpiScoringService = kpiScoringService;
                this.technicalImpactAnalysisService = technicalImpactAnalysisService;
                this.kpiProperties = kpiProperties;
        }

        /**
         * Comprueba si hay suficientes snapshots reales para el periodo; si no,
         * persiste un escenario demo controlado.
         */
        public void ensureAnalysisSnapshots(String period) {

                int days = analysisSnapshotService.daysFromPeriod(period);
                LocalDateTime since = LocalDateTime.now().minusDays(days);
                long daysWithUsableSnapshots = analysisSnapshotService.getSnapshots(period)
                                .stream()
                                .filter(snapshot -> snapshot.getTimestamp() != null)
                                .filter(this::hasSpecificRelationData)
                                .map(snapshot -> snapshot.getTimestamp().toLocalDate())
                                .distinct()
                                .count();

                if (analysisSnapshotRepository.countByTimestampAfter(since) >= MINIMUM_REAL_SNAPSHOTS
                                && daysWithUsableSnapshots >= days) {
                        return;
                }

                persistScenarioSnapshots(period);
        }

        /**
         * Genera una serie coherente de snapshots demo a partir de los valores
         * actuales disponibles.
         */
        private void persistScenarioSnapshots(String period) {

                Map<String, Double> values = historyService.calculateCurrentValues();
                int days = analysisSnapshotService.daysFromPeriod(period);

                for (int dayIndex = days - 1; dayIndex >= 0; dayIndex--) {
                        for (int bucket = 0; bucket < DEMO_SNAPSHOTS_PER_DAY; bucket++) {

                        int sequence = dayIndex * DEMO_SNAPSHOTS_PER_DAY + bucket;
                        double wave = Math.sin(sequence * DEMO_WAVE_STEP) * DEMO_WAVE_AMPLITUDE;
                        AnalysisSnapshot snapshot = new AnalysisSnapshot();

                        int aruba = kpiScoringService.clampToInt(
                                        baseValue(values, TransversalKpiHistoryService.ARUBA_NETWORK_AFFECTATION)
                                                        + wave);
                        int citrix = kpiScoringService.clampToInt(
                                        baseValue(values, TransversalKpiHistoryService.CITRIX_TECHNICAL_AFFECTION)
                                                        + wave * DEMO_CITRIX_WAVE_FACTOR);
                        int microsoft365 = kpiScoringService.clampToInt(
                                        baseValue(
                                                        values,
                                                        TransversalKpiHistoryService.MICROSOFT365_TECHNICAL_AFFECTION)
                                                        + wave * DEMO_MICROSOFT365_WAVE_FACTOR);
                        int glpiPressure = kpiScoringService.clampToInt(
                                        baseValue(values, TransversalKpiHistoryService.GLPI_OPERATIONAL_PRESSURE)
                                                        + Math.max(aruba, citrix) * DEMO_GLPI_PLATFORM_INFLUENCE
                                                        + wave * DEMO_GLPI_WAVE_FACTOR);
                        int technicalDegradation =
                                        technicalImpactAnalysisService.calculateTechnicalDegradation(
                                                        aruba,
                                                        citrix,
                                                        microsoft365);
                        int userImpact =
                                        technicalImpactAnalysisService.calculateUserImpact(
                                                        aruba,
                                                        citrix,
                                                        microsoft365,
                                                        glpiPressure);
                        KpiProperties.PlatformWeights globalWeights =
                                        kpiProperties.getWeights().getGlobalStatus();

                        int global = kpiScoringService.clampToInt(
                                        aruba * globalWeights.getAruba()
                                                        + citrix * globalWeights.getCitrix()
                                                        + microsoft365 * globalWeights.getMicrosoft365()
                                                        + glpiPressure * globalWeights.getGlpi());
                        int affectedServicesPercent = affectedServicesPercent(
                                        aruba,
                                        citrix,
                                        microsoft365,
                                        glpiPressure);
                        int arubaOpenTickets = DEMO_ARUBA_TICKET_BASE + aruba / DEMO_ARUBA_TICKET_DIVISOR;
                        int citrixOpenTickets = DEMO_CITRIX_TICKET_BASE + citrix / DEMO_CITRIX_TICKET_DIVISOR;
                        int microsoft365OpenTickets =
                                        DEMO_MICROSOFT365_TICKET_BASE
                                                        + microsoft365 / DEMO_MICROSOFT365_TICKET_DIVISOR;
                        int glpiOpenTickets =
                                        arubaOpenTickets
                                                        + citrixOpenTickets
                                                        + microsoft365OpenTickets;

                        snapshot.setTimestamp(
                                        LocalDate.now()
                                                        .minusDays(dayIndex)
                                                        .atStartOfDay()
                                                        .plusHours((long) bucket * DEMO_BUCKET_HOURS));
                        snapshot.setArubaHealth(aruba);
                        snapshot.setCitrixHealth(citrix);
                        snapshot.setMicrosoft365Health(microsoft365);
                        snapshot.setGlpiHealth(glpiPressure);
                        snapshot.setGlpiOperationalPressure(glpiPressure);
                        snapshot.setTechnicalDegradation(technicalDegradation);
                        snapshot.setUserImpact(userImpact);
                        snapshot.setGlobalStatus(global);
                        int arubaWifiClients = Math.max(
                                        0,
                                        DEMO_BASE_WIFI_CLIENTS - aruba * DEMO_ARUBA_CLIENT_LOSS_FACTOR);
                        int citrixActiveSessions = Math.min(
                                        arubaWifiClients,
                                        Math.max(
                                                        0,
                                                        DEMO_BASE_CITRIX_SESSIONS
                                                                        - aruba
                                                                                        * DEMO_CITRIX_SESSION_LOSS_FACTOR));

                        snapshot.setArubaWifiClients(arubaWifiClients);
                        snapshot.setCitrixAverageLogonDurationSeconds(
                                        DEMO_LOGON_BASE_SECONDS + citrix / DEMO_LOGON_AFFECTION_DIVISOR);
                        snapshot.setCitrixActiveSessions(citrixActiveSessions);
                        snapshot.setCitrixFailedLogons(Math.max(
                                        0,
                                        citrix / DEMO_FAILED_LOGON_CITRIX_DIVISOR
                                                        + glpiPressure / DEMO_FAILED_LOGON_GLPI_DIVISOR));
                        snapshot.setGlpiOpenTickets(glpiOpenTickets);
                        snapshot.setArubaOpenTickets(arubaOpenTickets);
                        snapshot.setCitrixOpenTickets(citrixOpenTickets);
                        snapshot.setMicrosoft365OpenTickets(microsoft365OpenTickets);
                        snapshot.setMicrosoft365NonCompliantDevices(DEMO_BASE_NON_COMPLIANT_DEVICES + microsoft365);
                        snapshot.setAffectedServicesPercent(affectedServicesPercent);
                        snapshot.setArubaStatus(kpiScoringService.statusFromAffection(aruba));
                        snapshot.setCitrixStatus(kpiScoringService.statusFromAffection(citrix));
                        snapshot.setMicrosoft365Status(kpiScoringService.statusFromAffection(microsoft365));
                        snapshot.setGlpiStatus(kpiScoringService.statusFromAffection(glpiPressure));
                        snapshot.setGeneratedScenario(true);

                        analysisSnapshotRepository.save(snapshot);
                        }
                }
        }

        private double baseValue(Map<String, Double> values, String code) {

                Double value = values.get(code);

                // Los escenarios demo necesitan una base numerica para poder
                // dibujarse. Si falta el dato real, se parte del minimo de
                // afección y el snapshot queda marcado como generatedScenario.
                return value != null
                                ? value
                                : kpiProperties.getAffection().getGreen();
        }

        private boolean hasSpecificRelationData(AnalysisSnapshot snapshot) {

                return snapshot.getArubaHealth() != null
                                && snapshot.getCitrixAverageLogonDurationSeconds() != null
                                && snapshot.getArubaWifiClients() != null
                                && snapshot.getCitrixActiveSessions() != null
                                && snapshot.getCitrixFailedLogons() != null
                                && snapshot.getCitrixOpenTickets() != null
                                && snapshot.getMicrosoft365NonCompliantDevices() != null
                                && snapshot.getMicrosoft365OpenTickets() != null
                                && snapshot.getAffectedServicesPercent() != null
                                && snapshot.getGlpiOperationalPressure() != null;
        }

        private int affectedServicesPercent(
                        int aruba,
                        int citrix,
                        int microsoft365,
                        int glpiPressure) {

                int affectedPlatforms = 0;
                int threshold = kpiProperties.getStatus().getYellowMin();

                if (aruba >= threshold) {
                        affectedPlatforms++;
                }

                if (citrix >= threshold) {
                        affectedPlatforms++;
                }

                if (microsoft365 >= threshold) {
                        affectedPlatforms++;
                }

                if (glpiPressure >= threshold) {
                        affectedPlatforms++;
                }

                return affectedPlatforms * (kpiProperties.getStatus().getMax() / 4);
        }

}
