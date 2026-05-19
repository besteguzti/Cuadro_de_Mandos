package com.tfg.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.dashboard.client.ArubaApiClient;
import com.tfg.dashboard.dto.ArubaApInfo;
import com.tfg.dashboard.dto.ArubaSwitchInfo;
import com.tfg.dashboard.dto.ArubaWifiClientInfo;
import com.tfg.dashboard.model.AccessPoint;
import com.tfg.dashboard.model.ArubaSummary;
import com.tfg.dashboard.model.ArubaSwitch;
import com.tfg.dashboard.model.ArubaSwitchClientUsage;
import com.tfg.dashboard.repository.AccessPointRepository;
import com.tfg.dashboard.repository.ArubaSwitchClientUsageRepository;
import com.tfg.dashboard.repository.ArubaSwitchRepository;

@ExtendWith(MockitoExtension.class)
class ArubaServiceTest {

    @Mock
    private ArubaApiClient client;

    @Mock
    private AccessPointRepository accessPointRepository;

    @Mock
    private ArubaSwitchRepository arubaSwitchRepository;

    @Mock
    private ArubaSwitchClientUsageRepository switchClientUsageRepository;

    @InjectMocks
    private ArubaService arubaService;

    @Test
    void getSummaryCalculatesArubaKpis() throws Exception {

        when(client.getApsList()).thenReturn(List.of(
                ap("AP-1", "Up", "SER-1", "Site A", "Swarm A", "1.1.1.1"),
                ap("AP-2", "Down", "SER-2", "Site A", "Swarm A", ""),
                ap("AP-3", "Up", "SER-3", "Site B", "Swarm B", null)
        ));

        when(client.getFirmwareSwarms()).thenReturn(
                firmwareSwarms()
        );

        when(client.getSwitchesList()).thenReturn(List.of(
                arubaSwitch("SW-1", "Up", false),
                arubaSwitch("SW-2", "Down", true)
        ));

        when(client.getWifiClientsList()).thenReturn(List.of(
                wifiClient("MUTUALIA-APs", "MUTUALIA_AP"),
                wifiClient("MUTUALIA-WIFI", "MUTUALIA"),
                wifiClient("MUTUALIA-WIFI", "MUTUALIA"),
                wifiClient("MUTUALIA-WIFI", "MUTUALIA_RED_INTERNA"),
                wifiClient("MUTUALIA-WIFI", "WIFI_PACs"),
                wifiClient("MUTUALIA-WIFI", "MUT_VIDEO")
        ));

        when(client.getWiredClientsList()).thenReturn(List.of(
                wiredClient("SW-LOW", "Switch bajo", "00:aa:bb:cc:dd:01"),
                wiredClient("SW-LOW", "Switch bajo", "00:aa:bb:cc:dd:01")
        ));

        when(accessPointRepository.findBySerial(any()))
                .thenReturn(Optional.empty());

        when(arubaSwitchRepository.findBySerial(any()))
                .thenReturn(Optional.empty());

        when(switchClientUsageRepository.findAll())
                .thenReturn(List.of());

        when(switchClientUsageRepository.findByAssociatedDevice(any()))
                .thenReturn(Optional.empty());

        when(switchClientUsageRepository
                .findByWiredClientsLessThanOrderByWiredClientsAscAssociatedDeviceAsc(10))
                .thenReturn(List.of(switchUsage("SW-LOW", "Switch bajo", 2)));

        when(accessPointRepository
                .countBySerialIsNotNullAndLastSeenAtBefore(any()))
                .thenReturn(4L);

        ArubaSummary summary =
                arubaService.getSummary();

        assertThat(summary.getTotalAps()).isEqualTo(3);
        assertThat(summary.getUpAps()).isEqualTo(2);
        assertThat(summary.getDownAps()).isEqualTo(1);
        assertThat(summary.getTotalSites()).isEqualTo(2);
        assertThat(summary.getTotalSwarms()).isEqualTo(2);
        assertThat(summary.getFirmwareOutdated()).isEqualTo(1);
        assertThat(summary.getApsWithoutPublicIp()).isEqualTo(2);
        assertThat(summary.getInactiveAps()).isEqualTo(4);
        assertThat(summary.getTotalSwitches()).isEqualTo(2);
        assertThat(summary.getDownSwitches()).isEqualTo(1);
        assertThat(summary.getSwitchesFirmwareUpgradeRequired()).isEqualTo(1);
        assertThat(summary.getUnderusedSwitches()).hasSize(1);
        assertThat(summary.getUnderusedSwitches().get(0).getWiredClients())
                .isEqualTo(2);
        assertThat(summary.getTotalWifiClients()).isEqualTo(6);
        assertThat(summary.getMutualiaApsClients()).isEqualTo(1);
        assertThat(summary.getMutualiaWifiClients()).isEqualTo(5);
        assertThat(summary.getMutualiaClients()).isEqualTo(2);
        assertThat(summary.getMutualiaRedInternaClients()).isEqualTo(1);
        assertThat(summary.getWifiPacsClients()).isEqualTo(1);
        assertThat(summary.getMutVideoClients()).isEqualTo(1);
        assertThat(summary.getNetworkStatus()).isEqualTo("RED");
    }

