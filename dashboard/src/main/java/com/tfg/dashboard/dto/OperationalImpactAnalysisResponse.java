package com.tfg.dashboard.dto;

import java.util.List;

public class OperationalImpactAnalysisResponse {

    private List<TechnicalPlatformRelationDto> technicalRelations;
    private List<TechnicalTimelinePointDto> technicalTimeline;
    private List<SpecificKpiRelationDto> specificKpiRelations;

    public List<TechnicalPlatformRelationDto> getTechnicalRelations() {
        return technicalRelations;
    }

    public List<TechnicalTimelinePointDto> getTechnicalTimeline() {
        return technicalTimeline;
    }

    public List<SpecificKpiRelationDto> getSpecificKpiRelations() {
        return specificKpiRelations;
    }

    public void setTechnicalRelations(List<TechnicalPlatformRelationDto> technicalRelations) {
        this.technicalRelations = technicalRelations;
    }

    public void setTechnicalTimeline(List<TechnicalTimelinePointDto> technicalTimeline) {
        this.technicalTimeline = technicalTimeline;
    }

    public void setSpecificKpiRelations(List<SpecificKpiRelationDto> specificKpiRelations) {
        this.specificKpiRelations = specificKpiRelations;
    }
}
