package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.CitrixHealthStatusDto;
import com.tfg.dashboard.dto.CitrixIndicatorStatusDto;
import com.tfg.dashboard.dto.KpiResultDto;
import com.tfg.dashboard.dto.KpiStatus;
import com.tfg.dashboard.model.CitrixMetricsHistory;
import com.tfg.dashboard.dto.summary.CitrixSummary;
import com.tfg.dashboard.repository.CitrixMetricsHistoryRepository;

/**
 * Servicio de Citrix simulado.
 *
 * Genera métricas dinámicas, recupera el último snapshot persistido y calcula
 * el índice de salud Citrix con la misma escala de afección que el resto de
 * plataformas.
 */
@Service
public class CitrixService {

        private final Random random = new Random();
        private static final String GREEN = "GREEN";
        private static final String YELLOW = "YELLOW";
        private static final String RED = "RED";
        private final CitrixMetricsHistoryRepository metricsHistoryRepository;
        private final KpiProperties kpiProperties;

        public CitrixService(CitrixMetricsHistoryRepository metricsHistoryRepository,KpiProperties kpiProperties) {
                this.metricsHistoryRepository = metricsHistoryRepository;
                this.kpiProperties = kpiProperties;
        }

        /**
         * Devuelve el último snapshot Citrix almacenado en MySQL o NO_DATA si
         * aún no existe histórico.
         */
        public CitrixSummary getSummary() {

                return metricsHistoryRepository.findTopByOrderByCollectedAtDesc().map(this::mapHistoryToSummary)
                                .orElseGet(this::noDataSummary);
        }

        /**
         * Genera un resumen simulado que posteriormente se guarda como snapshot.
         */
        public CitrixSummary generateSimulatedSummary() {

                // Crear DTO respuesta

                CitrixSummary summary = new CitrixSummary();

                int activeSessions = 250 + random.nextInt(200);
                int activeLicenses = 500 + random.nextInt(100);
                int totalDeliveryControllers = 4;
                int availableDeliveryControllers = 3 + random.nextInt(2);
                int disconnectedSessions = random.nextInt(40);
                int averageLogonDurationSeconds = 10 + random.nextInt(45);
                int serverLoadPercent = 40 + random.nextInt(55);
                int failedLogons = random.nextInt(15);
                CitrixHealthStatusDto citrixHealthDetails = calculateCitrixHealthDetails(
                                activeSessions,
                                availableDeliveryControllers,
                                totalDeliveryControllers,
                                averageLogonDurationSeconds,
                                serverLoadPercent,
                                failedLogons);

        
                summary.setActiveSessions(activeSessions);
                summary.setActiveLicenses(activeLicenses);
                summary.setAvailableDeliveryControllers(availableDeliveryControllers);
                summary.setTotalDeliveryControllers(totalDeliveryControllers);
                summary.setDisconnectedSessions(disconnectedSessions);
                summary.setAverageLogonDurationSeconds(averageLogonDurationSeconds);
                summary.setServerLoadPercent(serverLoadPercent);
                summary.setFailedLogons(failedLogons);
                summary.setCitrixHealth(citrixHealthDetails.getColor());
                summary.setCitrixHealthDetails(citrixHealthDetails);
                summary.setCitrixHealthKpi(buildCitrixHealthKpi(citrixHealthDetails,LocalDateTime.now(),"SIMULATED"));

                return summary;
        }