    @Test
    void syncAccessPointsCreatesNewApWithFirstSeenAt() {

        when(client.getApsList()).thenReturn(List.of(
                ap("AP-1", "Up", "SER-1", "Site A", "Swarm A", "1.1.1.1")
        ));

        when(accessPointRepository.findBySerial("SER-1"))
                .thenReturn(Optional.empty());

        arubaService.syncAccessPoints();

        ArgumentCaptor<AccessPoint> captor =
                ArgumentCaptor.forClass(AccessPoint.class);

        verify(accessPointRepository).save(captor.capture());

        AccessPoint saved =
                captor.getValue();

        assertThat(saved.getSerial()).isEqualTo("SER-1");
        assertThat(saved.getFirstSeenAt()).isNotNull();
        assertThat(saved.getLastSeenAt()).isNotNull();
    }

    @Test
    void syncAccessPointsKeepsExistingFirstSeenAt() {

        LocalDateTime originalFirstSeenAt =
                LocalDateTime.now().minusDays(10);

        AccessPoint existing =
                new AccessPoint();

        existing.setSerial("SER-1");
        existing.setFirstSeenAt(originalFirstSeenAt);

        when(client.getApsList()).thenReturn(List.of(
                ap("AP-1-renamed", "Up", "SER-1", "Site B", "Swarm B", "2.2.2.2")
        ));

        when(accessPointRepository.findBySerial("SER-1"))
                .thenReturn(Optional.of(existing));

        arubaService.syncAccessPoints();

        ArgumentCaptor<AccessPoint> captor =
                ArgumentCaptor.forClass(AccessPoint.class);

        verify(accessPointRepository).save(captor.capture());

        AccessPoint saved =
                captor.getValue();

        assertThat(saved.getFirstSeenAt()).isEqualTo(originalFirstSeenAt);
        assertThat(saved.getName()).isEqualTo("AP-1-renamed");
        assertThat(saved.getIpAddress()).isEqualTo("2.2.2.2");
        assertThat(saved.getLastSeenAt()).isAfter(originalFirstSeenAt);
    }

    @Test
    void syncSwitchesKeepsExistingFirstSeenAt() {

        LocalDateTime originalFirstSeenAt =
                LocalDateTime.now().minusDays(20);

        ArubaSwitch existing =
                new ArubaSwitch();

        existing.setSerial("SW-1");
        existing.setFirstSeenAt(originalFirstSeenAt);

        when(client.getSwitchesList()).thenReturn(List.of(
                arubaSwitch("SW-1", "Down", true)
        ));

        when(arubaSwitchRepository.findBySerial("SW-1"))
                .thenReturn(Optional.of(existing));

        arubaService.syncSwitches();

        ArgumentCaptor<ArubaSwitch> captor =
                ArgumentCaptor.forClass(ArubaSwitch.class);

        verify(arubaSwitchRepository).save(captor.capture());

        ArubaSwitch saved =
                captor.getValue();

        assertThat(saved.getFirstSeenAt()).isEqualTo(originalFirstSeenAt);
        assertThat(saved.getSerial()).isEqualTo("SW-1");
        assertThat(saved.getDeviceStatus()).isEqualTo("Down");
        assertThat(saved.isUpgradeRequired()).isTrue();
        assertThat(saved.getLastSeenAt()).isAfter(originalFirstSeenAt);
    }

