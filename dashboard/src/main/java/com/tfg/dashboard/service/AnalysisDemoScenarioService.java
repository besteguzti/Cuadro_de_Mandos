package com.tfg.dashboard.service;

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
        private static final int DEMO_SNAPSHOT_COUNT = 12;
        private static final int FIRST_DEMO_SNAPSHOT_INDEX = 0;
        private static final double DEMO_WAVE_STEP = 0.9;
        private static final double DEMO_WAVE_AMPLITUDE = 24;
        private static final double DEMO_CITRIX_WAVE_FACTOR = 0.7;
        private static final double DEMO_MICROSOFT365_WAVE_FACTOR = 0.45;
        private static final double DEMO_GLPI_PLATFORM_INFLUENCE = 0.18;
        private static final double DEMO_GLPI_WAVE_FACTOR = 0.35;
        private static final int MINIMUM_DAYS_BETWEEN_DEMO_SNAPSHOTS = 1;

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

                LocalDateTime since =
                                LocalDateTime.now().minusDays(analysisSnapshotService.daysFromPeriod(period));

                if (analysisSnapshotRepository.countByTimestampAfter(since) >= MINIMUM_REAL_SNAPSHOTS) {
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

                for (int index = DEMO_SNAPSHOT_COUNT - 1; index >= FIRST_DEMO_SNAPSHOT_INDEX; index--) {

                        double wave = Math.sin(index * DEMO_WAVE_STEP) * DEMO_WAVE_AMPLITUDE;
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

                        snapshot.setTimestamp(LocalDateTime.now().minusDays(
                                        Math.max(
                                                        MINIMUM_DAYS_BETWEEN_DEMO_SNAPSHOTS,
                                                        days / DEMO_SNAPSHOT_COUNT) * index));
                        snapshot.setArubaHealth(aruba);
                        snapshot.setCitrixHealth(citrix);
                        snapshot.setMicrosoft365Health(microsoft365);
                        snapshot.setGlpiHealth(glpiPressure);
                        snapshot.setGlpiOperationalPressure(glpiPressure);
                        snapshot.setTechnicalDegradation(technicalDegradation);
                        snapshot.setUserImpact(userImpact);
                        snapshot.setGlobalStatus(global);
                        snapshot.setArubaStatus(kpiScoringService.statusFromAffection(aruba));
                        snapshot.setCitrixStatus(kpiScoringService.statusFromAffection(citrix));
                        snapshot.setMicrosoft365Status(kpiScoringService.statusFromAffection(microsoft365));
                        snapshot.setGlpiStatus(kpiScoringService.statusFromAffection(glpiPressure));
                        snapshot.setGeneratedScenario(true);

                        analysisSnapshotRepository.save(snapshot);
                }
        }

        private double baseValue(Map<String, Double> values, String code) {

                Double value = values.get(code);

                // Los escenarios demo necesitan una base numerica para poder
                // dibujarse. Si falta el dato real, se parte del minimo de
                // afeccion y el snapshot queda marcado como generatedScenario.
                return value != null
                                ? value
                                : kpiProperties.getAffection().getGreen();
        }

}