        private CitrixSummary mapHistoryToSummary(CitrixMetricsHistory history) {

                CitrixSummary summary = new CitrixSummary();

                summary.setActiveSessions(history.getActiveSessions());
                summary.setActiveLicenses(history.getActiveLicenses());
                summary.setAvailableDeliveryControllers(history.getAvailableDeliveryControllers());
                summary.setTotalDeliveryControllers(history.getTotalDeliveryControllers());
                summary.setDisconnectedSessions(history.getDisconnectedSessions());
                summary.setAverageLogonDurationSeconds(history.getAverageLogonDurationSeconds());
                summary.setServerLoadPercent(history.getServerLoadPercent());
                summary.setFailedLogons(history.getFailedLogons());
                CitrixHealthStatusDto citrixHealthDetails = calculateCitrixHealthDetails(
                                history.getActiveSessions(),
                                history.getAvailableDeliveryControllers(),
                                history.getTotalDeliveryControllers(),
                                history.getAverageLogonDurationSeconds(),
                                history.getServerLoadPercent(),
                                history.getFailedLogons());

                summary.setCitrixHealth(citrixHealthDetails.getColor());
                summary.setCitrixHealthDetails(citrixHealthDetails);
                summary.setLastUpdated(history.getCollectedAt());
                summary.setDataStatus(calculateDataStatus(history.getCollectedAt()));
                summary.setCitrixHealthKpi(buildCitrixHealthKpi(citrixHealthDetails,history.getCollectedAt(),summary.getDataStatus()));

                return summary;
        }

        private CitrixSummary noDataSummary() {

                CitrixSummary summary = new CitrixSummary();

                summary.setCitrixHealth("NO_DATA");
                summary.setDataStatus("NO_DATA");
                summary.setCitrixHealthDetails(noDataCitrixHealthDetails());
                summary.setCitrixHealthKpi(buildCitrixHealthKpi(summary.getCitrixHealthDetails(),null,summary.getDataStatus()));

                return summary;
        }

        private String calculateDataStatus(
                        LocalDateTime collectedAt) {

                
                if (collectedAt == null) {

                        return "NO_DATA";
                }

                if (collectedAt.isAfter(LocalDateTime.now().minusMinutes(2))) {

                        return "OK";
                }

                return "STALE";
        }

        
        /**
         * Convierte cinco indicadores Citrix a estados GREEN/YELLOW/RED y
         * calcula su afección media.
         */
        private CitrixHealthStatusDto calculateCitrixHealthDetails(
                        int activeSessions,
                        int availableDeliveryControllers,
                        int totalDeliveryControllers,
                        int averageLogonDurationSeconds,
                        int serverLoadPercent,
                        int failedLogons) {

                List<CitrixIndicatorStatusDto> indicators = List.of(
                                evaluateActiveSessions(activeSessions),
                                evaluateDeliveryControllers(availableDeliveryControllers,totalDeliveryControllers),
                                evaluateAverageLogonDuration(averageLogonDurationSeconds),
                                evaluateServerLoad(serverLoadPercent),
                                evaluateFailedLogons(failedLogons));

                int percentage = (int) Math.round(
                                indicators.stream()
                                                .mapToInt(CitrixIndicatorStatusDto::getAffectionPercent)
                                                .average()
                                                .orElse(100));

                String color = colorByPercentage(percentage);

                List<String> reasons = indicators.stream()
                                .filter(indicator -> !GREEN.equals(indicator.getColor()))
                                .map(CitrixIndicatorStatusDto::getReason)
                                .toList();

                CitrixHealthStatusDto details = new CitrixHealthStatusDto();

                details.setPercentage(percentage);
                details.setColor(color);
                details.setIndicators(indicators);
                details.setReasons(reasons);
                details.setAffectedService(!GREEN.equals(color));
                details.setCriticalCondition(indicators.stream().anyMatch(indicator -> RED.equals(indicator.getColor())));
                details.setTechnicalDegradationValue(percentage);
                details.setTransversalReady(true);

                return details;
        }

        private CitrixIndicatorStatusDto evaluateActiveSessions(int activeSessions) {

                if (activeSessions <= 0) {

                        return indicator("Sesiones activas",RED,"No hay sesiones activas en Citrix");
                }

                return indicator("Sesiones activas",GREEN,"Hay sesiones activas en Citrix");
        }

        private CitrixIndicatorStatusDto evaluateDeliveryControllers(int availableDeliveryControllers,int totalDeliveryControllers) {

                if (totalDeliveryControllers <= 0 || availableDeliveryControllers <= 0) {

                        return indicator("Delivery Controllers disponibles",RED,"No hay Delivery Controllers disponibles");
                }

                if (availableDeliveryControllers * 100
                                < totalDeliveryControllers * kpiProperties.getCitrix().getDeliveryControllerYellowBelowPercent()) {

                        return indicator("Delivery Controllers disponibles",YELLOW,"Menos del 50 % de Delivery Controllers disponibles");
                }

                return indicator("Delivery Controllers disponibles",GREEN,"50 % o mas de Delivery Controllers disponibles");
        }