    @Test
    void syncSwitchClientUsageCountsWiredClientsByAssociatedDevice() {

        when(client.getWiredClientsList()).thenReturn(List.of(
                wiredClient("SW-1", "Switch 1", "00:aa:bb:cc:dd:01"),
                wiredClient("SW-1", "Switch 1", "00:aa:bb:cc:dd:01"),
                wiredClient("SW-2", "Switch 2", "00:aa:bb:cc:dd:02")
        ));

        when(client.getSwitchesList()).thenReturn(List.of());

        when(switchClientUsageRepository.findAll())
                .thenReturn(List.of());

        when(switchClientUsageRepository.findByAssociatedDevice(any()))
                .thenReturn(Optional.empty());

        arubaService.syncSwitchClientUsage();

        ArgumentCaptor<ArubaSwitchClientUsage> captor =
                ArgumentCaptor.forClass(ArubaSwitchClientUsage.class);

        verify(switchClientUsageRepository, times(2))
                .save(captor.capture());

        List<ArubaSwitchClientUsage> saved =
                captor.getAllValues();

        assertThat(saved)
                .extracting(
                        ArubaSwitchClientUsage::getAssociatedDevice,
                        ArubaSwitchClientUsage::getWiredClients
                )
                .containsExactlyInAnyOrder(
                        org.assertj.core.api.Assertions.tuple("SW-1", 2),
                        org.assertj.core.api.Assertions.tuple("SW-2", 1)
                );
    }

    private ArubaApInfo ap(
            String name,
            String status,
            String serial,
            String site,
            String swarm,
            String publicIp
    ) {

        ArubaApInfo ap =
                new ArubaApInfo();

        ap.setName(name);
        ap.setStatus(status);
        ap.setSerial(serial);
        ap.setSite(site);
        ap.setSwarmName(swarm);
        ap.setIpAddress(publicIp);
        ap.setPublicIpAddress(publicIp);
        ap.setFirmwareVersion("8.13.0");
        ap.setMacaddr("00:11:22:33:44:55");

        return ap;
    }

    private JsonNode firmwareSwarms() throws Exception {

        return new ObjectMapper().readTree("""
                {
                  "swarms": [
                    { "swarm_name": "Swarm A", "status": { "state": "UPGRADE_REQUIRED" } },
                    { "swarm_name": "Swarm B", "status": { "state": "UP_TO_DATE" } }
                  ]
                }
                """);
    }

    private ArubaSwitchInfo arubaSwitch(
            String serial,
            String deviceStatus,
            boolean upgradeRequired
    ) {

        ArubaSwitchInfo switchInfo =
                new ArubaSwitchInfo();

        switchInfo.setSerial(serial);
        switchInfo.setMacAddress("00:aa:bb:cc:dd:ee");
        switchInfo.setHostname("switch-" + serial);
        switchInfo.setModel("Aruba 6300");
        switchInfo.setDeviceStatus(deviceStatus);
        switchInfo.setUpgradeRequired(upgradeRequired);
        switchInfo.setStatusState(upgradeRequired ? "UPGRADE_REQUIRED" : "UP_TO_DATE");

        return switchInfo;
    }

    private ArubaWifiClientInfo wifiClient(
            String groupName,
            String network
    ) {

        ArubaWifiClientInfo client =
                new ArubaWifiClientInfo();

        client.setAssociatedDevice("ap-serial");
        client.setAssociatedDeviceMac("00:11:22:33:44:55");
        client.setAssociatedDeviceName("AP-1");
        client.setGroupName(groupName);
        client.setHostname("client-host");
        client.setIpAddress("192.168.1.20");
        client.setLastConnectionTime(123456789L);
        client.setMacaddr("aa:bb:cc:dd:ee:ff");
        client.setNetwork(network);
        client.setOsType("Windows");

        return client;
    }

    private ArubaWifiClientInfo wiredClient(
            String associatedDevice,
            String associatedDeviceName,
            String associatedDeviceMac
    ) {

        ArubaWifiClientInfo client =
                wifiClient("", "");

        client.setAssociatedDevice(associatedDevice);
        client.setAssociatedDeviceName(associatedDeviceName);
        client.setAssociatedDeviceMac(associatedDeviceMac);

        return client;
    }

    private ArubaSwitchClientUsage switchUsage(
            String associatedDevice,
            String associatedDeviceName,
            int wiredClients
    ) {

        ArubaSwitchClientUsage usage =
                new ArubaSwitchClientUsage();

        usage.setAssociatedDevice(associatedDevice);
        usage.setAssociatedDeviceName(associatedDeviceName);
        usage.setAssociatedDeviceMac("00:aa:bb:cc:dd:01");
        usage.setWiredClients(wiredClients);

        return usage;
    }
}
