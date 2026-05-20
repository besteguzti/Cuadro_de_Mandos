package com.tfg.dashboard.model;

import java.time.LocalDateTime;
import java.util.List;

public class ArubaSummary {

    private int totalAps;

    private int upAps;

    private int downAps;

    private int totalSites;

    private int totalSwarms;

    private int firmwareOutdated;

    private int apsWithoutPublicIp;

    private String networkStatus;

    private int inactiveAps;

    private int totalSwitches;

    private int downSwitches;

    private int switchesFirmwareUpgradeRequired;

    private List<ArubaSwitchClientUsage> underusedSwitches;

    private int totalWifiClients;

    private int mutualiaApsClients;

    private int mutualiaWifiClients;

    private int mutualiaLangileakClients;

    private int mutualiaClients;

    private int mutualiaRedInternaClients;

    private int mutualiaRedExternaClients;

    private int mutualiaKorporatiboaClients;

    private int wifiPacsClients;

    private int mutVideoClients;

    // =========================
    // Frescura Aruba
    // =========================
    //
    // lastUpdated indica la fecha
    // y hora del último dato Aruba
    // usado para construir este
    // resumen.
    //
    // dataStatus indica si los
    // datos Aruba son recientes,
    // antiguos o inexistentes:
    // OK, STALE o NO_DATA.
    //

    private LocalDateTime lastUpdated;

    private String dataStatus;

    // =========================
    // GETTERS
    // =========================

    public int getTotalAps() {
        return totalAps;
    }

    public int getUpAps() {
        return upAps;
    }

    public int getDownAps() {
        return downAps;
    }

    public int getTotalSites() {
        return totalSites;
    }

    public int getTotalSwarms() {
        return totalSwarms;
    }

    public int getFirmwareOutdated() {
        return firmwareOutdated;
    }

    public int getApsWithoutPublicIp() {
        return apsWithoutPublicIp;
    }

    public String getNetworkStatus() {
        return networkStatus;
    }

    public int getInactiveAps() {
        return inactiveAps;
    }

    public int getTotalSwitches() {
        return totalSwitches;
    }

    public int getDownSwitches() {
        return downSwitches;
    }

    public int getSwitchesFirmwareUpgradeRequired() {
        return switchesFirmwareUpgradeRequired;
    }

    public List<ArubaSwitchClientUsage> getUnderusedSwitches() {
        return underusedSwitches;
    }

    public int getTotalWifiClients() {
        return totalWifiClients;
    }

    public int getMutualiaApsClients() {
        return mutualiaApsClients;
    }

    public int getMutualiaWifiClients() {
        return mutualiaWifiClients;
    }

    public int getMutualiaLangileakClients() {
        return mutualiaLangileakClients;
    }

    public int getMutualiaClients() {
        return mutualiaClients;
    }

    public int getMutualiaRedInternaClients() {
        return mutualiaRedInternaClients;
    }

    public int getMutualiaRedExternaClients() {
        return mutualiaRedExternaClients;
    }

    public int getMutualiaKorporatiboaClients() {
        return mutualiaKorporatiboaClients;
    }

    public int getWifiPacsClients() {
        return wifiPacsClients;
    }

    public int getMutVideoClients() {
        return mutVideoClients;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public String getDataStatus() {
        return dataStatus;
    }

    // =========================
    // SETTERS
    // =========================

    public void setTotalAps(int totalAps) {
        this.totalAps = totalAps;
    }

    public void setUpAps(int upAps) {
        this.upAps = upAps;
    }

    public void setDownAps(int downAps) {
        this.downAps = downAps;
    }

    public void setTotalSites(int totalSites) {
        this.totalSites = totalSites;
    }

    public void setTotalSwarms(int totalSwarms) {
        this.totalSwarms = totalSwarms;
    }

    public void setFirmwareOutdated(int firmwareOutdated) {
        this.firmwareOutdated = firmwareOutdated;
    }

    public void setApsWithoutPublicIp(int apsWithoutPublicIp) {
        this.apsWithoutPublicIp = apsWithoutPublicIp;
    }

    public void setNetworkStatus(String networkStatus) {
        this.networkStatus = networkStatus;
    }

    public void setInactiveAps(int inactiveAps) {
        this.inactiveAps = inactiveAps;
    }

    public void setTotalSwitches(int totalSwitches) {
        this.totalSwitches = totalSwitches;
    }

    public void setDownSwitches(int downSwitches) {
        this.downSwitches = downSwitches;
    }

    public void setSwitchesFirmwareUpgradeRequired(
            int switchesFirmwareUpgradeRequired
    ) {
        this.switchesFirmwareUpgradeRequired =
                switchesFirmwareUpgradeRequired;
    }

    public void setUnderusedSwitches(
            List<ArubaSwitchClientUsage> underusedSwitches
    ) {
        this.underusedSwitches = underusedSwitches;
    }

    public void setTotalWifiClients(int totalWifiClients) {
        this.totalWifiClients = totalWifiClients;
    }

    public void setMutualiaApsClients(int mutualiaApsClients) {
        this.mutualiaApsClients = mutualiaApsClients;
    }

    public void setMutualiaWifiClients(int mutualiaWifiClients) {
        this.mutualiaWifiClients = mutualiaWifiClients;
    }

    public void setMutualiaLangileakClients(int mutualiaLangileakClients) {
        this.mutualiaLangileakClients = mutualiaLangileakClients;
    }

    public void setMutualiaClients(int mutualiaClients) {
        this.mutualiaClients = mutualiaClients;
    }

    public void setMutualiaRedInternaClients(
            int mutualiaRedInternaClients
    ) {
        this.mutualiaRedInternaClients =
                mutualiaRedInternaClients;
    }

    public void setMutualiaRedExternaClients(
            int mutualiaRedExternaClients
    ) {
        this.mutualiaRedExternaClients =
                mutualiaRedExternaClients;
    }

    public void setMutualiaKorporatiboaClients(
            int mutualiaKorporatiboaClients
    ) {
        this.mutualiaKorporatiboaClients =
                mutualiaKorporatiboaClients;
    }

    public void setWifiPacsClients(int wifiPacsClients) {
        this.wifiPacsClients = wifiPacsClients;
    }

    public void setMutVideoClients(int mutVideoClients) {
        this.mutVideoClients = mutVideoClients;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setDataStatus(String dataStatus) {
        this.dataStatus = dataStatus;
    }
}
