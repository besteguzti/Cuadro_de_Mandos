package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.AccessPointStatusDto;
import com.tfg.dashboard.dto.ArubaNetworkStatusDto;
import com.tfg.dashboard.dto.KpiResultDto;
import com.tfg.dashboard.dto.KpiStatus;
import com.tfg.dashboard.dto.SwitchStatusDto;
import com.tfg.dashboard.model.ArubaNetworkStatusHistory;
import com.tfg.dashboard.model.TransversalKpiHistory;
import com.tfg.dashboard.repository.ArubaNetworkStatusHistoryRepository;
import com.tfg.dashboard.repository.TransversalKpiHistoryRepository;

/**
 * Calcula el estado normalizado de red Aruba.
 *
 * Aplica las reglas de afección para Access Points y switches, genera motivos
 * explicativos, guarda históricos y prepara valores para el análisis
 * transversal.
 */
@Service
public class ArubaNetworkStatusService {

        private static final String GREEN = "GREEN";
        private static final String YELLOW = "YELLOW";
        private static final String RED = "RED";

        private final ArubaNetworkStatusHistoryRepository networkStatusHistoryRepository;
        private final TransversalKpiHistoryRepository transversalKpiHistoryRepository;
        private final KpiProperties kpiProperties;

        public ArubaNetworkStatusService(
                        ArubaNetworkStatusHistoryRepository networkStatusHistoryRepository,
                        TransversalKpiHistoryRepository transversalKpiHistoryRepository,
                        KpiProperties kpiProperties) {

                this.networkStatusHistoryRepository = networkStatusHistoryRepository;
                this.transversalKpiHistoryRepository = transversalKpiHistoryRepository;
                this.kpiProperties = kpiProperties;
        }

        /**
         * Calcula porcentaje, color global y motivos del estado de red Aruba.
         */
        public ArubaNetworkStatusDto buildNetworkStatusDetails(
                        int totalAps,
                        int downAps,
                        int inactiveAps,
                        int pendingFirmwareAps,
                        int totalWifiClients,
                        int mutualiaApsClients,
                        int mutualiaWifiClients,
                        int totalSwitches,
                        int downSwitches,
                        int pendingFirmwareSwitches) {

                AccessPointStatusDto accessPointStatus = buildAccessPointStatus(
                                totalAps,
                                downAps,
                                inactiveAps,
                                pendingFirmwareAps,
                                totalWifiClients,
                                mutualiaApsClients,
                                mutualiaWifiClients);

                SwitchStatusDto switchStatus = buildSwitchStatus(
                                totalSwitches,
                                downSwitches,
                                pendingFirmwareSwitches);

                int percentage = accessPointStatus.getPercentageContribution() + switchStatus.getPercentageContribution();
                String percentageColor = colorByPercentage(percentage);
                String color = applyCriticalPrecedence(percentageColor, accessPointStatus.getColor(), switchStatus.getColor());
                List<String> reasons = List.of(accessPointStatus.getReasons(), switchStatus.getReasons())
                                .stream()
                                .flatMap(List::stream)
                                .toList();

                ArubaNetworkStatusDto status = new ArubaNetworkStatusDto();

                status.setPercentage(percentage);
                status.setColor(color);
                status.setAccessPointStatus(accessPointStatus);
                status.setSwitchStatus(switchStatus);
                status.setReasons(reasons);
                status.setAffectedService(!GREEN.equals(color));
                status.setCriticalCondition(RED.equals(accessPointStatus.getColor()) || RED.equals(switchStatus.getColor()));
                status.setTechnicalDegradationValue(percentage);
                status.setTransversalReady(true);

                return status;
        }

