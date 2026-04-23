package com.tfg.dashboard.model;

public class ArubaSummary {

    private int wifiUsers;
    private int remoteUsers;
    private int apsDegraded;
    private int apsSaturated;
    private int vpnApsActive;
    private String networkStatus;

    public ArubaSummary(int wifiUsers, int remoteUsers, int apsDegraded,
                        int apsSaturated, int vpnApsActive, String networkStatus) {
        this.wifiUsers = wifiUsers;
        this.remoteUsers = remoteUsers;
        this.apsDegraded = apsDegraded;
        this.apsSaturated = apsSaturated;
        this.vpnApsActive = vpnApsActive;
        this.networkStatus = networkStatus;
    }

    public int getWifiUsers() { return wifiUsers; }
    public int getRemoteUsers() { return remoteUsers; }
    public int getApsDegraded() { return apsDegraded; }
    public int getApsSaturated() { return apsSaturated; }
    public int getVpnApsActive() { return vpnApsActive; }
    public String getNetworkStatus() { return networkStatus; }
}