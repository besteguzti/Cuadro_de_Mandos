package com.tfg.dashboard.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.ExecutiveSummaryDto;
import com.tfg.dashboard.dto.KpiResultDto;
import com.tfg.dashboard.model.AnalysisSnapshot;
import com.tfg.dashboard.dto.summary.ArubaSummary;
import com.tfg.dashboard.model.CitrixMetricsHistory;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.dto.summary.MainDashboardSummary;
import com.tfg.dashboard.model.Microsoft365MetricsHistory;
import com.tfg.dashboard.repository.AnalysisSnapshotRepository;
import com.tfg.dashboard.repository.CitrixMetricsHistoryRepository;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;
import com.tfg.dashboard.repository.Microsoft365MetricsHistoryRepository;

/**
 * Construye el diagnóstico operativo del dashboard principal.
 *
 * Resume qué servicio está afectado, qué plataforma contribuye más al estado
 * global, qué prioridad tiene la situación y qué primera acción revisar. Es una
 * lectura heurística para orientar la operación, no un motor de causa raíz.
 */
@Service
public class ExecutiveSummaryService {

        private static final String NOT_ESTIMABLE = "No estimable con los datos actuales";
        private final MainDashboardService mainDashboardService;
        private final ArubaService arubaService;
        private final CitrixMetricsHistoryRepository citrixRepository;
        private final Microsoft365MetricsHistoryRepository microsoft365Repository;
        private final GlpiMetricsHistoryRepository glpiRepository;
        private final AnalysisSnapshotRepository analysisSnapshotRepository;
        private final KpiProperties kpiProperties;

        public ExecutiveSummaryService(
                        MainDashboardService mainDashboardService,
                        ArubaService arubaService,
                        CitrixMetricsHistoryRepository citrixRepository,
                        Microsoft365MetricsHistoryRepository microsoft365Repository,
                        GlpiMetricsHistoryRepository glpiRepository,
                        AnalysisSnapshotRepository analysisSnapshotRepository,
                        KpiProperties kpiProperties) {

                this.mainDashboardService = mainDashboardService;
                this.arubaService = arubaService;
                this.citrixRepository = citrixRepository;
                this.microsoft365Repository = microsoft365Repository;
                this.glpiRepository = glpiRepository;
                this.analysisSnapshotRepository = analysisSnapshotRepository;
                this.kpiProperties = kpiProperties;
        }

        /**
         * Genera la tarjeta ejecutiva que se muestra encima de los KPIs del
         * dashboard principal.
         */
        public ExecutiveSummaryDto getExecutiveSummary() {

                MainDashboardSummary dashboardSummary = mainDashboardService.getSummary();
                return buildOperationalSummary(
                                dashboardSummary,
                                resolveTrend(dashboardSummary.getGlobalHealthPercentage()),
                                null,
                                true);
        }

        /**
         * Genera un resumen ejecutivo para el Banco de pruebas.
         *
         * Usa los mismos criterios de plataforma afectada, impacto y prioridad
         * que el dashboard real, pero no consulta históricos ni repositorios:
         * un escenario manual no tiene tendencia temporal fiable ni usuarios
         * reales estimables.
         */
        public ExecutiveSummaryDto buildScenarioSummary(MainDashboardSummary dashboardSummary) {

                return buildOperationalSummary(
                                dashboardSummary,
                                "Tendencia no disponible en escenario manual",
                                "No estimable en escenario manual",
                                false);
        }

        private ExecutiveSummaryDto buildOperationalSummary(
                        MainDashboardSummary dashboardSummary,
                        String trend,
                        String estimatedUsersOverride,
                        boolean useRealObservedData) {

                Map<String, Integer> platformAffectations = extractPlatformAffectations(dashboardSummary);
                List<String> affectedServices = resolveAffectedServices(platformAffectations);
                List<String> criticalPlatforms = resolveCriticalPlatforms(platformAffectations);
                String mainAffectedPlatform = resolveMainAffectedPlatform(platformAffectations);
                String probableOrigin = resolveProbableOrigin(mainAffectedPlatform);
                String impactLevel = resolveImpactLevel(dashboardSummary.getUserImpact(), criticalPlatforms.size());
                String priority = resolvePriority(dashboardSummary, affectedServices.size(), criticalPlatforms.size());
                String estimatedAffectedUsers = useRealObservedData
                                ? estimateAffectedUsers(mainAffectedPlatform)
                                : estimatedUsersOverride;
                ExecutiveSummaryDto summary = new ExecutiveSummaryDto();

                summary.setGlobalStatus(dashboardSummary.getGlobalHealth());
                summary.setAffectedServices(affectedServices);
                summary.setMainAffectedPlatform(mainAffectedPlatform);
                summary.setProbableOrigin(probableOrigin);
                summary.setImpactLevel(impactLevel);
                summary.setEstimatedAffectedUsers(estimatedAffectedUsers);
                summary.setPriority(priority);
                summary.setFirstAction(firstAction(mainAffectedPlatform));
                summary.setTrend(trend);
                summary.setSummaryText(
                                useRealObservedData
                                                ? buildSummaryText(
                                                                dashboardSummary,
                                                                mainAffectedPlatform,
                                                                affectedServices,
                                                                criticalPlatforms,
                                                                priority,
                                                                trend)
                                                : buildScenarioSummaryText(
                                                                dashboardSummary,
                                                                mainAffectedPlatform,
                                                                affectedServices,
                                                                criticalPlatforms,
                                                                priority));

                return summary;
        }

