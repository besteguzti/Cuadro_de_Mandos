package com.tfg.dashboard.model;

import java.time.LocalDateTime;

public class Microsoft365Summary {

    // =========================
    // Uso y licenciamiento
    // =========================

    private int activeUsers;

    private int unassignedLicenses;

    // =========================
    // Estado servicios Microsoft
    // =========================

    private String outlookStatus;

    private String teamsStatus;

    private String sharePointStatus;

    // =========================
    // Exchange / SharePoint
    // =========================

    private int nearlyFullMailboxes;

    private int emailsQuarantined;

    private int sharePointStoragePercent;

    // =========================
    // Seguridad e identidad
    // =========================

    private int riskyUsers;

    private int failedSignIns;

    private int usersWithoutMfa;

    // =========================
    // Aplicaciones empresariales
    // =========================

    private int appsSecretsExpiringSoon;

    private int unusedApplications;

    private int highPrivilegeApplications;

    // =========================
    // Intune / Endpoint Manager
    // =========================

    private int nonCompliantDevices;

    private int outdatedWindowsDevices;

    private int devicesWithoutEncryption;

    private int staleDevices;

    // =========================
    // KPIs compuestos
    // =========================

    private String microsoft365Health;

    private int microsoft365OperationalRisk;

    // =========================
    // Frescura de datos
    // =========================
    //
    // dataStatus permite saber si
    // el snapshot de MySQL es OK,
    // STALE o NO_DATA.
    //

    private LocalDateTime lastUpdated;

    private String dataStatus;

    public Microsoft365Summary() {
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public int getUnassignedLicenses() {
        return unassignedLicenses;
    }

    public void setUnassignedLicenses(int unassignedLicenses) {
        this.unassignedLicenses = unassignedLicenses;
    }

    public String getOutlookStatus() {
        return outlookStatus;
    }

    public void setOutlookStatus(String outlookStatus) {
        this.outlookStatus = outlookStatus;
    }

    public String getTeamsStatus() {
        return teamsStatus;
    }

    public void setTeamsStatus(String teamsStatus) {
        this.teamsStatus = teamsStatus;
    }

    public String getSharePointStatus() {
        return sharePointStatus;
    }

    public void setSharePointStatus(String sharePointStatus) {
        this.sharePointStatus = sharePointStatus;
    }

    public int getNearlyFullMailboxes() {
        return nearlyFullMailboxes;
    }

    public void setNearlyFullMailboxes(int nearlyFullMailboxes) {
        this.nearlyFullMailboxes = nearlyFullMailboxes;
    }

    public int getEmailsQuarantined() {
        return emailsQuarantined;
    }

    public void setEmailsQuarantined(int emailsQuarantined) {
        this.emailsQuarantined = emailsQuarantined;
    }

    public int getSharePointStoragePercent() {
        return sharePointStoragePercent;
    }

    public void setSharePointStoragePercent(int sharePointStoragePercent) {
        this.sharePointStoragePercent = sharePointStoragePercent;
    }

    public int getRiskyUsers() {
        return riskyUsers;
    }

    public void setRiskyUsers(int riskyUsers) {
        this.riskyUsers = riskyUsers;
    }

    public int getFailedSignIns() {
        return failedSignIns;
    }

    public void setFailedSignIns(int failedSignIns) {
        this.failedSignIns = failedSignIns;
    }

    public int getUsersWithoutMfa() {
        return usersWithoutMfa;
    }

    public void setUsersWithoutMfa(int usersWithoutMfa) {
        this.usersWithoutMfa = usersWithoutMfa;
    }

    public int getAppsSecretsExpiringSoon() {
        return appsSecretsExpiringSoon;
    }

    public void setAppsSecretsExpiringSoon(int appsSecretsExpiringSoon) {
        this.appsSecretsExpiringSoon = appsSecretsExpiringSoon;
    }

    public int getUnusedApplications() {
        return unusedApplications;
    }

    public void setUnusedApplications(int unusedApplications) {
        this.unusedApplications = unusedApplications;
    }

    public int getHighPrivilegeApplications() {
        return highPrivilegeApplications;
    }

    public void setHighPrivilegeApplications(int highPrivilegeApplications) {
        this.highPrivilegeApplications = highPrivilegeApplications;
    }

    public int getNonCompliantDevices() {
        return nonCompliantDevices;
    }

    public void setNonCompliantDevices(int nonCompliantDevices) {
        this.nonCompliantDevices = nonCompliantDevices;
    }

    public int getOutdatedWindowsDevices() {
        return outdatedWindowsDevices;
    }

    public void setOutdatedWindowsDevices(int outdatedWindowsDevices) {
        this.outdatedWindowsDevices = outdatedWindowsDevices;
    }

    public int getDevicesWithoutEncryption() {
        return devicesWithoutEncryption;
    }

    public void setDevicesWithoutEncryption(int devicesWithoutEncryption) {
        this.devicesWithoutEncryption = devicesWithoutEncryption;
    }

    public int getStaleDevices() {
        return staleDevices;
    }

    public void setStaleDevices(int staleDevices) {
        this.staleDevices = staleDevices;
    }

    public String getMicrosoft365Health() {
        return microsoft365Health;
    }

    public void setMicrosoft365Health(String microsoft365Health) {
        this.microsoft365Health = microsoft365Health;
    }

    public int getMicrosoft365OperationalRisk() {
        return microsoft365OperationalRisk;
    }

    public void setMicrosoft365OperationalRisk(int microsoft365OperationalRisk) {
        this.microsoft365OperationalRisk = microsoft365OperationalRisk;
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
