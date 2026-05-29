package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.ArubaNetworkStatusDto;
import com.tfg.dashboard.model.AccessPoint;
import com.tfg.dashboard.model.ArubaDashboardMetrics;
import com.tfg.dashboard.dto.summary.ArubaSummary;
import com.tfg.dashboard.model.ArubaSwitch;
import com.tfg.dashboard.model.ArubaSwitchClientUsage;
import com.tfg.dashboard.model.ArubaSwitchInterfaceUsageHistory;
import com.tfg.dashboard.repository.AccessPointRepository;
import com.tfg.dashboard.repository.ArubaDashboardMetricsRepository;
import com.tfg.dashboard.repository.ArubaSwitchClientUsageRepository;
import com.tfg.dashboard.repository.ArubaSwitchInterfaceUsageHistoryRepository;
import com.tfg.dashboard.repository.ArubaSwitchRepository;

/**
 * Construye el resumen Aruba que consume el frontend.
 *
 * Lee entidades persistidas en MySQL, calcula contadores de APs, switches,
 * clientes WiFi y frescura, y delega el índice de estado de red en
 * ArubaNetworkStatusService.
 */
@Service
public class ArubaSummaryService {

        private static final long METRICS_ID = 1L;
        private final AccessPointRepository accessPointRepository;
        private final ArubaSwitchRepository arubaSwitchRepository;
        private final ArubaSwitchClientUsageRepository switchClientUsageRepository;
        private final ArubaSwitchInterfaceUsageHistoryRepository switchInterfaceUsageHistoryRepository;
        private final ArubaDashboardMetricsRepository dashboardMetricsRepository;
        private final ArubaSwitchUsageService switchUsageService;
        private final ArubaNetworkStatusService networkStatusService;
        private final GlpiPlatformTicketService glpiPlatformTicketService;
        private final KpiProperties kpiProperties;

        public ArubaSummaryService(
                        AccessPointRepository accessPointRepository,
                        ArubaSwitchRepository arubaSwitchRepository,
                        ArubaSwitchClientUsageRepository switchClientUsageRepository,
                        ArubaSwitchInterfaceUsageHistoryRepository switchInterfaceUsageHistoryRepository,
                        ArubaDashboardMetricsRepository dashboardMetricsRepository,
                        ArubaSwitchUsageService switchUsageService,
                        ArubaNetworkStatusService networkStatusService,
                        GlpiPlatformTicketService glpiPlatformTicketService,
                        KpiProperties kpiProperties) {

                this.accessPointRepository = accessPointRepository;
                this.arubaSwitchRepository = arubaSwitchRepository;
                this.switchClientUsageRepository = switchClientUsageRepository;
                this.switchInterfaceUsageHistoryRepository = switchInterfaceUsageHistoryRepository;
                this.dashboardMetricsRepository = dashboardMetricsRepository;
                this.switchUsageService = switchUsageService;
                this.networkStatusService = networkStatusService;
                this.glpiPlatformTicketService = glpiPlatformTicketService;
                this.kpiProperties = kpiProperties;
        }

