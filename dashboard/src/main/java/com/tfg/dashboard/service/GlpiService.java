package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;

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
}
