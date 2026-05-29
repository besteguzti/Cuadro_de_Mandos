package com.tfg.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.summary.GlpiSummary;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;

@ExtendWith(MockitoExtension.class)
class GlpiServiceTest {

    @Mock
    private GlpiMetricsHistoryRepository glpiRepository;

    @Test
    void generatedGlpiSummaryKeepsOpenTicketsCoherentWithPlatformCategories() {
        GlpiService service =
                new GlpiService(glpiRepository, new KpiProperties());

        GlpiSummary summary =
                service.generateSimulatedSummary();

        assertThat(summary.getOpenTickets())
                .isEqualTo(summary.getArubaOpenTickets()
                        + summary.getCitrixOpenTickets()
                        + summary.getMicrosoft365OpenTickets());
    }

    @Test
    void summaryMapsPlatformOpenTicketsFromLatestHistory() {
        GlpiMetricsHistory history =
                new GlpiMetricsHistory();
        history.setCollectedAt(LocalDateTime.now());
        history.setOpenTickets(160);
        history.setArubaOpenTickets(35);
        history.setCitrixOpenTickets(100);
        history.setMicrosoft365OpenTickets(25);

        when(glpiRepository.findTopByOrderByCollectedAtDesc())
                .thenReturn(Optional.of(history));

        GlpiService service =
                new GlpiService(glpiRepository, new KpiProperties());

        GlpiSummary summary =
                service.getSummary();

        assertThat(summary.getOpenTickets()).isEqualTo(160);
        assertThat(summary.getArubaOpenTickets()).isEqualTo(35);
        assertThat(summary.getCitrixOpenTickets()).isEqualTo(100);
        assertThat(summary.getMicrosoft365OpenTickets()).isEqualTo(25);
    }
}
