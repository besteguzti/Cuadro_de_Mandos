package com.tfg.dashboard.dto;

import java.util.List;

public class OperationalImpactAnalysisResponse {

    /**
     * DTO para la respuesta del análisis de impacto operativo.
     * Este DTO alimenta el panel exploratorio específico para comparar la presión
     * operativa de GLPI con la degradación de Aruba, Citrix y Microsoft 365.
     */
    private int glpiOperationalPressure;
    private String glpiOperationalPressureColor;
    private String averageGlpiIncreaseStatus;
    private String highHighCooccurrenceStatus;
    private String apparentOperationalRelationStatus;
    private String highestRelationStatus;
    private String estimatedOperationalImpactStatus;
    private String selectedPlatformAffectionStatus;
    private int arubaAffection;
    private int citrixAffection;
    private int microsoft365Affection;
    private int arubaGlpiRelation;
    private String arubaGlpiRelationStatus;
    private int citrixGlpiRelation;
    private String citrixGlpiRelationStatus;
    private int microsoft365GlpiRelation;
    private String microsoft365GlpiRelationStatus;
    private String highestRelatedPlatform;
    private int highestRelationValue;
    private int estimatedOperationalImpact;
    private int averageGlpiWhenPlatformNormal;
    private int averageGlpiWhenPlatformAffected;
    private int normalSnapshots;
    private int affectedSnapshots;
    private int averageGlpiIncreaseWhenAffected;
    private int highHighCooccurrencePercentage;
    private int apparentOperationalRelation;
    private String selectedPlatform;
    private int selectedPlatformAffection;
    private List<AnalyticsComparePoint> points;
    private List<OperationalImpactBucketDto> buckets;
    private List<TechnicalPlatformRelationDto> technicalRelations;
    private int technicalDegradation;
    private String technicalDegradationStatus;
    private int userImpact;
    private String userImpactStatus;
    private int technicalOperationalConversion;
    private String technicalOperationalConversionStatus;
    private List<AnalyticsComparePoint> technicalImpactPoints;
    private List<TechnicalTimelinePointDto> technicalTimeline;
    private List<SpecificKpiRelationDto> specificKpiRelations;
    private List<KpiResultDto> kpis;
    private boolean demoData;
    private String interpretation;
    private String technicalImpactInterpretation;

    public int getGlpiOperationalPressure() {
        return glpiOperationalPressure;
    }

    public String getGlpiOperationalPressureColor() {
        return glpiOperationalPressureColor;
    }

    public String getAverageGlpiIncreaseStatus() {
        return averageGlpiIncreaseStatus;
    }

    public String getHighHighCooccurrenceStatus() {
        return highHighCooccurrenceStatus;
    }

    public String getApparentOperationalRelationStatus() {
        return apparentOperationalRelationStatus;
    }

    public String getHighestRelationStatus() {
        return highestRelationStatus;
    }

    public String getEstimatedOperationalImpactStatus() {
        return estimatedOperationalImpactStatus;
    }

    public String getSelectedPlatformAffectionStatus() {
        return selectedPlatformAffectionStatus;
    }

    public int getArubaAffection() {
        return arubaAffection;
    }

    public int getCitrixAffection() {
        return citrixAffection;
    }

    public int getMicrosoft365Affection() {
        return microsoft365Affection;
    }

    public int getArubaGlpiRelation() {
        return arubaGlpiRelation;
    }

    public String getArubaGlpiRelationStatus() {
        return arubaGlpiRelationStatus;
    }

    public int getCitrixGlpiRelation() {
        return citrixGlpiRelation;
    }

    public String getCitrixGlpiRelationStatus() {
        return citrixGlpiRelationStatus;
    }

    public int getMicrosoft365GlpiRelation() {
        return microsoft365GlpiRelation;
    }

    public String getMicrosoft365GlpiRelationStatus() {
        return microsoft365GlpiRelationStatus;
    }

    public String getHighestRelatedPlatform() {
        return highestRelatedPlatform;
    }

    public int getHighestRelationValue() {
        return highestRelationValue;
    }

    public int getEstimatedOperationalImpact() {
        return estimatedOperationalImpact;
    }

    public int getAverageGlpiWhenPlatformNormal() {
        return averageGlpiWhenPlatformNormal;
    }

    public int getAverageGlpiWhenPlatformAffected() {
        return averageGlpiWhenPlatformAffected;
    }

