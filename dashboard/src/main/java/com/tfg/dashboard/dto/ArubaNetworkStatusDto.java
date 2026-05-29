package com.tfg.dashboard.dto;

import java.util.List;

public class ArubaNetworkStatusDto {

    private int percentage;
    private String color;
    private AccessPointStatusDto accessPointStatus;
    private SwitchStatusDto switchStatus;
    private List<String> reasons;
    private boolean affectedService;
    private boolean criticalCondition;
    private int technicalDegradationValue;
    private boolean transversalReady;

    public int getPercentage() {
        return percentage;
    }

    public String getColor() {
        return color;
    }

    public AccessPointStatusDto getAccessPointStatus() {
        return accessPointStatus;
    }

    public SwitchStatusDto getSwitchStatus() {
        return switchStatus;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public boolean isAffectedService() {
        return affectedService;
    }

    public boolean isCriticalCondition() {
        return criticalCondition;
    }

    public int getTechnicalDegradationValue() {
        return technicalDegradationValue;
    }

    public boolean isTransversalReady() {
        return transversalReady;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setAccessPointStatus(AccessPointStatusDto accessPointStatus) {
        this.accessPointStatus = accessPointStatus;
    }

    public void setSwitchStatus(SwitchStatusDto switchStatus) {
        this.switchStatus = switchStatus;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public void setAffectedService(boolean affectedService) {
        this.affectedService = affectedService;
    }

    public void setCriticalCondition(boolean criticalCondition) {
        this.criticalCondition = criticalCondition;
    }

    public void setTechnicalDegradationValue(int technicalDegradationValue) {
        this.technicalDegradationValue = technicalDegradationValue;
    }

    public void setTransversalReady(boolean transversalReady) {
        this.transversalReady = transversalReady;
    }
}
