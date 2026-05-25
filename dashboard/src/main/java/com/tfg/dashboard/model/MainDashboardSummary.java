package com.tfg.dashboard.model;

import java.time.LocalDateTime;

public class MainDashboardSummary {

    // =========================
    // KPIs transversales
    // =========================
    //
    // Estos indicadores combinan
    // información de varias
    // plataformas para alimentar
    // la vista Principal.
    //

    private String globalHealth;

    private int globalHealthPercentage;

    private int globalCriticality;

    private int globalAvailability;

    private int userImpact;

    private int affectedServicesPercent;

    private int technicalDegradation;

    private int operationalPressure;

    private int operationalBacklog;

    private int slaRisk;

    private int globalOperationalRisk;

    private int servicesWithAlerts;

    private int totalActiveUsers;

    private int itemsRequiringAction;

    private int criticalOpenTickets;

    private int securityRiskItems;

    private int capacityPressure;

    // =========================
    // Frescura transversal
    // =========================
    //
    // dataStatus resume si los
    // snapshots usados por la vista
    // principal son recientes,
    // antiguos o inexistentes.
    //

    private LocalDateTime lastUpdated;

    private String dataStatus;

    private String arubaDataStatus;

    private String citrixDataStatus;

    private String microsoft365DataStatus;

    private String glpiDataStatus;

    public MainDashboardSummary() {
    }

    public String getGlobalHealth() {
        return globalHealth;
    }

    public int getGlobalHealthPercentage() {
        return globalHealthPercentage;
    }

    public int getGlobalCriticality() {
        return globalCriticality;
    }

    public int getGlobalAvailability() {
        return globalAvailability;
    }

    public int getUserImpact() {
        return userImpact;
    }

    public int getAffectedServicesPercent() {
        return affectedServicesPercent;
    }

    public int getTechnicalDegradation() {
        return technicalDegradation;
    }

    public int getOperationalPressure() {
        return operationalPressure;
    }

    public int getOperationalBacklog() {
        return operationalBacklog;
    }

    public int getSlaRisk() {
        return slaRisk;
    }

    public int getGlobalOperationalRisk() {
        return globalOperationalRisk;
    }

    public int getServicesWithAlerts() {
        return servicesWithAlerts;
    }

    public int getTotalActiveUsers() {
        return totalActiveUsers;
    }

    public int getItemsRequiringAction() {
        return itemsRequiringAction;
    }

    public int getCriticalOpenTickets() {
        return criticalOpenTickets;
    }

    public int getSecurityRiskItems() {
        return securityRiskItems;
    }

    public int getCapacityPressure() {
        return capacityPressure;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public String getDataStatus() {
        return dataStatus;
    }

    public String getCitrixDataStatus() {
        return citrixDataStatus;
    }

    public String getArubaDataStatus() {
        return arubaDataStatus;
    }

    public String getMicrosoft365DataStatus() {
        return microsoft365DataStatus;
    }

    public String getGlpiDataStatus() {
        return glpiDataStatus;
    }

    public void setGlobalHealth(String globalHealth) {
        this.globalHealth = globalHealth;
    }

    public void setGlobalHealthPercentage(int globalHealthPercentage) {
        this.globalHealthPercentage = globalHealthPercentage;
    }

    public void setGlobalCriticality(int globalCriticality) {
        this.globalCriticality = globalCriticality;
    }

    public void setGlobalAvailability(int globalAvailability) {
        this.globalAvailability = globalAvailability;
    }

    public void setUserImpact(int userImpact) {
        this.userImpact = userImpact;
    }

    public void setAffectedServicesPercent(int affectedServicesPercent) {
        this.affectedServicesPercent = affectedServicesPercent;
    }

    public void setTechnicalDegradation(int technicalDegradation) {
        this.technicalDegradation = technicalDegradation;
    }

    public void setOperationalPressure(int operationalPressure) {
        this.operationalPressure = operationalPressure;
    }

    public void setOperationalBacklog(int operationalBacklog) {
        this.operationalBacklog = operationalBacklog;
    }

    public void setSlaRisk(int slaRisk) {
        this.slaRisk = slaRisk;
    }

    public void setGlobalOperationalRisk(int globalOperationalRisk) {
        this.globalOperationalRisk = globalOperationalRisk;
    }

    public void setServicesWithAlerts(int servicesWithAlerts) {
        this.servicesWithAlerts = servicesWithAlerts;
    }

    public void setTotalActiveUsers(int totalActiveUsers) {
        this.totalActiveUsers = totalActiveUsers;
    }

    public void setItemsRequiringAction(int itemsRequiringAction) {
        this.itemsRequiringAction = itemsRequiringAction;
    }

    public void setCriticalOpenTickets(int criticalOpenTickets) {
        this.criticalOpenTickets = criticalOpenTickets;
    }

    public void setSecurityRiskItems(int securityRiskItems) {
        this.securityRiskItems = securityRiskItems;
    }

    public void setCapacityPressure(int capacityPressure) {
        this.capacityPressure = capacityPressure;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setDataStatus(String dataStatus) {
        this.dataStatus = dataStatus;
    }

    public void setArubaDataStatus(String arubaDataStatus) {
        this.arubaDataStatus = arubaDataStatus;
    }

    public void setCitrixDataStatus(String citrixDataStatus) {
        this.citrixDataStatus = citrixDataStatus;
    }

    public void setMicrosoft365DataStatus(String microsoft365DataStatus) {
        this.microsoft365DataStatus = microsoft365DataStatus;
    }

    public void setGlpiDataStatus(String glpiDataStatus) {
        this.glpiDataStatus = glpiDataStatus;
    }
}
