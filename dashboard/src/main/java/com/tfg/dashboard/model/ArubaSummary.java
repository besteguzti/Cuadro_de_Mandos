package com.tfg.dashboard.model;

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
}