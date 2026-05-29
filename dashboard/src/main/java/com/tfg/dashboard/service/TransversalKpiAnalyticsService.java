package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.dto.AnalyticsComparePoint;
import com.tfg.dashboard.dto.OperationalImpactAnalysisResponse;
import com.tfg.dashboard.dto.TechnicalTimelinePointDto;
import com.tfg.dashboard.model.AnalysisSnapshot;

/**
 * Fachada del módulo de análisis transversal.
 *
 * Mantiene estable el contrato usado por los controladores y delega la lógica
 * en servicios especializados de snapshots, relaciones GLPI-plataforma,
 * degradación técnica e históricos.
 */
@Service
public class TransversalKpiAnalyticsService {

        private final AnalysisSnapshotOrchestrator analysisSnapshotOrchestrator;
        private final TransversalKpiHistoryService historyService;

        public TransversalKpiAnalyticsService(
                        AnalysisSnapshotOrchestrator analysisSnapshotOrchestrator,
                        TransversalKpiHistoryService historyService) {

                this.analysisSnapshotOrchestrator = analysisSnapshotOrchestrator;
                this.historyService = historyService;
        }

        /**
         * Recupera snapshots del panel de análisis para el periodo solicitado.
         */
        public List<AnalysisSnapshot> getAnalysisSnapshots(String period) {

                return analysisSnapshotOrchestrator.getAnalysisSnapshots(period);
        }

        /**
         * Devuelve el análisis operativo principal entre GLPI y una plataforma
         * técnica seleccionada.
         */
        public OperationalImpactAnalysisResponse getGlpiPlatformRelation(
                        String platform,
                        String period) {

                return analysisSnapshotOrchestrator.getGlpiPlatformRelation(platform, period);
        }

        public List<AnalyticsComparePoint> getTechnicalDegradationImpact(String period) {

                return analysisSnapshotOrchestrator.getTechnicalDegradationImpact(period);
        }

        public List<TechnicalTimelinePointDto> getPlatformEvolution(String period) {

                return analysisSnapshotOrchestrator.getPlatformEvolution(period);
        }

        /**
         * Guarda KPIs transversales en histórico.
         */
        public void saveCurrentSnapshot(LocalDateTime collectedAt) {

                historyService.saveCurrentSnapshot(collectedAt);
        }

        /**
         * Guarda el snapshot agregado que alimenta el panel de análisis.
         */
        public void saveAnalysisSnapshot(LocalDateTime collectedAt) {

                analysisSnapshotOrchestrator.saveAnalysisSnapshot(collectedAt);
        }
}
