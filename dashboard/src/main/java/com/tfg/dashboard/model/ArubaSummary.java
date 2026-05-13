package com.tfg.dashboard.model;

public class ArubaSummary {

    private int wifiUsers;
    private int remoteUsers;
    private int apsDegraded;
    private int apsSaturated;
    private int vpnApsActive;
    private String networkStatus;
    private int downAps;
    private int networkTraffic;

     public ArubaSummary() {
    }
    public ArubaSummary(
        int wifiUsers,
        int remoteUsers,
        int apsDegraded,
        int apsSaturated,
        int vpnApsActive,
        int downAps,
        int networkTraffic,
        String networkStatus
    ){
        this.wifiUsers = wifiUsers;
        this.remoteUsers = remoteUsers;
        this.apsDegraded = apsDegraded;
        this.apsSaturated = apsSaturated;
        this.vpnApsActive = vpnApsActive;
        this.downAps = downAps;
        this.networkTraffic = networkTraffic;
        this.networkStatus = networkStatus;
    }

    public int getWifiUsers() {
    return wifiUsers;
}

public int getRemoteUsers() {
    return remoteUsers;
}

public int getApsDegraded() {
    return apsDegraded;
}

public int getApsSaturated() {
    return apsSaturated;
}

public int getVpnApsActive() {
    return vpnApsActive;
}

public int getDownAps() {
    return downAps;
}

public int getNetworkTraffic() {
    return networkTraffic;
}

public String getNetworkStatus() {
    return networkStatus;
}
}