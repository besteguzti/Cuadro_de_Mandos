package com.tfg.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import com.tfg.dashboard.dto.ApInfo;
import com.tfg.dashboard.model.AccessPoint;
import com.tfg.dashboard.model.ArubaSummary;
import com.tfg.dashboard.repository.AccessPointRepository;

@ExtendWith(MockitoExtension.class)
class ArubaServiceTest {

    @Mock
    private ArubaApiClient client;

    @Mock
    private AccessPointRepository accessPointRepository;

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

        when(accessPointRepository.findBySerial(any()))
                .thenReturn(Optional.empty());

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
        assertThat(summary.getNetworkStatus()).isEqualTo("YELLOW");
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

    private ApInfo ap(
            String name,
            String status,
            String serial,
            String site,
            String swarm,
            String publicIp
    ) {

        ApInfo ap =
                new ApInfo();

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
}