        private CitrixIndicatorStatusDto evaluateAverageLogonDuration(int averageLogonDurationSeconds) {

                if (averageLogonDurationSeconds > kpiProperties.getCitrix().getLogonDurationRedAboveSeconds()) {

                        return indicator("Average Logon Duration",RED,"Average Logon Duration superior a 60 segundos");
                }

                if (averageLogonDurationSeconds > kpiProperties.getCitrix().getLogonDurationYellowAboveSeconds()) {

                        return indicator("Average Logon Duration",YELLOW,"Average Logon Duration entre 21 y 60 segundos");
                }

                return indicator("Average Logon Duration",GREEN,"Average Logon Duration entre 0 y 20 segundos");
        }

        private CitrixIndicatorStatusDto evaluateServerLoad(int serverLoadPercent) {

                if (serverLoadPercent >= kpiProperties.getCitrix().getServerLoadRedMin()) {

                        return indicator("Carga de servidores",RED,"Carga de servidores entre 67 % y 100 %");
                }

                if (serverLoadPercent >= kpiProperties.getCitrix().getServerLoadYellowMin()) {

                        return indicator("Carga de servidores",YELLOW,"Carga de servidores entre 34 % y 66 %");
                }

                return indicator("Carga de servidores",GREEN,"Carga de servidores entre 0 % y 33 %");
        }

        private CitrixIndicatorStatusDto evaluateFailedLogons(int failedLogons) {

                if (failedLogons > kpiProperties.getCitrix().getFailedLogonsRedAbove()) {

                        return indicator("Errores de inicio",RED,"Mas de 30 errores de inicio");
                }

                if (failedLogons > kpiProperties.getCitrix().getFailedLogonsYellowAbove()) {

                        return indicator("Errores de inicio",YELLOW,"Entre 11 y 30 errores de inicio");
                }

                return indicator("Errores de inicio",GREEN,"Entre 0 y 10 errores de inicio");
        }

        private CitrixIndicatorStatusDto indicator(String name,String color,String reason) {

                CitrixIndicatorStatusDto indicator = new CitrixIndicatorStatusDto();

                indicator.setName(name);
                indicator.setColor(color);
                indicator.setAffectionPercent(affectionPercent(color));
                indicator.setReason(reason);

                return indicator;
        }

        private int affectionPercent(String color) {

                if (RED.equals(color)) {

                        return kpiProperties.getAffection().getRed();
                }

                if (YELLOW.equals(color)) {

                        return kpiProperties.getAffection().getYellow();
                }

                return kpiProperties.getAffection().getGreen();
        }

        private String colorByPercentage(int percentage) {

                if (percentage >= kpiProperties.getStatus().getRedMin()) {

                        return RED;
                }

                if (percentage >= kpiProperties.getStatus().getYellowMin()) {

                        return YELLOW;
                }

                return GREEN;
        }

        private KpiResultDto buildCitrixHealthKpi(CitrixHealthStatusDto details,LocalDateTime timestamp,String freshness) {

                return new KpiResultDto(
                                "citrix_health",
                                "Indice de salud Citrix",
                                details.getPercentage(),
                                KpiStatus.from(details.getColor()),
                                "Afeccion normalizada del entorno Citrix.",
                                "Media uniforme de sesiones activas, Delivery Controllers, logon duration, carga de servidores y errores de inicio.",
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

                return name.toLowerCase().replace(" ", "_").replace("%", "percent");
        }

        private CitrixHealthStatusDto noDataCitrixHealthDetails() {

                CitrixIndicatorStatusDto noData = indicator("Datos Citrix",RED,"No hay snapshot Citrix disponible");

                CitrixHealthStatusDto details = new CitrixHealthStatusDto();

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

}
