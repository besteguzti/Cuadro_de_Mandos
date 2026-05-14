package com.tfg.dashboard.client;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tfg.dashboard.dto.ApInfo;
import com.tfg.dashboard.service.ArubaAuthService;

@Component
public class ArubaApiClient {

    private static final Logger log =
            LoggerFactory.getLogger(ArubaApiClient.class);

    // =========================
    // Auth Aruba
    // =========================

    private final ArubaAuthService authService;

    // =========================
    // Base URL Aruba
    // =========================

    @Value("${aruba.base.url}")
    private String baseUrl;

    // =========================
    // Constructor
    // =========================

    public ArubaApiClient(
            ArubaAuthService authService
    ) {
        this.authService = authService;
    }

    // =========================
    // Obtener APs RAW
    // =========================

    public String getAccessPoints() {

        String token =
                authService.getAccessToken();

        RestTemplate restTemplate =
                new RestTemplate();

        String url =
                baseUrl
                + "/monitoring/v2/aps";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setBearerAuth(token);

        HttpEntity<String> entity =
                new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

        return response.getBody();
    }

    // =========================
    // Obtener lista APs parseada
    // =========================

    public List<ApInfo> getApsList() {

    List<ApInfo> result =
            new ArrayList<>();

    try {

        String token =
                authService.getAccessToken();

        RestTemplate restTemplate =
                new RestTemplate();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setBearerAuth(token);

        HttpEntity<String> entity =
                new HttpEntity<>(headers);

        ObjectMapper mapper =
                new ObjectMapper();

        int offset = 0;

        int limit = 100;

        while (true) {

            String url =
                    baseUrl
                    + "/monitoring/v2/aps"
                    + "?offset=" + offset
                    + "&limit=" + limit;

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

            JsonNode root =
                    mapper.readTree(
                            response.getBody()
                    );

            JsonNode aps =
                    root.get("aps");

            if (aps == null
                    || !aps.isArray()
                    || aps.size() == 0) {

                break;
            }

            for (JsonNode ap : aps) {

                ApInfo info =
                        new ApInfo();

                info.setName(
                        ap.path("name").asText()
                );

                info.setStatus(
                        ap.path("status").asText()
                );

                info.setIpAddress(
                        ap.path("ip_address").asText()
                );

                info.setPublicIpAddress(
                        ap.path("public_ip_address").asText()
                );

                info.setSerial(
                        ap.path("serial").asText()
                );

                info.setSite(
                        ap.path("site").asText()
                );

                info.setFirmwareVersion(
                        ap.path("firmware_version")
                                .asText()
                );

                info.setMacaddr(
                        ap.path("macaddr").asText()
                );

                info.setSwarmName(
                        ap.path("swarm_name").asText()
                );

                result.add(info);
            }

            offset += limit;
        }



    } catch (Exception e) {

        log.error(
                "Error obteniendo listado de APs desde Aruba",
                e
        );
    }

    return result;
}

    // =========================
    // Firmware devices Aruba
    // =========================

    public JsonNode getFirmwareSwarms() {

    try {

        String token =
                authService.getAccessToken();

        RestTemplate restTemplate =
                new RestTemplate();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setBearerAuth(token);

        HttpEntity<String> entity =
                new HttpEntity<>(headers);

        ObjectMapper mapper =
                new ObjectMapper();

        ArrayNode allSwarms =
                mapper.createArrayNode();

        int offset = 0;

        int limit = 20;

        while (true) {

            String url =
                    baseUrl
                    + "/firmware/v1/swarms"
                    + "?offset=" + offset
                    + "&limit=" + limit;

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

            JsonNode root =
                    mapper.readTree(
                            response.getBody()
                    );

            JsonNode swarms =
                    root.get("swarms");

            if (swarms == null
                    || !swarms.isArray()
                    || swarms.size() == 0) {

                break;
            }

            for (JsonNode swarm : swarms) {

                allSwarms.add(swarm);
            }

            offset += limit;
        }

        ObjectNode result =
                mapper.createObjectNode();

        result.set("swarms", allSwarms);

        

        return result;

    } catch (Exception e) {

        log.error(
                "Error obteniendo firmware de swarms desde Aruba",
                e
        );

        return null;
    }
}
}