    public int getNormalSnapshots() {
        return normalSnapshots;
    }

    public int getAffectedSnapshots() {
        return affectedSnapshots;
    }

    public int getAverageGlpiIncreaseWhenAffected() {
        return averageGlpiIncreaseWhenAffected;
    }

    public int getHighHighCooccurrencePercentage() {
        return highHighCooccurrencePercentage;
    }

    public int getApparentOperationalRelation() {
        return apparentOperationalRelation;
    }

    public String getSelectedPlatform() {
        return selectedPlatform;
    }

    public int getSelectedPlatformAffection() {
        return selectedPlatformAffection;
    }

    public List<AnalyticsComparePoint> getPoints() {
        return points;
    }

    public List<OperationalImpactBucketDto> getBuckets() {
        return buckets;
    }

    public List<TechnicalPlatformRelationDto> getTechnicalRelations() {
        return technicalRelations;
    }

    public int getTechnicalDegradation() {
        return technicalDegradation;
    }

    public String getTechnicalDegradationStatus() {
        return technicalDegradationStatus;
    }

    public int getUserImpact() {
        return userImpact;
    }

    public String getUserImpactStatus() {
        return userImpactStatus;
    }

    public int getTechnicalOperationalConversion() {
        return technicalOperationalConversion;
    }

    public String getTechnicalOperationalConversionStatus() {
        return technicalOperationalConversionStatus;
    }

    public List<AnalyticsComparePoint> getTechnicalImpactPoints() {
        return technicalImpactPoints;
    }

    public List<TechnicalTimelinePointDto> getTechnicalTimeline() {
        return technicalTimeline;
    }

    public List<SpecificKpiRelationDto> getSpecificKpiRelations() {
        return specificKpiRelations;
    }

    public List<KpiResultDto> getKpis() {
        return kpis;
    }

