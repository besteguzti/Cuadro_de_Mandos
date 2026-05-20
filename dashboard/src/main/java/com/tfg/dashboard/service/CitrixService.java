package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.model.CitrixMetricsHistory;
import com.tfg.dashboard.model.CitrixSummary;
import com.tfg.dashboard.repository.CitrixMetricsHistoryRepository;

@Service
public class CitrixService {

    // =========================
    // Generador temporal MOCK
    // =========================
    //
    // En Entrega 2 NO se conecta
    // con API real de Citrix.
    //
    // Este generador permite
    // simular valores realistas
    // manteniendo la arquitectura.
    //
    private final Random random =
            new Random();

    private final CitrixMetricsHistoryRepository metricsHistoryRepository;

    public CitrixService(
            CitrixMetricsHistoryRepository metricsHistoryRepository
    ) {

        this.metricsHistoryRepository =
                metricsHistoryRepository;
    }

    // =========================
    // Resumen persistido Citrix
    // =========================
    //
    // El endpoint /citrix/summary
    // devuelve el último snapshot
    // almacenado en MySQL.
    //
    public CitrixSummary getSummary() {

        return metricsHistoryRepository
                .findTopByOrderByCollectedAtDesc()
                .map(this::mapHistoryToSummary)
                .orElseGet(this::noDataSummary);
    }

    // =========================
    // Generación resumen Citrix
    // =========================
    //
    // Simula métricas operativas
    // que posteriormente podrán
    // sustituirse por API real.
    //
    public CitrixSummary generateSimulatedSummary() {

        // Crear DTO respuesta

        CitrixSummary summary =
                new CitrixSummary();

        // =========================
        // KPI:
        // Sesiones activas
        // =========================
        //
        // Usuarios actualmente
        // utilizando Citrix
        //

        int activeSessions =
                250
                +
                random.nextInt(200);

        // =========================
        // KPI:
        // Licencias activas
        // =========================
        //
        // Usuarios con licencia
        // disponible/asignada
        //

        int activeLicenses =
                500
                +
                random.nextInt(100);

        // =========================
        // KPI:
        // Delivery Controllers
        // =========================
        //
        // Total servidores que
        // gestionan sesiones
        //

        int totalDeliveryControllers =
                4;

        // Servidores actualmente
        // disponibles

        int availableDeliveryControllers =
                3
                +
                random.nextInt(2);

        // =========================
        // KPI:
        // Sesiones desconectadas
        // =========================
        //
        // Sesiones abiertas pero
        // sin usuario activo
        //

        int disconnectedSessions =
                random.nextInt(40);

        // =========================
        // KPI:
        // Average Logon Duration
        // =========================
        //
        // Tiempo medio de inicio
        // de sesión (segundos)
        //

        int averageLogonDurationSeconds =
                10
                +
                random.nextInt(45);

        // =========================
        // KPI:
        // Carga servidores
        // =========================
        //
        // Utilización media (%)
        //

        int serverLoadPercent =
                40
                +
                random.nextInt(55);

        // =========================
        // KPI:
        // Errores de inicio
        // =========================

        int failedLogons =
                random.nextInt(15);

        // =========================
        // KPI:
        // Índice salud Citrix
        // =========================

        String citrixHealth =
                calculateCitrixHealth(
                        availableDeliveryControllers,
                        totalDeliveryControllers,
                        averageLogonDurationSeconds,
                        serverLoadPercent,
                        failedLogons
                );

        // =========================
        // Construcción respuesta
        // =========================

        summary.setActiveSessions(
                activeSessions
        );

        summary.setActiveLicenses(
                activeLicenses
        );

        summary.setAvailableDeliveryControllers(
                availableDeliveryControllers
        );

        summary.setTotalDeliveryControllers(
                totalDeliveryControllers
        );

        summary.setDisconnectedSessions(
                disconnectedSessions
        );

        summary.setAverageLogonDurationSeconds(
                averageLogonDurationSeconds
        );

        summary.setServerLoadPercent(
                serverLoadPercent
        );

        summary.setFailedLogons(
                failedLogons
        );

        summary.setCitrixHealth(
                citrixHealth
        );

        return summary;
    }

    private CitrixSummary mapHistoryToSummary(
            CitrixMetricsHistory history
    ) {

        CitrixSummary summary =
                new CitrixSummary();

        summary.setActiveSessions(history.getActiveSessions());
        summary.setActiveLicenses(history.getActiveLicenses());
        summary.setAvailableDeliveryControllers(
                history.getAvailableDeliveryControllers()
        );
        summary.setTotalDeliveryControllers(
                history.getTotalDeliveryControllers()
        );
        summary.setDisconnectedSessions(history.getDisconnectedSessions());
        summary.setAverageLogonDurationSeconds(
                history.getAverageLogonDurationSeconds()
        );
        summary.setServerLoadPercent(history.getServerLoadPercent());
        summary.setFailedLogons(history.getFailedLogons());
        summary.setCitrixHealth(history.getCitrixHealth());
        summary.setLastUpdated(history.getCollectedAt());
        summary.setDataStatus(
                calculateDataStatus(history.getCollectedAt())
        );

        return summary;
    }

    private CitrixSummary noDataSummary() {

        CitrixSummary summary =
                new CitrixSummary();

        // Existe campo específico
        // de frescura en el DTO actual.
        // Usamos NO_DATA para evitar
        // devolver un GREEN falso si
        // la tabla histórica está vacía.
        summary.setCitrixHealth("NO_DATA");
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

    // =========================
    // Cálculo salud Citrix
    // =========================
    //
    // Reglas:
    //
    // RED
    // - Controllers caídos
    // - Login lento
    // - Mucha carga
    //
    // YELLOW
    // - Riesgo moderado
    //
    // GREEN
    // - Estado correcto
    //

    private String calculateCitrixHealth(

            int availableDeliveryControllers,

            int totalDeliveryControllers,

            int averageLogonDurationSeconds,

            int serverLoadPercent,

            int failedLogons

    ) {

        // Estado crítico

        if (

                availableDeliveryControllers
                <
                totalDeliveryControllers

                ||

                averageLogonDurationSeconds
                >
                40

                ||

                serverLoadPercent
                >
                85

                ||

                failedLogons
                >
                10

        ) {

            return "RED";
        }

        // Estado degradado

        if (

                averageLogonDurationSeconds
                >
                25

                ||

                serverLoadPercent
                >
                70

                ||

                failedLogons
                >
                5

        ) {

            return "YELLOW";
        }

        // Estado saludable

        return "GREEN";
    }

}
