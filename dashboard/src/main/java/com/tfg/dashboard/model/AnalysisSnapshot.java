package com.tfg.dashboard.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Snapshot persistido del panel de análisis exploratorio.
 *
 * Guarda valores normalizados de plataforma y KPIs transversales para que las
 * gráficas trabajen con histórico de base de datos. generatedScenario distingue datos demo de capturas reales.
 */
@Entity
@Table(name = "analysis_snapshots")
public class AnalysisSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private Integer arubaHealth;
    private Integer citrixHealth;
    private Integer microsoft365Health;
    private Integer glpiHealth;
    private Integer glpiOperationalPressure;
    private Integer technicalDegradation;
    private Integer userImpact;
    private Integer globalStatus;
    private Integer arubaWifiClients;
    private Integer citrixAverageLogonDurationSeconds;
    private Integer citrixActiveSessions;
    private Integer citrixFailedLogons;
    private Integer glpiOpenTickets;
    private Integer arubaOpenTickets;
    private Integer citrixOpenTickets;
    private Integer microsoft365OpenTickets;
    private Integer microsoft365NonCompliantDevices;
    private Integer affectedServicesPercent;
    private String arubaStatus;
    private String citrixStatus;
    private String microsoft365Status;
    private String glpiStatus;
    private Boolean generatedScenario;

    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Integer getArubaHealth() {
        return arubaHealth;
    }

    public Integer getCitrixHealth() {
        return citrixHealth;
    }

    public Integer getMicrosoft365Health() {
        return microsoft365Health;
    }

    public Integer getGlpiHealth() {
        return glpiHealth;
    }

    public Integer getGlpiOperationalPressure() {
        return glpiOperationalPressure;
    }

    public Integer getTechnicalDegradation() {
        return technicalDegradation;
    }

    public Integer getUserImpact() {
        return userImpact;
    }

    public Integer getGlobalStatus() {
        return globalStatus;
    }

    public Integer getArubaWifiClients() {
        return arubaWifiClients;
    }

    public Integer getCitrixAverageLogonDurationSeconds() {
        return citrixAverageLogonDurationSeconds;
    }

    public Integer getCitrixActiveSessions() {
        return citrixActiveSessions;
    }

    public Integer getCitrixFailedLogons() {
        return citrixFailedLogons;
    }

    public Integer getGlpiOpenTickets() {
        return glpiOpenTickets;
    }

    public Integer getArubaOpenTickets() {
        return arubaOpenTickets;
    }

    public Integer getCitrixOpenTickets() {
        return citrixOpenTickets;
    }

    public Integer getMicrosoft365OpenTickets() {
        return microsoft365OpenTickets;
    }

    public Integer getMicrosoft365NonCompliantDevices() {
        return microsoft365NonCompliantDevices;
    }

    public Integer getAffectedServicesPercent() {
        return affectedServicesPercent;
    }

    public String getArubaStatus() {
        return arubaStatus;
    }

    public String getCitrixStatus() {
        return citrixStatus;
    }

    public String getMicrosoft365Status() {
        return microsoft365Status;
    }

    public String getGlpiStatus() {
        return glpiStatus;
    }

    public Boolean isGeneratedScenario() {
        return generatedScenario;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setArubaHealth(Integer arubaHealth) {
        this.arubaHealth = arubaHealth;
    }

    public void setCitrixHealth(Integer citrixHealth) {
        this.citrixHealth = citrixHealth;
    }

    public void setMicrosoft365Health(Integer microsoft365Health) {
        this.microsoft365Health = microsoft365Health;
    }

    public void setGlpiHealth(Integer glpiHealth) {
        this.glpiHealth = glpiHealth;
    }

    public void setGlpiOperationalPressure(Integer glpiOperationalPressure) {
        this.glpiOperationalPressure = glpiOperationalPressure;
    }

    public void setTechnicalDegradation(Integer technicalDegradation) {
        this.technicalDegradation = technicalDegradation;
    }

    public void setUserImpact(Integer userImpact) {
        this.userImpact = userImpact;
    }

    public void setGlobalStatus(Integer globalStatus) {
        this.globalStatus = globalStatus;
    }

    public void setArubaWifiClients(Integer arubaWifiClients) {
        this.arubaWifiClients = arubaWifiClients;
    }

    public void setCitrixAverageLogonDurationSeconds(Integer citrixAverageLogonDurationSeconds) {
        this.citrixAverageLogonDurationSeconds = citrixAverageLogonDurationSeconds;
    }

    public void setCitrixActiveSessions(Integer citrixActiveSessions) {
        this.citrixActiveSessions = citrixActiveSessions;
    }

    public void setCitrixFailedLogons(Integer citrixFailedLogons) {
        this.citrixFailedLogons = citrixFailedLogons;
    }

    public void setGlpiOpenTickets(Integer glpiOpenTickets) {
        this.glpiOpenTickets = glpiOpenTickets;
    }

    public void setArubaOpenTickets(Integer arubaOpenTickets) {
        this.arubaOpenTickets = arubaOpenTickets;
    }

    public void setCitrixOpenTickets(Integer citrixOpenTickets) {
        this.citrixOpenTickets = citrixOpenTickets;
    }

    public void setMicrosoft365OpenTickets(Integer microsoft365OpenTickets) {
        this.microsoft365OpenTickets = microsoft365OpenTickets;
    }

    public void setMicrosoft365NonCompliantDevices(Integer microsoft365NonCompliantDevices) {
        this.microsoft365NonCompliantDevices = microsoft365NonCompliantDevices;
    }

    public void setAffectedServicesPercent(Integer affectedServicesPercent) {
        this.affectedServicesPercent = affectedServicesPercent;
    }

    public void setArubaStatus(String arubaStatus) {
        this.arubaStatus = arubaStatus;
    }

    public void setCitrixStatus(String citrixStatus) {
        this.citrixStatus = citrixStatus;
    }

    public void setMicrosoft365Status(String microsoft365Status) {
        this.microsoft365Status = microsoft365Status;
    }

    public void setGlpiStatus(String glpiStatus) {
        this.glpiStatus = glpiStatus;
    }

    public void setGeneratedScenario(Boolean generatedScenario) {
        this.generatedScenario = generatedScenario;
    }
}
