package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.GlpiHealthStatusDto;
import com.tfg.dashboard.dto.GlpiIndicatorStatusDto;
import com.tfg.dashboard.dto.KpiResultDto;
import com.tfg.dashboard.dto.KpiStatus;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.dto.summary.GlpiSummary;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;

/**
 * Servicio GLPI simulado.
 *
 * GLPI se usa como señal de consecuencia operativa: genera y lee snapshots de
 * tickets, SLA y capacidad de cierre para alimentar tanto su página como el
 * análisis transversal.
 */
@Service
public class GlpiService {

        private final Random random = new Random();

        private static final String GREEN = "GREEN";

        private static final String YELLOW = "YELLOW";

        private static final String RED = "RED";

        private final GlpiMetricsHistoryRepository metricsHistoryRepository;
        private final KpiProperties kpiProperties;

        public GlpiService(
                        GlpiMetricsHistoryRepository metricsHistoryRepository,
                        KpiProperties kpiProperties) {

                this.metricsHistoryRepository = metricsHistoryRepository;
                this.kpiProperties = kpiProperties;
        }

        /**
         * Devuelve el último snapshot GLPI almacenado en MySQL o NO_DATA si
         * aún no existe histórico.
         */
        public GlpiSummary getSummary() {

                return metricsHistoryRepository
                                .findTopByOrderByCollectedAtDesc()
                                .map(this::mapHistoryToSummary)
                                .orElseGet(this::noDataSummary);
        }

        /**
         * Genera KPIs simulados de operación, SLA, actividad diaria/semanal y
         * backlog para persistirlos como snapshot.
         */
        public GlpiSummary generateSimulatedSummary() {

                GlpiSummary summary = new GlpiSummary();

                int openTickets = 80
                                +
                                random.nextInt(120);
                int arubaOpenTickets = random.nextInt(openTickets + 1);
                int remainingAfterAruba = openTickets - arubaOpenTickets;
                int citrixOpenTickets = random.nextInt(remainingAfterAruba + 1);
                int microsoft365OpenTickets = remainingAfterAruba - citrixOpenTickets;

                int criticalOpenTickets = random.nextInt(
                                Math.max(
                                                1,
                                                openTickets / 10));

                int slaBreachedTickets = random.nextInt(
                                Math.max(
                                                1,
                                                openTickets / 4));

                int averageResolutionHours = 4
                                +
                                random.nextInt(30);

                int createdToday = 10
                                +
                                random.nextInt(40);

                int closedToday = random.nextInt(
                                createdToday + 10);

                int createdThisWeek = createdToday
                                +
                                50
                                +
                                random.nextInt(120);

                int closedThisWeek = random.nextInt(
                                createdThisWeek + 1);

                int operationalBacklog = Math.max(
                                0,
                                openTickets);

                summary.setOpenTickets(
                                openTickets);
                summary.setArubaOpenTickets(arubaOpenTickets);
                summary.setCitrixOpenTickets(citrixOpenTickets);
                summary.setMicrosoft365OpenTickets(microsoft365OpenTickets);

                summary.setCriticalOpenTickets(
                                criticalOpenTickets);

                summary.setSlaBreachedTickets(
                                slaBreachedTickets);

                summary.setAverageResolutionHours(
                                averageResolutionHours);

                summary.setCreatedToday(
                                createdToday);

                summary.setClosedToday(
                                closedToday);

                summary.setCreatedThisWeek(
                                createdThisWeek);

                summary.setClosedThisWeek(
                                closedThisWeek);

                summary.setOperationalBacklog(
                                operationalBacklog);
                summary.setGlpiHealthDetails(
                                calculateGlpiHealthDetails(
                                                openTickets,
                                                criticalOpenTickets,
                                                createdToday,
                                                closedToday,
                                                createdThisWeek,
                                                closedThisWeek));
                summary.setGlpiHealthKpi(
                                buildGlpiHealthKpi(
                                                summary.getGlpiHealthDetails(),
                                                LocalDateTime.now(),
                                                "SIMULATED"));

                return summary;
        }