    public boolean isDemoData() {
        return demoData;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public String getTechnicalImpactInterpretation() {
        return technicalImpactInterpretation;
    }

    public void setGlpiOperationalPressure(int glpiOperationalPressure) {
        this.glpiOperationalPressure = glpiOperationalPressure;
    }

    public void setGlpiOperationalPressureColor(String glpiOperationalPressureColor) {
        this.glpiOperationalPressureColor = glpiOperationalPressureColor;
    }

    public void setAverageGlpiIncreaseStatus(String averageGlpiIncreaseStatus) {
        this.averageGlpiIncreaseStatus = averageGlpiIncreaseStatus;
    }

    public void setHighHighCooccurrenceStatus(String highHighCooccurrenceStatus) {
        this.highHighCooccurrenceStatus = highHighCooccurrenceStatus;
    }

    public void setApparentOperationalRelationStatus(String apparentOperationalRelationStatus) {
        this.apparentOperationalRelationStatus = apparentOperationalRelationStatus;
    }

    public void setHighestRelationStatus(String highestRelationStatus) {
        this.highestRelationStatus = highestRelationStatus;
    }

    public void setEstimatedOperationalImpactStatus(String estimatedOperationalImpactStatus) {
        this.estimatedOperationalImpactStatus = estimatedOperationalImpactStatus;
    }

    public void setSelectedPlatformAffectionStatus(String selectedPlatformAffectionStatus) {
        this.selectedPlatformAffectionStatus = selectedPlatformAffectionStatus;
    }

    public void setArubaAffection(int arubaAffection) {
        this.arubaAffection = arubaAffection;
    }

    public void setCitrixAffection(int citrixAffection) {
        this.citrixAffection = citrixAffection;
    }

    public void setMicrosoft365Affection(int microsoft365Affection) {
        this.microsoft365Affection = microsoft365Affection;
    }

    public void setArubaGlpiRelation(int arubaGlpiRelation) {
        this.arubaGlpiRelation = arubaGlpiRelation;
    }

    public void setArubaGlpiRelationStatus(String arubaGlpiRelationStatus) {
        this.arubaGlpiRelationStatus = arubaGlpiRelationStatus;
    }

    public void setCitrixGlpiRelation(int citrixGlpiRelation) {
        this.citrixGlpiRelation = citrixGlpiRelation;
    }

    public void setCitrixGlpiRelationStatus(String citrixGlpiRelationStatus) {
        this.citrixGlpiRelationStatus = citrixGlpiRelationStatus;
    }

    public void setMicrosoft365GlpiRelation(int microsoft365GlpiRelation) {
        this.microsoft365GlpiRelation = microsoft365GlpiRelation;
    }

    public void setMicrosoft365GlpiRelationStatus(String microsoft365GlpiRelationStatus) {
        this.microsoft365GlpiRelationStatus = microsoft365GlpiRelationStatus;
    }

    public void setHighestRelatedPlatform(String highestRelatedPlatform) {
        this.highestRelatedPlatform = highestRelatedPlatform;
    }

    public void setHighestRelationValue(int highestRelationValue) {
        this.highestRelationValue = highestRelationValue;
    }

    public void setEstimatedOperationalImpact(int estimatedOperationalImpact) {
        this.estimatedOperationalImpact = estimatedOperationalImpact;
    }

    public void setAverageGlpiWhenPlatformNormal(int averageGlpiWhenPlatformNormal) {
        this.averageGlpiWhenPlatformNormal = averageGlpiWhenPlatformNormal;
    }

    public void setAverageGlpiWhenPlatformAffected(int averageGlpiWhenPlatformAffected) {
        this.averageGlpiWhenPlatformAffected = averageGlpiWhenPlatformAffected;
    }

    public void setNormalSnapshots(int normalSnapshots) {
        this.normalSnapshots = normalSnapshots;
    }

    public void setAffectedSnapshots(int affectedSnapshots) {
        this.affectedSnapshots = affectedSnapshots;
    }

    public void setAverageGlpiIncreaseWhenAffected(int averageGlpiIncreaseWhenAffected) {
        this.averageGlpiIncreaseWhenAffected = averageGlpiIncreaseWhenAffected;
    }

    public void setHighHighCooccurrencePercentage(int highHighCooccurrencePercentage) {
        this.highHighCooccurrencePercentage = highHighCooccurrencePercentage;
    }

    public void setApparentOperationalRelation(int apparentOperationalRelation) {
        this.apparentOperationalRelation = apparentOperationalRelation;
    }

    public void setSelectedPlatform(String selectedPlatform) {
        this.selectedPlatform = selectedPlatform;
    }

    public void setSelectedPlatformAffection(int selectedPlatformAffection) {
        this.selectedPlatformAffection = selectedPlatformAffection;
    }

    public void setPoints(List<AnalyticsComparePoint> points) {
        this.points = points;
    }

    public void setBuckets(List<OperationalImpactBucketDto> buckets) {
        this.buckets = buckets;
    }

    public void setTechnicalRelations(List<TechnicalPlatformRelationDto> technicalRelations) {
        this.technicalRelations = technicalRelations;
    }

    public void setTechnicalDegradation(int technicalDegradation) {
        this.technicalDegradation = technicalDegradation;
    }

    public void setTechnicalDegradationStatus(String technicalDegradationStatus) {
        this.technicalDegradationStatus = technicalDegradationStatus;
    }

    public void setUserImpact(int userImpact) {
        this.userImpact = userImpact;
    }

    public void setUserImpactStatus(String userImpactStatus) {
        this.userImpactStatus = userImpactStatus;
    }

    public void setTechnicalOperationalConversion(int technicalOperationalConversion) {
        this.technicalOperationalConversion = technicalOperationalConversion;
    }

    public void setTechnicalOperationalConversionStatus(String technicalOperationalConversionStatus) {
        this.technicalOperationalConversionStatus = technicalOperationalConversionStatus;
    }

    public void setTechnicalImpactPoints(List<AnalyticsComparePoint> technicalImpactPoints) {
        this.technicalImpactPoints = technicalImpactPoints;
    }

    public void setTechnicalTimeline(List<TechnicalTimelinePointDto> technicalTimeline) {
        this.technicalTimeline = technicalTimeline;
    }

    public void setSpecificKpiRelations(List<SpecificKpiRelationDto> specificKpiRelations) {
        this.specificKpiRelations = specificKpiRelations;
    }

    public void setKpis(List<KpiResultDto> kpis) {
        this.kpis = kpis;
    }

    public void setDemoData(boolean demoData) {
        this.demoData = demoData;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    public void setTechnicalImpactInterpretation(String technicalImpactInterpretation) {
        this.technicalImpactInterpretation = technicalImpactInterpretation;
    }
}