        /**
         * Convierte el estado de red Aruba en un KPI homogéneo con componentes
         * de APs y switches.
         */
        public KpiResultDto buildNetworkStatusKpi(ArubaNetworkStatusDto details,LocalDateTime timestamp,String freshness) {

                return new KpiResultDto(
                                "aruba_network_affectation",
                                "Estado de red Aruba",
                                details.getPercentage(),
                                KpiStatus.from(details.getColor()),
                                "Afeccion normalizada de la red Aruba.",
                                "Access Points aportan hasta 50 puntos y switches hasta 50 puntos, con prevalencia de condiciones rojas.",
                                timestamp,
                                freshness,
                                details.getPercentage(),
                                List.of(
                                                new KpiResultDto(
                                                                "aruba_access_points_status",
                                                                "Estado parcial Access Points",
                                                                details.getAccessPointStatus().getPercentageContribution(),
                                                                KpiStatus.from(details.getAccessPointStatus().getColor()),
                                                                String.join("; ", details.getAccessPointStatus().getReasons()),
                                                                "Condiciones rojas=50, amarillas=25, verdes=0.",
                                                                timestamp,
                                                                freshness,
                                                                details.getAccessPointStatus().getPercentageContribution(),
                                                                List.of()),
                                                new KpiResultDto(
                                                                "aruba_switches_status",
                                                                "Estado parcial Switches",
                                                                details.getSwitchStatus().getPercentageContribution(),
                                                                KpiStatus.from(details.getSwitchStatus().getColor()),
                                                                String.join("; ", details.getSwitchStatus().getReasons()),
                                                                "Condiciones rojas=50, amarillas=25, verdes=0.",
                                                                timestamp,
                                                                freshness,
                                                                details.getSwitchStatus().getPercentageContribution(),
                                                                List.of())));
        }

        /**
         * Persiste el estado calculado y sus KPIs transversales derivados.
         */
        public void saveNetworkStatusSnapshot(ArubaNetworkStatusDto status,LocalDateTime collectedAt) {

                ArubaNetworkStatusHistory history = new ArubaNetworkStatusHistory();

                history.setPercentage(status.getPercentage());
                history.setColor(status.getColor());
                history.setAccessPointContribution(status.getAccessPointStatus().getPercentageContribution());
                history.setAccessPointColor(status.getAccessPointStatus().getColor());
                history.setSwitchContribution(status.getSwitchStatus().getPercentageContribution());
                history.setSwitchColor(status.getSwitchStatus().getColor());
                history.setAffectedService(status.isAffectedService());
                history.setCriticalCondition(status.isCriticalCondition());
                history.setTechnicalDegradationValue(status.getTechnicalDegradationValue());
                history.setReasons(String.join(" | ", status.getReasons()));
                history.setCollectedAt(collectedAt);

                networkStatusHistoryRepository.save(history);
                saveArubaTransversalSnapshot(status, collectedAt);
        }

        /**
         * Evalúa el bloque de Access Points, incluyendo clientes WiFi críticos.
         */
        private AccessPointStatusDto buildAccessPointStatus(
                        int totalAps,
                        int downAps,
                        int inactiveAps,
                        int pendingFirmwareAps,
                        int totalWifiClients,
                        int mutualiaApsClients,
                        int mutualiaWifiClients) {

                List<String> redReasons = new java.util.ArrayList<>();
                List<String> yellowReasons = new java.util.ArrayList<>();

                if (totalAps <= 0) {
                        redReasons.add("No hay Access Points registrados");
                } else {

                        if (downAps >= totalAps) {
                                redReasons.add("Todos los APs estan caidos");
                        } else if (downAps * 100 >= totalAps * kpiProperties.getAruba().getAccessPointDownRedPercent()) {

                                redReasons.add("El 50 % o mas de los APs estan caidos");
                        } else if (downAps > 0) {

                                yellowReasons.add("Hay APs caidos");
                        }
                }

                if (totalWifiClients <= 0) {

                        // La condicion global de ausencia de clientes WiFi se evalua una sola vez para no duplicar motivos por cada grupo.
                        redReasons.add("No hay clientes WiFi");
                } else {

                        if (mutualiaApsClients <= 0) {

                                redReasons.add("No hay clientes MUTUALIA-APs");
                        }

                        if (mutualiaWifiClients <= 0) {

                                redReasons.add("No hay clientes MUTUALIA-WIFI");
                        }
                }

                if (pendingFirmwareAps > 0) {

                        yellowReasons.add("Firmware pendiente en Access Points");
                }

                if (inactiveAps > 0) {

                        yellowReasons.add("Hay APs inactivos");
                }

                String color = redReasons.isEmpty()
                                ? yellowReasons.isEmpty() ? GREEN : YELLOW
                                : RED;

                int contribution = RED.equals(color)
                                ? kpiProperties.getAruba().getBlockRedContribution()
                                : YELLOW.equals(color) ? kpiProperties.getAruba().getBlockYellowContribution() : 0;

                AccessPointStatusDto status = new AccessPointStatusDto();

                status.setPercentageContribution(contribution);
                status.setColor(color);
                status.setTotalAps(totalAps);
                status.setDownAps(downAps);
                status.setInactiveAps(inactiveAps);
                status.setPendingFirmwareAps(pendingFirmwareAps);
                status.setTotalWifiClients(totalWifiClients);
                status.setMutualiaApsClients(mutualiaApsClients);
                status.setMutualiaWifiClients(mutualiaWifiClients);
                status.setReasons(RED.equals(color) ? redReasons : yellowReasons);

                return status;
        }

