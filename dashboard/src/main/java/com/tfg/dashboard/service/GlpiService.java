package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.dto.GlpiHealthStatusDto;
import com.tfg.dashboard.dto.GlpiIndicatorStatusDto;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.model.GlpiSummary;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;

@Service
public class GlpiService {

    // =========================
    // Generador temporal MOCK
    // =========================
    //
    // En esta fase no se conecta
    // con la API real de GLPI.
    //
    // Se generan datos dinámicos
    // para validar la arquitectura
    // multiproveedor del dashboard.
    //
    private final Random random =
            new Random();

    private static final String GREEN = "GREEN";

    private static final String YELLOW = "YELLOW";

    private static final String RED = "RED";

    private final GlpiMetricsHistoryRepository metricsHistoryRepository;

    public GlpiService(
            GlpiMetricsHistoryRepository metricsHistoryRepository
    ) {

        this.metricsHistoryRepository =
                metricsHistoryRepository;
    }

    // =========================
    // Resumen persistido GLPI
    // =========================
    //
    // El endpoint /glpi/summary
    // devuelve el último snapshot
    // almacenado en MySQL.
    //
    public GlpiSummary getSummary() {

        return metricsHistoryRepository
                .findTopByOrderByCollectedAtDesc()
                .map(this::mapHistoryToSummary)
                .orElseGet(this::noDataSummary);
    }

    // =========================
    // Generación resumen GLPI
    // =========================
    //
    // Devuelve KPIs simulados
    // relacionados con operación,
    // SLA, actividad diaria,
    // actividad semanal y backlog.
    //
    public GlpiSummary generateSimulatedSummary() {

        GlpiSummary summary =
                new GlpiSummary();

        // =========================
        // Tickets abiertos
        // =========================
        //
        // Representa la carga actual
        // pendiente del departamento IT.
        //

        int openTickets =
                80
                +
                random.nextInt(120);

        // =========================
        // Tickets críticos abiertos
        // =========================
        //
        // Regla:
        // nunca puede ser mayor que
        // el número total de tickets
        // abiertos.
        //

        int criticalOpenTickets =
                random.nextInt(
                        Math.max(
                                1,
                                openTickets / 10
                        )
                );

        // =========================
        // Tickets vencidos SLA
        // =========================
        //
        // Regla:
        // nunca puede superar
        // los tickets abiertos.
        //

        int slaBreachedTickets =
                random.nextInt(
                        Math.max(
                                1,
                                openTickets / 4
                        )
                );

        // =========================
        // Tiempo medio resolución
        // =========================
        //
        // Expresado en horas.
        //

        int averageResolutionHours =
                4
                +
                random.nextInt(30);

        // =========================
        // Actividad diaria
        // =========================
        //
        // Tickets creados hoy.
        //

        int createdToday =
                10
                +
                random.nextInt(40);

        // Tickets cerrados hoy.
        //
        // Regla:
        // puede ser mayor que
        // los creados hoy, porque se
        // pueden cerrar tickets antiguos,
        // pero lo limitamos para mantener
        // valores coherentes en demo.
        //

        int closedToday =
                random.nextInt(
                        createdToday + 10
                );

        // =========================
        // Actividad semanal
        // =========================
        //
        // Tickets creados durante
        // la semana en curso.
        //

        int createdThisWeek =
                createdToday
                +
                50
                +
                random.nextInt(120);

        // Tickets cerrados durante
        // la semana en curso.
        //
        // Regla:
        // se mantiene menor o igual
        // que creados de la semana
        // para evitar datos absurdos
        // en esta simulación.
        //

        int closedThisWeek =
                random.nextInt(
                        createdThisWeek + 1
                );

        // =========================
        // Backlog operativo
        // =========================
        //
        // Carga pendiente estimada.
        //
        // En este mock lo aproximamos
        // a tickets abiertos menos
        // parte de los cierres recientes.
        //

        int operationalBacklog =
                Math.max(
                        0,
                        openTickets
                );

        // =========================
        // Construcción respuesta
        // =========================

        summary.setOpenTickets(
                openTickets
        );

        summary.setCriticalOpenTickets(
                criticalOpenTickets
        );

        summary.setSlaBreachedTickets(
                slaBreachedTickets
        );

        summary.setAverageResolutionHours(
                averageResolutionHours
        );

        summary.setCreatedToday(
                createdToday
        );

        summary.setClosedToday(
                closedToday
        );

        summary.setCreatedThisWeek(
                createdThisWeek
        );

        summary.setClosedThisWeek(
                closedThisWeek
        );

        summary.setOperationalBacklog(
                operationalBacklog
        );
        summary.setGlpiHealthDetails(
                calculateGlpiHealthDetails(
                        openTickets,
                        criticalOpenTickets,
                        createdToday,
                        closedToday,
                        createdThisWeek,
                        closedThisWeek
                )
        );

        return summary;
    }

