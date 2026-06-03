package com.tfg.dashboard.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.dto.OperationalImpactAnalysisResponse;
import com.tfg.dashboard.model.AnalysisSnapshot;

/**
 * Construye la respuesta agregada del panel de análisis exploratorio.
 *
 * El frontend actual consume el endpoint /api/analysis/glpi-platform-relation
 * como respuesta compacta con tres bloques: relaciónes técnicas aparentes,
 * evolución temporal y relaciónes específicas entre indicadores.
 */
@Service
public class AnalysisPanelResponseService {

    private final AnalysisSnapshotQueryService queryService;
    private final GlpiPlatformRelationService glpiPlatformRelationService;
    private final TechnicalTimelineService technicalTimelineService;
    private final SpecificKpiRelationService specificKpiRelationService;

    public AnalysisPanelResponseService(
            AnalysisSnapshotQueryService queryService,
            GlpiPlatformRelationService glpiPlatformRelationService,
            TechnicalTimelineService technicalTimelineService,
            SpecificKpiRelationService specificKpiRelationService) {

        this.queryService = queryService;
        this.glpiPlatformRelationService = glpiPlatformRelationService;
        this.technicalTimelineService = technicalTimelineService;
        this.specificKpiRelationService = specificKpiRelationService;
    }

    public OperationalImpactAnalysisResponse buildOperationalImpactResponse(String period) {

        List<AnalysisSnapshot> snapshots = queryService.getAnalysisSnapshots(period);

        OperationalImpactAnalysisResponse response = new OperationalImpactAnalysisResponse();
        response.setTechnicalRelations(
                glpiPlatformRelationService.buildTechnicalRelations(snapshots));
        response.setTechnicalTimeline(
                technicalTimelineService.buildPlatformEvolution(snapshots));
        response.setSpecificKpiRelations(
                specificKpiRelationService.buildRelations(snapshots));

        return response;
    }
}
