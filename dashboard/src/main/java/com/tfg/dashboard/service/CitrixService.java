package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.dto.CitrixHealthStatusDto;
import com.tfg.dashboard.dto.CitrixIndicatorStatusDto;
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

    private static final String GREEN = "GREEN";

    private static final String YELLOW = "YELLOW";

    private static final String RED = "RED";

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

        CitrixHealthStatusDto citrixHealthDetails =
                calculateCitrixHealthDetails(
                        activeSessions,
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
                citrixHealthDetails.getColor()
        );
        summary.setCitrixHealthDetails(citrixHealthDetails);

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
        CitrixHealthStatusDto citrixHealthDetails =
                calculateCitrixHealthDetails(
                        history.getActiveSessions(),
                        history.getAvailableDeliveryControllers(),
                        history.getTotalDeliveryControllers(),
                        history.getAverageLogonDurationSeconds(),
                        history.getServerLoadPercent(),
                        history.getFailedLogons()
                );

        summary.setCitrixHealth(citrixHealthDetails.getColor());
        summary.setCitrixHealthDetails(citrixHealthDetails);
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
        summary.setCitrixHealthDetails(noDataCitrixHealthDetails());

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

    // =========================
    // Calculo salud Citrix normalizado
    // =========================
    //
    // Cada indicador se convierte a
    // verde, amarillo o rojo y aporta
    // el mismo peso al porcentaje
    // global de afeccion.
    //

    private CitrixHealthStatusDto calculateCitrixHealthDetails(
            int activeSessions,
            int availableDeliveryControllers,
            int totalDeliveryControllers,
            int averageLogonDurationSeconds,
            int serverLoadPercent,
            int failedLogons
    ) {

        List<CitrixIndicatorStatusDto> indicators =
                List.of(
                        evaluateActiveSessions(activeSessions),
                        evaluateDeliveryControllers(
                                availableDeliveryControllers,
                                totalDeliveryControllers
                        ),
                        evaluateAverageLogonDuration(
                                averageLogonDurationSeconds
                        ),
                        evaluateServerLoad(serverLoadPercent),
                        evaluateFailedLogons(failedLogons)
                );

        int percentage =
                (int) Math.round(
                        indicators.stream()
                                .mapToInt(
                                        CitrixIndicatorStatusDto
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
                        .map(CitrixIndicatorStatusDto::getReason)
                        .toList();

        CitrixHealthStatusDto details =
                new CitrixHealthStatusDto();

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

    private CitrixIndicatorStatusDto evaluateActiveSessions(
            int activeSessions
    ) {

        if (activeSessions <= 0) {

            return indicator(
                    "Sesiones activas",
                    RED,
                    "No hay sesiones activas en Citrix"
            );
        }

        return indicator(
                "Sesiones activas",
                GREEN,
                "Hay sesiones activas en Citrix"
        );
    }

    private CitrixIndicatorStatusDto evaluateDeliveryControllers(
            int availableDeliveryControllers,
            int totalDeliveryControllers
    ) {

        if (totalDeliveryControllers <= 0
                || availableDeliveryControllers <= 0) {

            return indicator(
                    "Delivery Controllers disponibles",
                    RED,
                    "No hay Delivery Controllers disponibles"
            );
        }

        if (availableDeliveryControllers * 100
                < totalDeliveryControllers * 50) {

            return indicator(
                    "Delivery Controllers disponibles",
                    YELLOW,
                    "Menos del 50 % de Delivery Controllers disponibles"
            );
        }

        return indicator(
                "Delivery Controllers disponibles",
                GREEN,
                "50 % o mas de Delivery Controllers disponibles"
        );
    }

    private CitrixIndicatorStatusDto evaluateAverageLogonDuration(
            int averageLogonDurationSeconds
    ) {

        if (averageLogonDurationSeconds > 60) {

            return indicator(
                    "Average Logon Duration",
                    RED,
                    "Average Logon Duration superior a 60 segundos"
            );
        }

        if (averageLogonDurationSeconds > 20) {

            return indicator(
                    "Average Logon Duration",
                    YELLOW,
                    "Average Logon Duration entre 21 y 60 segundos"
            );
        }

        return indicator(
                "Average Logon Duration",
                GREEN,
                "Average Logon Duration entre 0 y 20 segundos"
        );
    }

    private CitrixIndicatorStatusDto evaluateServerLoad(
            int serverLoadPercent
    ) {

        if (serverLoadPercent >= 67) {

            return indicator(
                    "Carga de servidores",
                    RED,
                    "Carga de servidores entre 67 % y 100 %"
            );
        }

        if (serverLoadPercent >= 34) {

            return indicator(
                    "Carga de servidores",
                    YELLOW,
                    "Carga de servidores entre 34 % y 66 %"
            );
        }

        return indicator(
                "Carga de servidores",
                GREEN,
                "Carga de servidores entre 0 % y 33 %"
        );
    }

    private CitrixIndicatorStatusDto evaluateFailedLogons(
            int failedLogons
    ) {

        if (failedLogons > 30) {

            return indicator(
                    "Errores de inicio",
                    RED,
                    "Mas de 30 errores de inicio"
            );
        }

        if (failedLogons > 10) {

            return indicator(
                    "Errores de inicio",
                    YELLOW,
                    "Entre 11 y 30 errores de inicio"
            );
        }

        return indicator(
                "Errores de inicio",
                GREEN,
                "Entre 0 y 10 errores de inicio"
        );
    }

    private CitrixIndicatorStatusDto indicator(
            String name,
            String color,
            String reason
    ) {

        CitrixIndicatorStatusDto indicator =
                new CitrixIndicatorStatusDto();

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

    private CitrixHealthStatusDto noDataCitrixHealthDetails() {

        CitrixIndicatorStatusDto noData =
                indicator(
                        "Datos Citrix",
                        RED,
                        "No hay snapshot Citrix disponible"
                );

        CitrixHealthStatusDto details =
                new CitrixHealthStatusDto();

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