    private GlpiSummary mapHistoryToSummary(
            GlpiMetricsHistory history
    ) {

        GlpiSummary summary =
                new GlpiSummary();

        summary.setOpenTickets(history.getOpenTickets());
        summary.setCriticalOpenTickets(history.getCriticalOpenTickets());
        summary.setSlaBreachedTickets(history.getSlaBreachedTickets());
        summary.setAverageResolutionHours(
                history.getAverageResolutionHours()
        );
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
                        history.getClosedThisWeek()
                )
        );
        summary.setLastUpdated(history.getCollectedAt());
        summary.setDataStatus(
                calculateDataStatus(history.getCollectedAt())
        );

        return summary;
    }

    private GlpiSummary noDataSummary() {

       // GLPI dispone de dataStatus.
       // Si no hay snapshot en MySQL,
       // se devuelve NO_DATA para evitar
       // mostrar métricas simuladas falsas.
        GlpiSummary summary =
                new GlpiSummary();

        summary.setDataStatus("NO_DATA");
        summary.setGlpiHealthDetails(noDataGlpiHealthDetails());

        return summary;
    }

    private String calculateDataStatus(
            LocalDateTime collectedAt
    ) {

        // OK: snapshot reciente.
        // STALE: existe, pero supera
        // el margen esperado.
        // NO_DATA: no hay snapshot.

        if (collectedAt == null) {

            return "NO_DATA";
        }

        if (collectedAt.isAfter(
                LocalDateTime.now().minusMinutes(2)
        )) {

            return "OK";
        }

        return "STALE";
    }

    // =========================
    // Indice salud GLPI
    // =========================
    //
    // Convierte los indicadores
    // principales de operacion en
    // una afeccion normalizada de
    // 0 a 100 para usar el mismo
    // semaforo que Aruba.
    //

    private GlpiHealthStatusDto calculateGlpiHealthDetails(
            int openTickets,
            int criticalOpenTickets,
            int createdToday,
            int closedToday,
            int createdThisWeek,
            int closedThisWeek
    ) {

        List<GlpiIndicatorStatusDto> indicators =
                List.of(
                        evaluateOpenTickets(openTickets),
                        evaluateCriticalOpenTickets(
                                criticalOpenTickets
                        ),
                        evaluateClosedPercentage(
                                "Porcentaje de tickets cerrados",
                                createdToday,
                                closedToday,
                                "diario"
                        ),
                        evaluateClosedPercentage(
                                "Porcentaje de tickets cerrados semana",
                                createdThisWeek,
                                closedThisWeek,
                                "semanal"
                        )
                );

        int percentage =
                (int) Math.round(
                        indicators.stream()
                                .mapToInt(
                                        GlpiIndicatorStatusDto
                                                ::getAffectionPercent
                                )
                                .average()
                                .orElse(100)
                );

        String color =
                colorByPercentage(percentage);

        List<String> reasons =
                indicators.stream()
                        .filter(indicator ->
                                !GREEN.equals(indicator.getColor()))
                        .map(GlpiIndicatorStatusDto::getReason)
                        .toList();

        GlpiHealthStatusDto details =
                new GlpiHealthStatusDto();

        details.setPercentage(percentage);
        details.setColor(color);
        details.setIndicators(indicators);
        details.setReasons(reasons);
        details.setAffectedService(!GREEN.equals(color));
        details.setCriticalCondition(
                indicators.stream()
                        .anyMatch(indicator ->
                                RED.equals(indicator.getColor()))
        );
        details.setTechnicalDegradationValue(percentage);
        details.setTransversalReady(true);

        return details;
    }

    private GlpiIndicatorStatusDto evaluateOpenTickets(
            int openTickets
    ) {

        if (openTickets >= 201) {

            return indicator(
                    "Tickets abiertos",
                    RED,
                    "Hay 201 tickets abiertos o mas"
            );
        }

        if (openTickets >= 101) {

            return indicator(
                    "Tickets abiertos",
                    YELLOW,
                    "Hay entre 101 y 200 tickets abiertos"
            );
        }

        return indicator(
                "Tickets abiertos",
                GREEN,
                "Hay entre 0 y 100 tickets abiertos"
        );
    }

    private GlpiIndicatorStatusDto evaluateCriticalOpenTickets(
            int criticalOpenTickets
    ) {

        if (criticalOpenTickets > 10) {

            return indicator(
                    "Tickets abiertos criticos",
                    RED,
                    "Hay mas de 10 tickets criticos abiertos"
            );
        }

        if (criticalOpenTickets > 0) {

            return indicator(
                    "Tickets abiertos criticos",
                    YELLOW,
                    "Hay entre 1 y 10 tickets criticos abiertos"
            );
        }

        return indicator(
                "Tickets abiertos criticos",
                GREEN,
                "No hay tickets criticos abiertos"
        );
    }

    private GlpiIndicatorStatusDto evaluateClosedPercentage(
            String name,
            int created,
            int closed,
            String periodLabel
    ) {

        int percentage =
                created <= 0
                        ? 100
                        : Math.min(100, closed * 100 / created);

        if (percentage < 50) {

            return indicator(
                    name,
                    YELLOW,
                    "El porcentaje de cierre " + periodLabel
                            + " es menor del 50 %"
            );
        }

        return indicator(
                name,
                GREEN,
                "El porcentaje de cierre " + periodLabel
                        + " es igual o superior al 50 %"
        );
    }

    private GlpiIndicatorStatusDto indicator(
            String name,
            String color,
            String reason
    ) {

        GlpiIndicatorStatusDto indicator =
                new GlpiIndicatorStatusDto();

        indicator.setName(name);
        indicator.setColor(color);
        indicator.setAffectionPercent(affectionPercent(color));
        indicator.setReason(reason);

        return indicator;
    }

    private int affectionPercent(
            String color
    ) {

        if (RED.equals(color)) {

            return 100;
        }

        if (YELLOW.equals(color)) {

            return 50;
        }

        return 0;
    }

    private String colorByPercentage(
            int percentage
    ) {

        if (percentage >= 67) {

            return RED;
        }

        if (percentage >= 34) {

            return YELLOW;
        }

        return GREEN;
    }

    private GlpiHealthStatusDto noDataGlpiHealthDetails() {

        GlpiIndicatorStatusDto noData =
                indicator(
                        "Datos GLPI",
                        RED,
                        "No hay snapshot GLPI disponible"
                );

        GlpiHealthStatusDto details =
                new GlpiHealthStatusDto();

        details.setPercentage(100);
        details.setColor(RED);
        details.setIndicators(List.of(noData));
        details.setReasons(List.of(noData.getReason()));
        details.setAffectedService(true);
        details.setCriticalCondition(true);
        details.setTechnicalDegradationValue(100);
        details.setTransversalReady(true);

        return details;
    }
}