        private Map<String, Integer> extractPlatformAffectations(MainDashboardSummary summary) {

                Map<String, Integer> values = new HashMap<>();

                values.put("Aruba", 0);
                values.put("Citrix", 0);
                values.put("Microsoft 365", 0);
                values.put("GLPI", 0);

                if (summary.getKpis() == null) {

                        return values;
                }

                for (KpiResultDto kpi : summary.getKpis()) {

                        if (kpi.getComponents() == null) {

                                continue;
                        }

                        for (KpiResultDto component : kpi.getComponents()) {

                                if ("aruba_network_affectation".equals(component.getId())) {

                                        values.put("Aruba", numericValue(component));
                                }

                                if ("citrix_health".equals(component.getId())) {

                                        values.put("Citrix", numericValue(component));
                                }

                                if ("microsoft365_health".equals(component.getId())) {

                                        values.put("Microsoft 365", numericValue(component));
                                }

                                if ("glpi_health".equals(component.getId())) {

                                        values.put("GLPI", numericValue(component));
                                }
                        }
                }

                return values;
        }

        private int numericValue(KpiResultDto component) {

                if (component.getScore() != null) {

                        return component.getScore();
                }

                if (component.getValue() instanceof Number number) {

                        return number.intValue();
                }

                return 0;
        }

        private List<String> resolveAffectedServices(Map<String, Integer> platformAffectations) {

                List<String> services = new ArrayList<>();

                if (platformAffectations.getOrDefault("Aruba", 0) >= kpiProperties.getStatus().getYellowMin()) {

                        services.add("Red corporativa / conectividad");
                }

                if (platformAffectations.getOrDefault("Citrix", 0) >= kpiProperties.getStatus().getYellowMin()) {

                        services.add("Acceso a aplicaciones corporativas");
                }

                if (platformAffectations.getOrDefault("Microsoft 365", 0) >= kpiProperties.getStatus().getYellowMin()) {

                        services.add("Servicios cloud / identidad / colaboracion");
                }

                if (platformAffectations.getOrDefault("GLPI", 0) >= kpiProperties.getStatus().getYellowMin()) {

                        services.add("Soporte IT / gestion de incidencias");
                }

                return services;
        }

        private List<String> resolveCriticalPlatforms(Map<String, Integer> platformAffectations) {

                return platformAffectations.entrySet().stream()
                                .filter(entry -> entry.getValue() >= kpiProperties.getStatus().getRedMin())
                                .map(Map.Entry::getKey)
                                .sorted()
                                .toList();
        }

        private String resolveMainAffectedPlatform(Map<String, Integer> platformAffectations) {

                Map<String, Double> weightedContributions = Map.of(
                                "Aruba",
                                platformAffectations.getOrDefault("Aruba", 0)
                                                * kpiProperties.getWeights().getGlobalStatus().getAruba(),
                                "Citrix",
                                platformAffectations.getOrDefault("Citrix", 0)
                                                * kpiProperties.getWeights().getGlobalStatus().getCitrix(),
                                "Microsoft 365",
                                platformAffectations.getOrDefault("Microsoft 365", 0)
                                                * kpiProperties.getWeights().getGlobalStatus().getMicrosoft365(),
                                "GLPI",
                                platformAffectations.getOrDefault("GLPI", 0)
                                                * kpiProperties.getWeights().getGlobalStatus().getGlpi());

                return weightedContributions.entrySet().stream()
                                .max(Comparator.comparingDouble(Map.Entry::getValue))
                                .filter(entry -> entry.getValue() > 0)
                                .map(Map.Entry::getKey)
                                .orElse("Sin plataforma afectada");
        }