        private GlpiSummary mapHistoryToSummary(
                        GlpiMetricsHistory history) {

                GlpiSummary summary = new GlpiSummary();

                summary.setOpenTickets(history.getOpenTickets());
                summary.setArubaOpenTickets(history.getArubaOpenTickets());
                summary.setCitrixOpenTickets(history.getCitrixOpenTickets());
                summary.setMicrosoft365OpenTickets(history.getMicrosoft365OpenTickets());
                summary.setCriticalOpenTickets(history.getCriticalOpenTickets());
                summary.setSlaBreachedTickets(history.getSlaBreachedTickets());
                summary.setAverageResolutionHours(
                                history.getAverageResolutionHours());
                summary.setCreatedToday(history.getCreatedToday());
                summary.setClosedToday(history.getClosedToday());
                summary.setCreatedThisWeek(history.getCreatedThisWeek());
                summary.setClosedThisWeek(history.getClosedThisWeek());
                summary.setOperationalBacklog(history.getOperationalBacklog());
                summary.setGlpiHealthDetails(
                                calculateGlpiHealthDetails(
                                                history.getOpenTickets(),
                                                history.getCriticalOpenTickets(),
                                                history.getCreatedToday(),
                                                history.getClosedToday(),
                                                history.getCreatedThisWeek(),
                                                history.getClosedThisWeek()));
                summary.setLastUpdated(history.getCollectedAt());
                summary.setDataStatus(
                                calculateDataStatus(history.getCollectedAt()));
                summary.setGlpiHealthKpi(
                                buildGlpiHealthKpi(
                                                summary.getGlpiHealthDetails(),
                                                history.getCollectedAt(),
                                                summary.getDataStatus()));

                return summary;
        }

        private GlpiSummary noDataSummary() {

                // GLPI dispone de dataStatus.
                // Si no hay snapshot en MySQL,
                // se devuelve NO_DATA para evitar
                // mostrar métricas simuladas falsas.
                GlpiSummary summary = new GlpiSummary();

                summary.setDataStatus("NO_DATA");
                summary.setGlpiHealthDetails(noDataGlpiHealthDetails());
                summary.setGlpiHealthKpi(
                                buildGlpiHealthKpi(
                                                summary.getGlpiHealthDetails(),
                                                null,
                                                summary.getDataStatus()));

                return summary;
        }

        private String calculateDataStatus(
                        LocalDateTime collectedAt) {

                // OK: snapshot reciente.
                // STALE: existe, pero supera
                // el margen esperado.
                // NO_DATA: no hay snapshot.

                if (collectedAt == null) {

                        return "NO_DATA";
                }

                if (collectedAt.isAfter(
                                LocalDateTime.now().minusMinutes(2))) {

                        return "OK";
                }

                return "STALE";
        }

        /**
         * Convierte los indicadores principales de operación en una afección
         * normalizada 0-100 para usar el mismo semáforo que el resto de
         * plataformas.
         */
        private GlpiHealthStatusDto calculateGlpiHealthDetails(
                        int openTickets,
                        int criticalOpenTickets,
                        int createdToday,
                        int closedToday,
                        int createdThisWeek,
                        int closedThisWeek) {

                List<GlpiIndicatorStatusDto> indicators = List.of(
                                evaluateOpenTickets(openTickets),
                                evaluateCriticalOpenTickets(
                                                criticalOpenTickets),
                                evaluateClosedPercentage(
                                                "Porcentaje de tickets cerrados",
                                                createdToday,
                                                closedToday,
                                                "diario"),
                                evaluateClosedPercentage(
                                                "Porcentaje de tickets cerrados semana",
                                                createdThisWeek,
                                                closedThisWeek,
                                                "semanal"));

                int percentage = (int) Math.round(
                                indicators.stream()
                                                .mapToInt(
                                                                GlpiIndicatorStatusDto::getAffectionPercent)
                                                .average()
                                                .orElse(100));

                String color = colorByPercentage(percentage);

                List<String> reasons = indicators.stream()
                                .filter(indicator -> !GREEN.equals(indicator.getColor()))
                                .map(GlpiIndicatorStatusDto::getReason)
                                .toList();

                GlpiHealthStatusDto details = new GlpiHealthStatusDto();

                details.setPercentage(percentage);
                details.setColor(color);
                details.setIndicators(indicators);
                details.setReasons(reasons);
                details.setAffectedService(!GREEN.equals(color));
                details.setCriticalCondition(
                                indicators.stream()
                                                .anyMatch(indicator -> RED.equals(indicator.getColor())));
                details.setTechnicalDegradationValue(percentage);
                details.setTransversalReady(true);

                return details;
        }

        private GlpiIndicatorStatusDto evaluateOpenTickets(
                        int openTickets) {

                if (openTickets >= kpiProperties.getGlpi().getOpenTicketsRedMin()) {

                        return indicator(
                                        "Tickets abiertos",
                                        RED,
                                        "Hay 201 tickets abiertos o mas");
                }

                if (openTickets >= kpiProperties.getGlpi().getOpenTicketsYellowMin()) {

                        return indicator(
                                        "Tickets abiertos",
                                        YELLOW,
                                        "Hay entre 101 y 200 tickets abiertos");
                }

                return indicator(
                                "Tickets abiertos",
                                GREEN,
                                "Hay entre 0 y 100 tickets abiertos");
        }

