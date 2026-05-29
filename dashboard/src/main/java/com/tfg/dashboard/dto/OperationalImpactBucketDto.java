package com.tfg.dashboard.dto;

public class OperationalImpactBucketDto {

    private String level;
    private int min;
    private int max;
    private int averageGlpiPressure;
    private String averageGlpiPressureStatus;
    private int snapshots;
    private int highGlpiPercentage;

    public String getLevel() {
        return level;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getAverageGlpiPressure() {
        return averageGlpiPressure;
    }

    public String getAverageGlpiPressureStatus() {
        return averageGlpiPressureStatus;
    }

    public int getSnapshots() {
        return snapshots;
    }

    public int getHighGlpiPercentage() {
        return highGlpiPercentage;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setAverageGlpiPressure(int averageGlpiPressure) {
        this.averageGlpiPressure = averageGlpiPressure;
    }

    public void setAverageGlpiPressureStatus(String averageGlpiPressureStatus) {
        this.averageGlpiPressureStatus = averageGlpiPressureStatus;
    }

    public void setSnapshots(int snapshots) {
        this.snapshots = snapshots;
    }

    public void setHighGlpiPercentage(int highGlpiPercentage) {
        this.highGlpiPercentage = highGlpiPercentage;
    }
}
