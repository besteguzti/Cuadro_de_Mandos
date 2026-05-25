package com.tfg.dashboard.model;

import java.time.LocalDateTime;

import com.tfg.dashboard.dto.CitrixHealthStatusDto;

public class CitrixSummary {

    private int activeSessions;

    private int activeLicenses;

    private int availableDeliveryControllers;

    private int totalDeliveryControllers;

    private int disconnectedSessions;

    private int averageLogonDurationSeconds;

    private int serverLoadPercent;

    private int failedLogons;

    private String citrixHealth;

    // Detalle normalizado del indice
    // Citrix. Mantiene citrixHealth
    // como semaforo simple para no
    // romper consumidores actuales.
    private CitrixHealthStatusDto citrixHealthDetails;

    // =========================
    // Frescura de datos
    // =========================
    //
    // dataStatus indica si el último
    // snapshot usado es reciente,
    // antiguo o inexistente.
    //

    private LocalDateTime lastUpdated;

    private String dataStatus;

    public CitrixSummary() {
    }

    public int getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(int activeSessions) {
        this.activeSessions = activeSessions;
    }

    public int getActiveLicenses() {
        return activeLicenses;
    }

    public void setActiveLicenses(int activeLicenses) {
        this.activeLicenses = activeLicenses;
    }

    public int getAvailableDeliveryControllers() {
        return availableDeliveryControllers;
    }

    public void setAvailableDeliveryControllers(int availableDeliveryControllers) {
        this.availableDeliveryControllers = availableDeliveryControllers;
    }

    public int getTotalDeliveryControllers() {
        return totalDeliveryControllers;
    }

    public void setTotalDeliveryControllers(int totalDeliveryControllers) {
        this.totalDeliveryControllers = totalDeliveryControllers;
    }

    public int getDisconnectedSessions() {
        return disconnectedSessions;
    }

    public void setDisconnectedSessions(int disconnectedSessions) {
        this.disconnectedSessions = disconnectedSessions;
    }

    public int getAverageLogonDurationSeconds() {
        return averageLogonDurationSeconds;
    }

    public void setAverageLogonDurationSeconds(int averageLogonDurationSeconds) {
        this.averageLogonDurationSeconds = averageLogonDurationSeconds;
    }

    public int getServerLoadPercent() {
        return serverLoadPercent;
    }

    public void setServerLoadPercent(int serverLoadPercent) {
        this.serverLoadPercent = serverLoadPercent;
    }

    public int getFailedLogons() {
        return failedLogons;
    }

    public void setFailedLogons(int failedLogons) {
        this.failedLogons = failedLogons;
    }

    public String getCitrixHealth() {
        return citrixHealth;
    }

    public void setCitrixHealth(String citrixHealth) {
        this.citrixHealth = citrixHealth;
    }

    public CitrixHealthStatusDto getCitrixHealthDetails() {
        return citrixHealthDetails;
    }

    public void setCitrixHealthDetails(
            CitrixHealthStatusDto citrixHealthDetails
    ) {
        this.citrixHealthDetails = citrixHealthDetails;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(String dataStatus) {
        this.dataStatus = dataStatus;
    }
}