        /**
         * Ensambla el DTO ArubaSummary a partir de datos ya sincronizados.
         */
        public ArubaSummary getSummary() {

                List<AccessPoint> aps = accessPointRepository.findAll();
                List<ArubaSwitch> switches = arubaSwitchRepository.findAll();
                ArubaDashboardMetrics metrics = dashboardMetricsRepository.findById(METRICS_ID)
                                .orElseGet(ArubaDashboardMetrics::new);

                int totalAps = aps.size();
                int upAps = (int) aps.stream()
                                .filter(ap -> ap.getStatus() != null && ap.getStatus().equalsIgnoreCase("Up"))
                                .count();
                int downAps = totalAps - upAps;
                int totalSites = (int) aps.stream().map(AccessPoint::getSite)
                                .filter(site -> site != null && !site.isBlank()).distinct().count();
                int totalSwarms = (int) aps.stream().map(AccessPoint::getSwarmName)
                                .filter(swarm -> swarm != null && !swarm.isBlank()).distinct().count();
                int apsWithoutPublicIp = (int) aps.stream()
                                .filter(ap -> ap.getPublicIpAddress() == null || ap.getPublicIpAddress().isBlank())
                                .count();
                int totalSwitches = switches.size();
                int downSwitches = (int) switches.stream().filter(switchInfo -> switchInfo.getDeviceStatus() == null
                                || !switchInfo.getDeviceStatus().equalsIgnoreCase("Up")).count();
                int switchesFirmwareUpgradeRequired = (int) switches.stream().filter(ArubaSwitch::isUpgradeRequired)
                                .count();

                LocalDateTime limitDate = LocalDateTime.now().minusMonths(3);
                long inactiveAps = accessPointRepository.countBySerialIsNotNullAndLastSeenAtBefore(limitDate);

                ArubaNetworkStatusDto networkStatusDetails = networkStatusService.buildNetworkStatusDetails(
                                totalAps,
                                downAps,
                                (int) inactiveAps,
                                metrics.getFirmwareOutdated(),
                                metrics.getTotalWifiClients(),
                                metrics.getMutualiaApsClients(),
                                metrics.getMutualiaWifiClients(),
                                totalSwitches,
                                downSwitches,
                                switchesFirmwareUpgradeRequired);

                String networkStatus = networkStatusDetails.getColor();
                LocalDateTime lastUpdated = resolveArubaLastUpdated();
                String dataStatus = calculateDataStatus(lastUpdated);

                if ("NO_DATA".equalsIgnoreCase(dataStatus)) {

                        // Si no existe ninguna fecha Aruba persistida, el resumen no debe quedar como GREEN.
                        // UNKNOWN evita ocultar que no hay datos suficientes sin modificar los calculos de KPIs.
                        networkStatus = "UNKNOWN";
                }

                ArubaSummary summary = new ArubaSummary();

                summary.setTotalAps(totalAps);
                summary.setUpAps(upAps);
                summary.setDownAps(downAps);
                summary.setTotalSites(totalSites);
                summary.setTotalSwarms(totalSwarms);
                summary.setFirmwareOutdated(metrics.getFirmwareOutdated());
                summary.setApsWithoutPublicIp(apsWithoutPublicIp);
                summary.setInactiveAps((int) inactiveAps);
                summary.setNetworkStatus(networkStatus);
                summary.setNetworkStatusDetails(networkStatusDetails);
                summary.setNetworkStatusKpi(networkStatusService.buildNetworkStatusKpi(networkStatusDetails, lastUpdated, dataStatus));
                summary.setTotalSwitches(totalSwitches);
                summary.setDownSwitches(downSwitches);
                summary.setSwitchesFirmwareUpgradeRequired(switchesFirmwareUpgradeRequired);
                summary.setUnderusedSwitches(switchUsageService.getUnderusedSwitches());
                summary.setTotalWifiClients(metrics.getTotalWifiClients());
                summary.setArubaOpenTickets(glpiPlatformTicketService.getArubaOpenTickets());
                summary.setMutualiaApsClients(metrics.getMutualiaApsClients());
                summary.setMutualiaWifiClients(metrics.getMutualiaWifiClients());
                summary.setMutualiaLangileakClients(metrics.getMutualiaLangileakClients());
                summary.setMutualiaClients(metrics.getMutualiaClients());
                summary.setMutualiaRedInternaClients(metrics.getMutualiaRedInternaClients());
                summary.setMutualiaRedExternaClients(metrics.getMutualiaRedExternaClients());
                summary.setMutualiaKorporatiboaClients(metrics.getMutualiaKorporatiboaClients());
                summary.setWifiPacsClients(metrics.getWifiPacsClients());
                summary.setMutVideoClients(metrics.getMutVideoClients());
                summary.setLastUpdated(lastUpdated);
                summary.setDataStatus(dataStatus);

                return summary;
        }

        /**
         * Devuelve solo el bloque normalizado de estado de red Aruba.
         */
        public ArubaNetworkStatusDto getNetworkStatus() {

                return getSummary().getNetworkStatusDetails();
        }

        /**
         * Busca la fecha más reciente entre métricas agregadas, APs, switches e
         * históricos de uso para calcular frescura Aruba.
         */
        private LocalDateTime resolveArubaLastUpdated() {

                // Se prioriza la tabla agregada del dashboard Aruba porque resume firmware y clientes WiFi.
                // Si aun no existe, se usa la fecha mas reciente de los datos Aruba persistidos.

                LocalDateTime latest = dashboardMetricsRepository
                                .findById(METRICS_ID)
                                .map(ArubaDashboardMetrics::getUpdatedAt)
                                .orElse(null);

                latest = newer(
                                latest,
                                accessPointRepository
                                                .findTopByLastSeenAtIsNotNullOrderByLastSeenAtDesc()
                                                .map(AccessPoint::getLastSeenAt)
                                                .orElse(null));

                latest = newer(
                                latest,
                                arubaSwitchRepository
                                                .findTopByLastSeenAtIsNotNullOrderByLastSeenAtDesc()
                                                .map(ArubaSwitch::getLastSeenAt)
                                                .orElse(null));

                latest = newer(
                                latest,
                                switchClientUsageRepository
                                                .findTopByUpdatedAtIsNotNullOrderByUpdatedAtDesc()
                                                .map(ArubaSwitchClientUsage::getUpdatedAt)
                                                .orElse(null));

                latest = newer(
                                latest,
                                switchInterfaceUsageHistoryRepository
                                                .findTopByObservedAtIsNotNullOrderByObservedAtDesc()
                                                .map(ArubaSwitchInterfaceUsageHistory::getObservedAt)
                                                .orElse(null));

                return latest;
        }

        /**
         * Aruba tiene una ventana de frescura propia porque depende de APIs
         * reales y no del scheduler de datos simulados.
         */
        private String calculateDataStatus(LocalDateTime lastUpdated) {

                // Aruba usa APIs reales y su sincronizacion puede ser menos frecuente que el scheduler de
                // datos simulados. Por eso se considera fresco durante 10 minutos.

                if (lastUpdated == null) {

                        return "NO_DATA";
                }

                if (lastUpdated.isBefore(LocalDateTime.now().minusMinutes(kpiProperties.getAruba().getFreshnessMinutes()))) {

                        return "STALE";
                }

                return "OK";
        }

        private LocalDateTime newer(LocalDateTime current,LocalDateTime candidate) {

                if (candidate == null) {

                        return current;
                }

                if (current == null || candidate.isAfter(current)) {

                        return candidate;
                }

                return current;
        }
}
