package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.dto.AnalyticsComparePoint;
import com.tfg.dashboard.dto.OperationalImpactAnalysisResponse;
import com.tfg.dashboard.dto.TechnicalTimelinePointDto;
import com.tfg.dashboard.model.AnalysisSnapshot;
import com.tfg.dashboard.repository.AnalysisSnapshotRepository;

/**
 * Orquesta los datos del panel de análisis exploratorio.
 *
 * Mantiene la fachada del módulo de análisis y delega la construcción de
 * snapshots y respuestas a servicios especializados.
 */
@Service
public class AnalysisSnapshotOrchestrator {

        private final AnalysisSnapshotRepository analysisSnapshotRepository;
        private final AnalysisSnapshotQueryService snapshotQueryService;
        private final AnalysisSnapshotBuilderService snapshotBuilderService;
        private final AnalysisPanelResponseService panelResponseService;
        private final AnalysisDemoScenarioService demoScenarioService;
        private final TechnicalImpactAnalysisService technicalImpactAnalysisService;
        private final TechnicalTimelineService technicalTimelineService;

        public AnalysisSnapshotOrchestrator(
                        AnalysisSnapshotRepository analysisSnapshotRepository,
                        AnalysisSnapshotQueryService snapshotQueryService,
                        AnalysisSnapshotBuilderService snapshotBuilderService,
                        AnalysisPanelResponseService panelResponseService,
                        AnalysisDemoScenarioService demoScenarioService,
                        TechnicalImpactAnalysisService technicalImpactAnalysisService,
                        TechnicalTimelineService technicalTimelineService) {

                this.analysisSnapshotRepository = analysisSnapshotRepository;
                this.snapshotQueryService = snapshotQueryService;
                this.snapshotBuilderService = snapshotBuilderService;
                this.panelResponseService = panelResponseService;
                this.demoScenarioService = demoScenarioService;
                this.technicalImpactAnalysisService = technicalImpactAnalysisService;
                this.technicalTimelineService = technicalTimelineService;
        }

        /**
         * Asegura que exista histórico suficiente para el periodo y devuelve los
         * snapshots ordenados.
         */
        public List<AnalysisSnapshot> getAnalysisSnapshots(String period) {

                demoScenarioService.ensureAnalysisSnapshots(period);
                return snapshotQueryService.getAnalysisSnapshots(period);
        }

        /**
         * Construye la respuesta principal del panel de análisis exploratorio.
         *
         * El parametro platform se conserva por compatibilidad historica, pero
         * la version actual devuelve un panel agregado sin selector de plataforma.
         */
        public OperationalImpactAnalysisResponse getGlpiPlatformRelation(
                        String platform,
                        String period) {

                return getGlpiPlatformRelation(period);
        }

        public OperationalImpactAnalysisResponse getGlpiPlatformRelation(String period) {

                demoScenarioService.ensureAnalysisSnapshots(period);
                return panelResponseService.buildOperationalImpactResponse(period);
        }

        public List<AnalyticsComparePoint> getTechnicalDegradationImpact(String period) {

                demoScenarioService.ensureAnalysisSnapshots(period);
                return technicalImpactAnalysisService.buildTechnicalImpactPoints(getAnalysisSnapshots(period));
        }

        public List<TechnicalTimelinePointDto> getPlatformEvolution(String period) {

                demoScenarioService.ensureAnalysisSnapshots(period);
                return technicalTimelineService.buildPlatformEvolution(getAnalysisSnapshots(period));
        }

        /**
         * Persiste una captura real de análisis calculada a partir del estado
         * actual del dashboard.
         */
        public void saveAnalysisSnapshot(LocalDateTime collectedAt) {

                analysisSnapshotRepository.save(snapshotBuilderService.buildAnalysisSnapshot(collectedAt, false));
        }
}
