package com.tfg.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfg.dashboard.model.CitrixMetricsHistory;
import com.tfg.dashboard.model.CitrixSummary;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.model.GlpiSummary;
import com.tfg.dashboard.model.Microsoft365MetricsHistory;
import com.tfg.dashboard.model.Microsoft365Summary;
import com.tfg.dashboard.repository.CitrixMetricsHistoryRepository;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;
import com.tfg.dashboard.repository.Microsoft365MetricsHistoryRepository;

@ExtendWith(MockitoExtension.class)
class MetricsSyncServiceTest {

    @Mock
    private CitrixService citrixService;

    @Mock
    private Microsoft365Service microsoft365Service;

    @Mock
    private GlpiService glpiService;

    @Mock
    private CitrixMetricsHistoryRepository citrixRepository;

    @Mock
    private Microsoft365MetricsHistoryRepository microsoft365Repository;

    @Mock
    private GlpiMetricsHistoryRepository glpiRepository;

    private MetricsSyncService service;

    @BeforeEach
    void setUp() {

        service = new MetricsSyncService(
                citrixService,
                microsoft365Service,
                glpiService,
                citrixRepository,
                microsoft365Repository,
                glpiRepository
        );
    }

    @Test
    void savesSnapshotsForAllPlatforms() {

        mockSuccessfulSummaries();

        service.syncExternalPlatformMetrics();

        ArgumentCaptor<CitrixMetricsHistory> citrixCaptor =
                ArgumentCaptor.forClass(CitrixMetricsHistory.class);
        ArgumentCaptor<Microsoft365MetricsHistory> microsoftCaptor =
                ArgumentCaptor.forClass(Microsoft365MetricsHistory.class);
        ArgumentCaptor<GlpiMetricsHistory> glpiCaptor =
                ArgumentCaptor.forClass(GlpiMetricsHistory.class);

        verify(citrixRepository).save(citrixCaptor.capture());
        verify(microsoft365Repository).save(microsoftCaptor.capture());
        verify(glpiRepository).save(glpiCaptor.capture());

        assertThat(citrixCaptor.getValue().getActiveSessions())
                .isEqualTo(300);
        assertThat(microsoftCaptor.getValue().getActiveUsers())
                .isEqualTo(1200);
        assertThat(glpiCaptor.getValue().getOpenTickets())
                .isEqualTo(80);
        assertThat(citrixCaptor.getValue().getCollectedAt()).isNotNull();
        assertThat(microsoftCaptor.getValue().getCollectedAt()).isNotNull();
        assertThat(glpiCaptor.getValue().getCollectedAt()).isNotNull();
    }

    @Test
    void continuesWhenCitrixFails() {

        when(citrixService.generateSimulatedSummary())
                .thenThrow(new RuntimeException("Citrix falla"));
        when(microsoft365Service.generateSimulatedSummary())
                .thenReturn(microsoft365Summary());
        when(glpiService.generateSimulatedSummary())
                .thenReturn(glpiSummary());

        service.syncExternalPlatformMetrics();

        verify(citrixRepository, never()).save(any());
        verify(microsoft365Repository).save(any());
        verify(glpiRepository).save(any());
    }

    @Test
    void continuesWhenMicrosoft365Fails() {

        when(citrixService.generateSimulatedSummary())
                .thenReturn(citrixSummary());
        when(microsoft365Service.generateSimulatedSummary())
                .thenThrow(new RuntimeException("M365 falla"));
        when(glpiService.generateSimulatedSummary())
                .thenReturn(glpiSummary());

        service.syncExternalPlatformMetrics();

        verify(citrixRepository).save(any());
        verify(microsoft365Repository, never()).save(any());
        verify(glpiRepository).save(any());
    }

    @Test
    void continuesWhenGlpiFails() {

        when(citrixService.generateSimulatedSummary())
                .thenReturn(citrixSummary());
        when(microsoft365Service.generateSimulatedSummary())
                .thenReturn(microsoft365Summary());
        when(glpiService.generateSimulatedSummary())
                .thenThrow(new RuntimeException("GLPI falla"));

        service.syncExternalPlatformMetrics();

        verify(citrixRepository).save(any());
        verify(microsoft365Repository).save(any());
        verify(glpiRepository, never()).save(any());
    }

    @Test
    void appliesNinetyDayRetentionPolicy() {

        mockSuccessfulSummaries();

        service.syncExternalPlatformMetrics();

        verify(citrixRepository).deleteByCollectedAtBefore(any());
        verify(microsoft365Repository).deleteByCollectedAtBefore(any());
        verify(glpiRepository).deleteByCollectedAtBefore(any());
    }

    private void mockSuccessfulSummaries() {

        when(citrixService.generateSimulatedSummary())
                .thenReturn(citrixSummary());
        when(microsoft365Service.generateSimulatedSummary())
                .thenReturn(microsoft365Summary());
        when(glpiService.generateSimulatedSummary())
                .thenReturn(glpiSummary());
    }

    private CitrixSummary citrixSummary() {

        CitrixSummary summary =
                new CitrixSummary();

        summary.setActiveSessions(300);
        summary.setCitrixHealth("GREEN");

        return summary;
    }

    private Microsoft365Summary microsoft365Summary() {

        Microsoft365Summary summary =
                new Microsoft365Summary();

        summary.setActiveUsers(1200);
        summary.setMicrosoft365Health("GREEN");

        return summary;
    }

    private GlpiSummary glpiSummary() {

        GlpiSummary summary =
                new GlpiSummary();

        summary.setOpenTickets(80);

        return summary;
    }
}