        private String resolveProbableOrigin(String mainAffectedPlatform) {

                if ("Sin plataforma afectada".equals(mainAffectedPlatform)) {

                        return "No se detecta origen probable con los datos actuales";
                }

                return mainAffectedPlatform;
        }

        private String levelFromAffectation(int value) {

                if (value >= kpiProperties.getStatus().getRedMin()) {

                        return "HIGH";
                }

                if (value >= kpiProperties.getStatus().getYellowMin()) {

                        return "MODERATE";
                }

                return "LOW";
        }

        private String resolveImpactLevel(int userImpact, int criticalPlatformCount) {

                if (criticalPlatformCount >= 2) {

                        return "HIGH";
                }

                return levelFromAffectation(userImpact);
        }

        private String resolvePriority(
                        MainDashboardSummary summary,
                        int affectedServicesCount,
                        int criticalPlatformCount) {

                if (criticalPlatformCount >= 2) {

                        return "HIGH";
                }

                if (summary.getUserImpact() >= kpiProperties.getStatus().getRedMin()
                                || summary.getSlaRisk() >= kpiProperties.getStatus().getRedMin()
                                || summary.getGlobalCriticality() >= kpiProperties.getStatus().getRedMin()) {

                        return "HIGH";
                }

                if (criticalPlatformCount >= 1) {

                        return "MEDIUM";
                }

                if (summary.getGlobalHealthPercentage() >= kpiProperties.getStatus().getYellowMin()
                                || affectedServicesCount >= 2) {

                        return "MEDIUM";
                }

                return "LOW";
        }

        /**
         * Compara el estado global actual con los últimos snapshots de análisis
         * para clasificar la tendencia como WORSENING, IMPROVING o STABLE.
         */
        private String resolveTrend(int currentGlobalStatus) {

                List<AnalysisSnapshot> snapshots = analysisSnapshotRepository.findTop5ByOrderByTimestampDesc();

                List<Integer> previousValues = snapshots.stream().map(AnalysisSnapshot::getGlobalStatus).filter(value -> value != null).toList();

                if (previousValues.isEmpty()) {

                        return "STABLE";
                }

                double average = previousValues.stream()
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(currentGlobalStatus);

                double difference = currentGlobalStatus - average;

                if (difference >= kpiProperties.getExecutive().getTrendDifferenceThreshold()) {

                        return "WORSENING";
                }

                if (difference <= -kpiProperties.getExecutive().getTrendDifferenceThreshold()) {

                        return "IMPROVING";
                }

                return "STABLE";
        }

        /**
         * Estima usuarios o elementos potencialmente afectados solo con datos
         * observables. Si no hay señal suficiente, no inventa cifras.
         */
        private String estimateAffectedUsers(String mainAffectedPlatform) {

                if ("Aruba".equals(mainAffectedPlatform)) {

                        ArubaSummary aruba = arubaService.getSummary();

                        if (aruba.getTotalWifiClients() > 0) {

                                return aruba.getTotalWifiClients() + " clientes WiFi observados";
                        }
                }

                if ("Citrix".equals(mainAffectedPlatform)) {

                        Optional<CitrixMetricsHistory> citrix = citrixRepository.findTopByOrderByCollectedAtDesc();

                        if (citrix.isPresent() && citrix.get().getActiveSessions() > 0) {

                                return citrix.get().getActiveSessions() + " sesiones activas observadas";
                        }

                        if (citrix.isPresent() && citrix.get().getFailedLogons() > 0) {

                                return citrix.get().getFailedLogons() + " errores de inicio observados";
                        }
                }

                if ("Microsoft 365".equals(mainAffectedPlatform)) {

                        Optional<Microsoft365MetricsHistory> microsoft365 = microsoft365Repository.findTopByOrderByCollectedAtDesc();

                        if (microsoft365.isPresent() && microsoft365.get().getActiveUsers() > 0) {

                                return microsoft365.get().getActiveUsers() + " usuarios activos observados";
                        }

                        int affectedDevices = microsoft365.map(this::microsoftAffectedDevices).orElse(0);

                        if (affectedDevices > 0) {

                                return affectedDevices + " dispositivos con senales de afección";
                        }
                }

                if ("GLPI".equals(mainAffectedPlatform)) {

                        Optional<GlpiMetricsHistory> glpi = glpiRepository.findTopByOrderByCollectedAtDesc();

                        if (glpi.isPresent() && glpi.get().getCriticalOpenTickets() > 0) {

                                return glpi.get().getCriticalOpenTickets() + " tickets críticos como senal indirecta";
                        }

                        if (glpi.isPresent() && glpi.get().getOpenTickets() > 0) {

                                return glpi.get().getOpenTickets() + " tickets abiertos como senal indirecta";
                        }
                }

                return NOT_ESTIMABLE;
        }

