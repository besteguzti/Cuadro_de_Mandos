package com.tfg.dashboard.dto;

import java.util.List;

public class AnalyticsCompareResponse {

    private String kpiX;

    private String kpiY;

    private String kpiXName;

    private String kpiYName;

    private List<AnalyticsComparePoint> points;

    private Double correlation;

    private String correlationLabel;

    private String interpretation;

    private boolean demoData;

    public AnalyticsCompareResponse() {
    }

    public String getKpiX() {
        return kpiX;
    }

    public String getKpiY() {
        return kpiY;
    }

    public String getKpiXName() {
        return kpiXName;
    }

    public String getKpiYName() {
        return kpiYName;
    }

    public List<AnalyticsComparePoint> getPoints() {
        return points;
    }

    public Double getCorrelation() {
        return correlation;
    }

    public String getCorrelationLabel() {
        return correlationLabel;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public boolean isDemoData() {
        return demoData;
    }

    public void setKpiX(String kpiX) {
        this.kpiX = kpiX;
    }

    public void setKpiY(String kpiY) {
        this.kpiY = kpiY;
    }

    public void setKpiXName(String kpiXName) {
        this.kpiXName = kpiXName;
    }

    public void setKpiYName(String kpiYName) {
        this.kpiYName = kpiYName;
    }

    public void setPoints(List<AnalyticsComparePoint> points) {
        this.points = points;
    }

    public void setCorrelation(Double correlation) {
        this.correlation = correlation;
    }

    public void setCorrelationLabel(String correlationLabel) {
        this.correlationLabel = correlationLabel;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    public void setDemoData(boolean demoData) {
        this.demoData = demoData;
    }
}
