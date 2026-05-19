package com.tfg.dashboard.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
import com.tfg.dashboard.dto.ArubaApInfo;
import com.tfg.dashboard.dto.ArubaSwitchInfo;
import com.tfg.dashboard.dto.ArubaWifiClientInfo;
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

    public List<ArubaApInfo> getApsList() {

    List<ArubaApInfo> result =
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

                ArubaApInfo info =
                        new ArubaApInfo();

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

    // =========================
    // Obtener switches Aruba
    // =========================

    public List<ArubaSwitchInfo> getSwitchesList() {

        List<ArubaSwitchInfo> result =
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
                        + "/firmware/v1/devices"
                        + "?device_type=HP"
                        + "&offset="
                        + offset
                        + "&limit=100";

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

                JsonNode devices =
                        root.get("devices");

                if (devices == null
                        || !devices.isArray()
                        || devices.size() == 0) {

                    break;
                }

                for (JsonNode device : devices) {

                    ArubaSwitchInfo info =
                            new ArubaSwitchInfo();

                    info.setSerial(
                            device.path("serial").asText()
                    );

                    info.setMacAddress(
                            device.path("mac_address").asText()
                    );

                    info.setHostname(
                            device.path("hostname").asText()
                    );

                    info.setModel(
                            device.path("model").asText()
                    );

                    info.setDeviceStatus(
                            device.path("device_status").asText()
                    );

                    info.setUpgradeRequired(
                            device.path("upgrade_required").asBoolean(false)
                    );

                    info.setStatusState(
                            device.path("status")
                                    .path("state")
                                    .asText()
                    );

                    result.add(info);
                }

                offset += limit;
            }

        } catch (Exception e) {

            log.error(
                    "Error obteniendo switches desde Aruba",
                    e
            );
        }

        return result;
    }

    public List<ArubaSwitchInfo> getMonitoringSwitchesList() {

        List<ArubaSwitchInfo> result =
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
                        + "/monitoring/v1/switches"
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

                JsonNode switches =
                        findArray(
                                root,
                                "switches",
                                "devices",
                                "items",
                                "data"
                        );

                if (switches == null
                        || switches.size() == 0) {

                    if (offset == 0) {

                        log.warn(
                                "La respuesta de monitoring switches no contiene switches. Campos raiz: {}",
                                fieldNames(root)
                        );
                    }

                    break;
                }

                for (JsonNode switchNode : switches) {

                    ArubaSwitchInfo info =
                            new ArubaSwitchInfo();

                    info.setSerial(
                            text(
                                    switchNode,
                                    "serial",
                                    "serial_number"
                            )
                    );

                    info.setMacAddress(
                            text(
                                    switchNode,
                                    "macaddr",
                                    "mac_address",
                                    "mac"
                            )
                    );

                    info.setHostname(
                            text(
                                    switchNode,
                                    "name",
                                    "hostname",
                                    "device_name"
                            )
                    );

                    info.setModel(
                            text(switchNode, "model")
                    );

                    info.setDeviceStatus(
                            text(
                                    switchNode,
                                    "status",
                                    "device_status"
                            )
                    );

                    result.add(info);
                }

                offset += limit;
            }

        } catch (Exception e) {

            log.error(
                    "Error obteniendo switches desde Aruba monitoring",
                    e
            );
        }

        return result;
    }

    // =========================
    // Obtener clientes WiFi Aruba
    // =========================

    public List<ArubaWifiClientInfo> getWifiClientsList() {

        return getClientsList(
                "WIRELESS",
                "clientes WiFi"
        );
    }

    private List<ArubaWifiClientInfo> getClientsList(
            String clientType,
            String logLabel
    ) {

        List<ArubaWifiClientInfo> result =
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

            int limit = 1000;

            while (true) {

                String url =
                        baseUrl
                        + "/monitoring/v2/clients"
                        + "?client_type=" + clientType
                        + "&client_status=CONNECTED"
                        + "&calculate_total=true"
                        + "&timerange=3H"
                        + "&offset=" + offset
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

                JsonNode clients =
                        findArray(
                                root,
                                "clients",
                                "client",
                                "items",
                                "data"
                        );

                if (clients == null
                        || clients.size() == 0) {

                    if (offset == 0) {

                        log.warn(
                                "La respuesta de Aruba clients {} no contiene clientes. Campos raiz: {}, total={}, count={}, client_count={}",
                                clientType,
                                fieldNames(root),
                                root.path("total").asText(""),
                                root.path("count").asText(""),
                                root.path("client_count").asText("")
                        );
                    }

                    break;
                }

                for (JsonNode client : clients) {

                    ArubaWifiClientInfo info =
                            new ArubaWifiClientInfo();

                    info.setAssociatedDevice(
                            text(client, "associated_device")
                    );

                    info.setAssociatedDeviceMac(
                            text(client, "associated_device_mac")
                    );

                    info.setAssociatedDeviceName(
                            text(client, "associated_device_name")
                    );

                    info.setGroupName(
                            text(
                                    client,
                                    "group_name",
                                    "groupName",
                                    "group"
                            )
                    );

                    info.setHostname(
                            text(client, "hostname")
                    );

                    info.setIpAddress(
                            text(client, "ip_address")
                    );

                    info.setLastConnectionTime(
                            client.path("last_connection_time").asLong(0)
                    );

                    info.setMacaddr(
                            text(client, "macaddr", "mac_address")
                    );

                    info.setNetwork(
                            text(
                                    client,
                                    "network",
                                    "network_name",
                                    "networkName",
                                    "ssid",
                                    "ssid_name"
                            )
                    );

                    info.setOsType(
                            text(client, "os_type")
                    );

                    result.add(info);
                }

                offset += limit;
            }

        } catch (Exception e) {

            log.error(
                    "Error obteniendo " + logLabel + " desde Aruba",
                    e
            );
        }

        log.info(
                "{} obtenidos desde Aruba: {}",
                logLabel,
                result.size()
        );

        return result;
    }

    public int countSwitchPortsDown(String serial) {

        if (serial == null
                || serial.isBlank()) {

            return 0;
        }

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

            String encodedSerial =
                    URLEncoder.encode(
                            serial,
                            StandardCharsets.UTF_8
                    );

            String url =
                    baseUrl
                    + "/monitoring/v1/switches/"
                    + encodedSerial
                    + "/ports";

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

            return countPortsByStatus(
                    root,
                    "down"
            );

        } catch (Exception e) {

            log.error(
                    "Error obteniendo puertos del switch {} desde Aruba",
                    serial,
                    e
            );

            return 0;
        }
    }

    private int countPortsByStatus(
            JsonNode root,
            String expectedStatus
    ) {

        JsonNode ports =
                findArray(
                        root,
                        "ports",
                        "interfaces",
                        "items",
                        "data"
                );

        if (ports == null
                || !ports.isArray()) {

            return 0;
        }

        int count = 0;

        for (JsonNode port : ports) {

            String status =
                    text(port, "status");

            if (expectedStatus.equalsIgnoreCase(status)) {

                count++;
            }
        }

        return count;
    }

    private JsonNode findArray(
            JsonNode root,
            String... names
    ) {

        for (String name : names) {

            JsonNode node =
                    root.get(name);

            if (node != null
                    && node.isArray()) {

                return node;
            }
        }

        return null;
    }

    private String text(
            JsonNode node,
            String... names
    ) {

        for (String name : names) {

            JsonNode value =
                    node.get(name);

            if (value != null
                    && !value.isNull()) {

                return value.asText().trim();
            }
        }

        return "";
    }

    private List<String> fieldNames(JsonNode node) {

        List<String> names =
                new ArrayList<>();

        Iterator<String> iterator =
                node.fieldNames();

        while (iterator.hasNext()) {

            names.add(iterator.next());
        }

        return names;
    }
}
