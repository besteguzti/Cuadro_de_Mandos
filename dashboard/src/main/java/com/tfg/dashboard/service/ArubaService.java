package com.tfg.dashboard.service;

import org.springframework.stereotype.Service;
import com.tfg.dashboard.client.ArubaApiClient;
import com.tfg.dashboard.model.ArubaSummary;

@Service
public class ArubaService {

    private final ArubaApiClient client;

    public ArubaService(ArubaApiClient client) {
        this.client = client;
    }

    public ArubaSummary getSummary() {

        int wifiUsers = client.getWifiUsers();
        int remoteUsers = client.getRemoteUsers();
        int apsDegraded = client.getApsDegraded();
        int apsSaturated = client.getApsSaturated();
        int vpnApsActive = client.getVpnApsActive();

        String status = calculateStatus(apsDegraded, apsSaturated);

        return new ArubaSummary(
                wifiUsers,
                remoteUsers,
                apsDegraded,
                apsSaturated,
                vpnApsActive,
                status
        );
    }

    private String calculateStatus(int degraded, int saturated) {

        if (degraded > 8 || saturated > 6) {
            return "RED";
        } else if (degraded > 3 || saturated > 3) {
            return "YELLOW";
        } else {
            return "GREEN";
        }
    }
}