        private GlpiIndicatorStatusDto evaluateCriticalOpenTickets(
                        int criticalOpenTickets) {

                if (criticalOpenTickets > kpiProperties.getGlpi().getCriticalTicketsRedAbove()) {

                        return indicator(
                                        "Tickets abiertos criticos",
                                        RED,
                                        "Hay mas de 10 tickets criticos abiertos");
                }

                if (criticalOpenTickets > kpiProperties.getGlpi().getCriticalTicketsYellowAbove()) {

                        return indicator(
                                        "Tickets abiertos criticos",
                                        YELLOW,
                                        "Hay entre 1 y 10 tickets criticos abiertos");
                }

                return indicator(
                                "Tickets abiertos criticos",
                                GREEN,
                                "No hay tickets criticos abiertos");
        }

        private GlpiIndicatorStatusDto evaluateClosedPercentage(
                        String name,
                        int created,
                        int closed,
                        String periodLabel) {

                int percentage = created <= 0
                                ? 100
                                : Math.min(100, closed * 100 / created);

                if (percentage < kpiProperties.getGlpi().getClosedPercentGreenMin()) {

                        return indicator(
                                        name,
                                        YELLOW,
                                        "El porcentaje de cierre " + periodLabel
                                                        + " es menor del 50 %");
                }

                return indicator(
                                name,
                                GREEN,
                                "El porcentaje de cierre " + periodLabel
                                                + " es igual o superior al 50 %");
        }

        private GlpiIndicatorStatusDto indicator(
                        String name,
                        String color,
                        String reason) {

                GlpiIndicatorStatusDto indicator = new GlpiIndicatorStatusDto();

                indicator.setName(name);
                indicator.setColor(color);
                indicator.setAffectionPercent(affectionPercent(color));
                indicator.setReason(reason);

                return indicator;
        }

        private int affectionPercent(
                        String color) {

                if (RED.equals(color)) {

                        return kpiProperties.getAffection().getRed();
                }

                if (YELLOW.equals(color)) {

                        return kpiProperties.getAffection().getYellow();
                }

                return kpiProperties.getAffection().getGreen();
        }

        private String colorByPercentage(
                        int percentage) {

                if (percentage >= kpiProperties.getStatus().getRedMin()) {

                        return RED;
                }

                if (percentage >= kpiProperties.getStatus().getYellowMin()) {

                        return YELLOW;
                }

                return GREEN;
        }

        private GlpiHealthStatusDto noDataGlpiHealthDetails() {

                GlpiIndicatorStatusDto noData = indicator(
                                "Datos GLPI",
                                RED,
                                "No hay snapshot GLPI disponible");

                GlpiHealthStatusDto details = new GlpiHealthStatusDto();

                details.setPercentage(kpiProperties.getAffection().getRed());
                details.setColor(RED);
                details.setIndicators(List.of(noData));
                details.setReasons(List.of(noData.getReason()));
                details.setAffectedService(true);
                details.setCriticalCondition(true);
                details.setTechnicalDegradationValue(kpiProperties.getAffection().getRed());
                details.setTransversalReady(true);

                return details;
        }

        private KpiResultDto buildGlpiHealthKpi(
                        GlpiHealthStatusDto details,
                        LocalDateTime timestamp,
                        String freshness) {

                return new KpiResultDto(
                                "glpi_health",
                                "Indice de salud GLPI",
                                details.getPercentage(),
                                KpiStatus.from(details.getColor()),
                                "Afeccion normalizada de GLPI como consecuencia operativa.",
                                "Media uniforme de tickets abiertos, tickets criticos, porcentaje de cierre diario y porcentaje de cierre semanal.",
                                timestamp,
                                freshness,
                                details.getPercentage(),
                                details.getIndicators().stream()
                                                .map(indicator -> new KpiResultDto(
                                                                indicatorId(indicator.getName()),
                                                                indicator.getName(),
                                                                indicator.getAffectionPercent(),
                                                                KpiStatus.from(indicator.getColor()),
                                                                indicator.getReason(),
                                                                null,
                                                                timestamp,
                                                                freshness,
                                                                indicator.getAffectionPercent(),
                                                                List.of()))
                                                .toList());
        }

        private String indicatorId(String name) {

                return name.toLowerCase()
                                .replace(" ", "_")
                                .replace("%", "percent");
        }
}
