package com.tfg.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.SpecificKpiRelationDto;
import com.tfg.dashboard.model.AnalysisSnapshot;

class SpecificKpiRelationServiceTest {

    private SpecificKpiRelationService service;

    @BeforeEach
    void setUp() {
        KpiProperties kpiProperties =
                new KpiProperties();

        service =
                new SpecificKpiRelationService(
                        kpiProperties,
                        new KpiScoringService(kpiProperties));
    }

    @Test
    void specificRelationsUseCategorizedGlpiTickets() {
        List<SpecificKpiRelationDto> relations =
                service.buildRelations(snapshotsForDays(7));

        SpecificKpiRelationDto citrixRelation =
                relation(relations, "citrix_failed_logons_vs_citrix_open_tickets");
        SpecificKpiRelationDto microsoftRelation =
                relation(relations, "microsoft365_non_compliant_devices_vs_microsoft365_open_tickets");

        assertThat(citrixRelation.getYLabel())
                .isEqualTo("Tickets abiertos Citrix");
        assertThat(citrixRelation.getPoints().get(0).getY())
                .isEqualTo(50.0);
        assertThat(microsoftRelation.getYLabel())
                .isEqualTo("Tickets abiertos Microsoft 365");
        assertThat(microsoftRelation.getPoints().get(0).getY())
                .isEqualTo(20.0);
    }

    @Test
    void aggregatesSpecificRelationPointsByDayUsingFourSixHourBuckets() {
        SpecificKpiRelationDto relation =
                relation(
                        service.buildRelations(snapshotsForDays(7)),
                        "affected_services_vs_glpi_pressure");

        assertThat(relation.getPoints())
                .hasSize(7)
                .allSatisfy(point -> assertThat(point.getSamplesUsed()).isEqualTo(4));
        assertThat(relation.getXLabel()).isEqualTo("Servicios afectados");
        assertThat(relation.getXUnit()).isEqualTo("%");
        assertThat(relation.getYLabel()).isEqualTo("Presión operativa GLPI");
        assertThat(relation.getYUnit()).isEqualTo("%");
    }

    @Test
    void lowVariationDoesNotReturnHighRelationReading() {
        SpecificKpiRelationDto relation =
                relation(
                        service.buildRelations(flatSnapshots()),
                        "affected_services_vs_glpi_pressure");

        assertThat(relation.getReading())
                .contains("No hay variación suficiente");
        assertThat(relation.getReading())
                .doesNotContain("Relación alta");
    }

    private SpecificKpiRelationDto relation(
            List<SpecificKpiRelationDto> relations,
            String code) {

        return relations.stream()
                .filter(candidate -> code.equals(candidate.getCode()))
                .findFirst()
                .orElseThrow();
    }

    private List<AnalysisSnapshot> snapshotsForDays(int days) {
        List<AnalysisSnapshot> snapshots =
                new ArrayList<>();

        for (int day = 0; day < days; day++) {
            for (int bucket = 0; bucket < 4; bucket++) {
                AnalysisSnapshot snapshot =
                        baseSnapshot(day, bucket);

                snapshot.setCitrixFailedLogons(12 + day);
                snapshot.setCitrixOpenTickets(50 + day);
                snapshot.setMicrosoft365NonCompliantDevices(60 + day);
                snapshot.setMicrosoft365OpenTickets(20 + day);
                snapshot.setAffectedServicesPercent(25 + day * 10);
                snapshot.setGlpiOperationalPressure(30 + day * 8);
                snapshots.add(snapshot);
            }
        }

        return snapshots;
    }

    private List<AnalysisSnapshot> flatSnapshots() {
        List<AnalysisSnapshot> snapshots =
                new ArrayList<>();

        for (int day = 0; day < 4; day++) {
            AnalysisSnapshot snapshot =
                    baseSnapshot(day, 0);

            snapshot.setAffectedServicesPercent(50);
            snapshot.setGlpiOperationalPressure(53);
            snapshots.add(snapshot);
        }

        return snapshots;
    }

    private AnalysisSnapshot baseSnapshot(int day, int bucket) {
        AnalysisSnapshot snapshot =
                new AnalysisSnapshot();

        snapshot.setTimestamp(
                LocalDate.of(2026, 5, 1)
                        .plusDays(day)
                        .atStartOfDay()
                        .plusHours(bucket * 6L));
        snapshot.setArubaHealth(30 + day);
        snapshot.setCitrixAverageLogonDurationSeconds(20 + day);
        snapshot.setArubaWifiClients(200 - day);
        snapshot.setCitrixActiveSessions(400 - day);
        snapshot.setGeneratedScenario(false);

        return snapshot;
    }
}