        /**
         * Evalúa el bloque de switches y sus condiciones de caída o firmware.
         */
        private SwitchStatusDto buildSwitchStatus(int totalSwitches,int downSwitches,int pendingFirmwareSwitches) {

                List<String> redReasons = new java.util.ArrayList<>();
                List<String> yellowReasons = new java.util.ArrayList<>();

                if (totalSwitches <= 0) {

                        redReasons.add("No hay switches registrados");
                } else if (downSwitches >= totalSwitches) {

                        redReasons.add("Todos los switches estan caidos");
                } else if (downSwitches >= kpiProperties.getAruba().getSwitchDownYellowMin()) {

                        yellowReasons.add("Hay 2 o mas switches caidos");
                }

                if (pendingFirmwareSwitches > 0) {

                        yellowReasons.add("Firmware pendiente en switches");
                }

                String color = redReasons.isEmpty()
                                ? yellowReasons.isEmpty() ? GREEN : YELLOW
                                : RED;

                int contribution = RED.equals(color)
                                ? kpiProperties.getAruba().getBlockRedContribution()
                                : YELLOW.equals(color) ? kpiProperties.getAruba().getBlockYellowContribution() : 0;

                SwitchStatusDto status = new SwitchStatusDto();

                status.setPercentageContribution(contribution);
                status.setColor(color);
                status.setTotalSwitches(totalSwitches);
                status.setDownSwitches(downSwitches);
                status.setPendingFirmwareSwitches(pendingFirmwareSwitches);
                status.setReasons(RED.equals(color) ? redReasons : yellowReasons);

                return status;
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

        private String applyCriticalPrecedence(String percentageColor,String accessPointColor,String switchColor) {

                // El rojo prevalece sobre el amarillo y el amarillo sobre el verde. Asi una condicion
                // critica no queda suavizada por un porcentaje global de 50.

                if (RED.equals(accessPointColor) || RED.equals(switchColor)) {

                        return RED;
                }

                if (YELLOW.equals(accessPointColor) || YELLOW.equals(switchColor)) {

                        return YELLOW;
                }

                return percentageColor;
        }

        /**
         * Guarda KPIs específicos de Aruba en la tabla transversal para que el
         * módulo de análisis pueda compararlos con otras fuentes.
         */
        private void saveArubaTransversalSnapshot(ArubaNetworkStatusDto status,LocalDateTime collectedAt) {

                // Estos tres codigos dejan Aruba preparado para el modulo de analisis exploratorio.
                // La afectacion y la degradacion son valores donde 100 es malo; la salud se calcula como inversa.

                List<TransversalKpiHistory> histories = List.of(
                                transversalHistory(
                                                "aruba_network_affectation",
                                                "Afectacion de red Aruba",
                                                "%",
                                                (double) status.getPercentage(),
                                                collectedAt),
                                transversalHistory(
                                                "aruba_network_degradation",
                                                "Degradacion de red Aruba",
                                                "indice 0-100",
                                                (double) status.getTechnicalDegradationValue(),
                                                collectedAt),
                                transversalHistory(
                                                "aruba_network_health",
                                                "Salud de red Aruba",
                                                "%",
                                                (double) (100 - status.getPercentage()),
                                                collectedAt));
                transversalKpiHistoryRepository.saveAll(Objects.requireNonNull(histories));
        }

        private TransversalKpiHistory transversalHistory(
                        String code,
                        String name,
                        String unit,
                        Double value,
                        LocalDateTime collectedAt) {

                TransversalKpiHistory history = new TransversalKpiHistory();

                history.setKpiCode(code);
                history.setKpiName(name);
                history.setUnit(unit);
                history.setValue(value);
                history.setCollectedAt(collectedAt);

                return history;
        }
}