        private int microsoftAffectedDevices(Microsoft365MetricsHistory microsoft365) {

                return microsoft365.getNonCompliantDevices()
                                + microsoft365.getOutdatedWindowsDevices()
                                + microsoft365.getDevicesWithoutEncryption()
                                + microsoft365.getUsersWithoutMfa();
        }

        private String firstAction(String mainAffectedPlatform) {

                return switch (mainAffectedPlatform) {
                        case "Aruba" -> "Revisar APs inactivos, switches caidos, clientes WiFi y firmware pendiente";
                        case "Citrix" -> "Revisar Delivery Controllers, sesiones activas, logon duration y errores de inicio";
                        case "Microsoft 365" -> "Revisar SharePoint, usuarios sin MFA, dispositivos no conformes, secretos proximos a caducar y cifrado";
                        case "GLPI" -> "Revisar tickets críticos, tickets abiertos y tasa de cierre";
                        default -> "Mantener seguimiento de KPIs y frescura de datos";
                };
        }

        /**
         * Compone una explicación breve del diagnóstico operativo.
         */
        private String buildSummaryText(
                        MainDashboardSummary summary,
                        String mainAffectedPlatform,
                        List<String> affectedServices,
                        List<String> criticalPlatforms,
                        String priority,
                        String trend) {

                String serviceText = affectedServices.isEmpty()
                                ? "no se detectan servicios afectados"
                                : "servicios afectados: " + String.join(", ", affectedServices);

                if ("Sin plataforma afectada".equals(mainAffectedPlatform)) {

                        return "El estado global se encuentra en "
                                        + statusText(summary.getGlobalHealth())
                                        + " y " + serviceText
                                        + ". La prioridad operativa es " + priority
                                        + " y la tendencia es " + trend + ".";
                }

                String criticalText = criticalPlatformText(summary, criticalPlatforms);

                return "El estado global se encuentra en "
                                + statusText(summary.getGlobalHealth())
                                + criticalText
                                + ". La principal contribucion procede de "
                                + mainAffectedPlatform
                                + ", con " + serviceText
                                + ". La prioridad operativa es " + priority
                                + " y la tendencia es " + trend
                                + ". Se recomienda revisar primero: "
                                + firstAction(mainAffectedPlatform)
                                + ".";
        }

        private String buildScenarioSummaryText(
                        MainDashboardSummary summary,
                        String mainAffectedPlatform,
                        List<String> affectedServices,
                        List<String> criticalPlatforms,
                        String priority) {

                String serviceText = affectedServices.isEmpty()
                                ? "no se detectan servicios afectados"
                                : "servicios afectados: " + String.join(", ", affectedServices);

                if ("Sin plataforma afectada".equals(mainAffectedPlatform)) {

                        return "Este escenario manual muestra un estado global "
                                        + statusText(summary.getGlobalHealth())
                                        + "; " + serviceText
                                        + ". La prioridad operativa es " + priority
                                        + ". No se calcula tendencia porque no hay histórico asociado al banco de pruebas.";
                }

                String criticalText = criticalPlatformText(summary, criticalPlatforms);

                return "Este escenario manual sugiere revisar primero "
                                + mainAffectedPlatform
                                + ". El estado global aparece en "
                                + statusText(summary.getGlobalHealth())
                                + criticalText
                                + ", con " + serviceText
                                + ". La prioridad operativa es " + priority
                                + ". La lectura orienta la revision, pero no demuestra causalidad ni modifica los datos reales.";
        }

        private String criticalPlatformText(MainDashboardSummary summary, List<String> criticalPlatforms) {

                if (criticalPlatforms.isEmpty()) {

                        return "";
                }

                String platforms = String.join(", ", criticalPlatforms);

                if (criticalPlatforms.size() >= 2) {

                        return ", aunque varias plataformas presentan estado critico: " + platforms;
                }

                if ("GREEN".equalsIgnoreCase(summary.getGlobalHealth())) {

                        return ", aunque " + platforms + " presenta estado critico";
                }

                return ", con estado critico en " + platforms;
        }

        private String statusText(String status) {

                if ("GREEN".equalsIgnoreCase(status)) {

                        return "verde";
                }

                if ("YELLOW".equalsIgnoreCase(status)) {

                        return "amarillo";
                }

                if ("RED".equalsIgnoreCase(status)) {

                        return "rojo";
                }

                return status == null ? "desconocido" : status.toLowerCase();
        }
}
