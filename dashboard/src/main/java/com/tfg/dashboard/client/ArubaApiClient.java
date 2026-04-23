package com.tfg.dashboard.client;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ArubaApiClient {

    private final Random random = new Random();

    public int getWifiUsers() {
        return 200 + random.nextInt(100);
    }

    public int getRemoteUsers() {
        return 50 + random.nextInt(50);
    }

    public int getApsDegraded() {
        return random.nextInt(10);
    }

    public int getApsSaturated() {
        return random.nextInt(8);
    }

    public int getVpnApsActive() {
        return 30 + random.nextInt(40);
    }
}