package com.tfg.dashboard.client;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ArubaApiClient {

    // Generador de datos aleatorios para simular métricas Aruba
    private final Random random = new Random();

    // Usuarios WiFi conectados
    public int getWifiUsers() {
        return 200 + random.nextInt(100);
    }

    // Usuarios remotos/VPN
    public int getRemoteUsers() {
        return 50 + random.nextInt(50);
    }

    // APs degradados
    public int getApsDegraded() {
        return random.nextInt(10);
    }

    // APs saturados
    public int getApsSaturated() {
        return random.nextInt(8);
    }

    // APs VPN activos
    public int getVpnApsActive() {
        return 30 + random.nextInt(40);
    }

    // APs completamente caídos
    public int getDownAps() {
       return random.nextInt(5);
       //return 5;
    }

    // Tráfico total de red en Mbps
    public int getNetworkTraffic() {
        return 500 + random.nextInt(1000);
    }

    
}