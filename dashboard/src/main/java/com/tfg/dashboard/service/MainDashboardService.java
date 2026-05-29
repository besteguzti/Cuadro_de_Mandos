package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.ArubaNetworkStatusDto;
import com.tfg.dashboard.dto.KpiResultDto;
import com.tfg.dashboard.dto.summary.ArubaSummary;
import com.tfg.dashboard.model.CitrixMetricsHistory;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.dto.summary.MainDashboardSummary;
import com.tfg.dashboard.model.Microsoft365MetricsHistory;
import com.tfg.dashboard.repository.CitrixMetricsHistoryRepository;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;
import com.tfg.dashboard.repository.Microsoft365MetricsHistoryRepository;

/**
 * Orquesta la respuesta del dashboard principal.
 *
 * Combina el resumen real de Aruba con los últimos snapshots persistidos de
 * Citrix, Microsoft 365 y GLPI, calcula los KPIs transversales y aplica la
 * frescura de datos antes de construir el DTO que consume React.
 */
@Service
public class MainDashboardService {

    private static final int MONITORED_PLATFORM_COUNT = 4;

    // Aruba se obtiene del flujo real persistido y las demás plataformas de snapshots MySQL.
    // desde las últimas muestras

    private final ArubaService arubaService;

    private final CitrixMetricsHistoryRepository citrixRepository;

    private final Microsoft365MetricsHistoryRepository microsoft365Repository;

    private final GlpiMetricsHistoryRepository glpiRepository;

    private final KpiScoringService kpiScoringService;

    private final GlobalKpiCalculationService globalKpiCalculationService;

    private final DashboardFreshnessService dashboardFreshnessService;

    private final KpiProperties kpiProperties;

    public MainDashboardService(
            ArubaService arubaService,
            CitrixMetricsHistoryRepository citrixRepository,
            Microsoft365MetricsHistoryRepository microsoft365Repository,
            GlpiMetricsHistoryRepository glpiRepository,
            KpiScoringService kpiScoringService,
            GlobalKpiCalculationService globalKpiCalculationService,
            DashboardFreshnessService dashboardFreshnessService,
            KpiProperties kpiProperties
    ) {

        this.arubaService = arubaService;
        this.citrixRepository = citrixRepository;
        this.microsoft365Repository = microsoft365Repository;
        this.glpiRepository = glpiRepository;
        this.kpiScoringService = kpiScoringService;
        this.globalKpiCalculationService = globalKpiCalculationService;
        this.dashboardFreshnessService = dashboardFreshnessService;
        this.kpiProperties = kpiProperties;
    }

    /**
     * Construye el resumen agregado del dashboard.
     *
     * Aruba se consulta a través de su fachada y el resto de plataformas se
     * leen desde sus últimos snapshots. La frescura se calcula antes de
     * aplicar colores para que un dato ausente u obsoleto no se muestre como
     * saludable.
     */
    public MainDashboardSummary getSummary() {

        ArubaSummary aruba =
                arubaService.getSummary();

        Optional<CitrixMetricsHistory> citrixSnapshot =
                citrixRepository.findTopByOrderByCollectedAtDesc();

        Optional<Microsoft365MetricsHistory> microsoft365Snapshot =
                microsoft365Repository.findTopByOrderByCollectedAtDesc();

        Optional<GlpiMetricsHistory> glpiSnapshot =
                glpiRepository.findTopByOrderByCollectedAtDesc();

        CitrixMetricsHistory citrix =
                citrixSnapshot.orElseGet(CitrixMetricsHistory::new);

        Microsoft365MetricsHistory microsoft365 =
                microsoft365Snapshot.orElseGet(
                        Microsoft365MetricsHistory::new
                );

        GlpiMetricsHistory glpi =
                glpiSnapshot.orElseGet(GlpiMetricsHistory::new);

        String citrixDataStatus =
                dashboardFreshnessService.calculateDataStatus(citrixSnapshot);

        String arubaDataStatus =
                dashboardFreshnessService.normalizeDataStatus(
                        aruba.getDataStatus()
                );

        String microsoft365DataStatus =
                dashboardFreshnessService.calculateDataStatus(
                        microsoft365Snapshot
                );

        String glpiDataStatus =
                dashboardFreshnessService.calculateDataStatus(glpiSnapshot);

        String dataStatus =
                dashboardFreshnessService.calculateGlobalDataStatus(
                        arubaDataStatus,
                        citrixDataStatus,
                        microsoft365DataStatus,
                        glpiDataStatus
                );

        MainDashboardSummary summary =
                new MainDashboardSummary();

        int arubaHealthIndex =
                globalKpiCalculationService.calculateArubaNetworkAffection(
                        aruba
                );

        int citrixHealthIndex =
                globalKpiCalculationService.calculateCitrixHealthAffection(
                        citrix
                );

        int microsoft365HealthIndex =
                globalKpiCalculationService
                        .calculateMicrosoft365HealthAffection(microsoft365);

        int glpiHealthIndex =
                globalKpiCalculationService.calculateGlpiHealthAffection(
                        glpi
                );

        KpiProperties.PlatformWeights globalStatusWeights =
                kpiProperties.getWeights().getGlobalStatus();

        int globalHealthPercentage =
                globalKpiCalculationService.weightedAverage(
                        arubaHealthIndex,
                        kpiProperties.asWeightPercent(
                                globalStatusWeights.getAruba()
                        ),
                        citrixHealthIndex,
                        kpiProperties.asWeightPercent(
                                globalStatusWeights.getCitrix()
                        ),
                        microsoft365HealthIndex,
                        kpiProperties.asWeightPercent(
                                globalStatusWeights.getMicrosoft365()
                        ),
                        glpiHealthIndex,
                        kpiProperties.asWeightPercent(
                                globalStatusWeights.getGlpi()
                        )
                );

        int globalCriticality =
                globalKpiCalculationService.calculateGlobalCriticality(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int globalAvailability =
                globalKpiCalculationService.calculateGlobalAvailability(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int operationalPressure =
                globalKpiCalculationService.calculateOperationalPressure(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int technicalDegradation =
                globalKpiCalculationService.calculateTechnicalDegradation(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int slaRisk =
                globalKpiCalculationService.calculateSlaRisk(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int operationalBacklog =
                globalKpiCalculationService.calculateOperationalBacklog(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int userImpact =
                globalKpiCalculationService.calculateUserImpact(
                        aruba,
                        citrix,
                        microsoft365,
                        glpi
                );

        int affectedServicesPercent =
                globalKpiCalculationService.calculateAffectedServicesPercent(
                        arubaHealthIndex,
                        citrixHealthIndex,
                        microsoft365HealthIndex,
                        glpiHealthIndex
                );

        summary.setGlobalHealth(
                dashboardFreshnessService.applyFreshnessToColor(
                        kpiScoringService.statusFromAffection(
                                globalHealthPercentage
                        ),
                        dataStatus
                )
        );
        summary.setGlobalHealthPercentage(globalHealthPercentage);
        summary.setGlobalHealthStatus(summary.getGlobalHealth());
        summary.setGlobalCriticality(globalCriticality);
        summary.setGlobalCriticalityStatus(
                kpiScoringService.statusFromAffection(globalCriticality)
        );
        summary.setGlobalAvailability(globalAvailability);
        summary.setGlobalAvailabilityStatus(
                kpiScoringService.statusFromAffection(globalAvailability)
        );
        summary.setUserImpact(userImpact);
        summary.setUserImpactStatus(
                kpiScoringService.statusFromAffection(userImpact)
        );
        summary.setAffectedServicesPercent(affectedServicesPercent);
        summary.setAffectedServicesStatus(
                kpiScoringService.statusFromAffection(affectedServicesPercent)
        );
        summary.setTechnicalDegradation(technicalDegradation);
        summary.setTechnicalDegradationStatus(
                kpiScoringService.statusFromAffection(technicalDegradation)
        );
        summary.setOperationalPressure(operationalPressure);
        summary.setOperationalPressureStatus(
                kpiScoringService.statusFromAffection(operationalPressure)
        );
        summary.setOperationalBacklog(operationalBacklog);
        summary.setOperationalBacklogStatus(
                kpiScoringService.statusFromAffection(operationalBacklog)
        );
        summary.setSlaRisk(slaRisk);
        summary.setSlaRiskStatus(
                kpiScoringService.statusFromAffection(slaRisk)
        );

        summary.setCriticalOpenTickets(
                glpi.getCriticalOpenTickets()
        );
        summary.setLastUpdated(
                dashboardFreshnessService.latestCollectedAt(
                        aruba.getLastUpdated(),
                        citrixSnapshot,
                        microsoft365Snapshot,
                        glpiSnapshot
                )
        );
        summary.setDataStatus(dataStatus);
        summary.setArubaDataStatus(arubaDataStatus);
        summary.setCitrixDataStatus(citrixDataStatus);
        summary.setMicrosoft365DataStatus(microsoft365DataStatus);
        summary.setGlpiDataStatus(glpiDataStatus);
        summary.setKpis(
                buildMainKpis(
                        summary,
                        arubaHealthIndex,
                        citrixHealthIndex,
                        microsoft365HealthIndex,
                        glpiHealthIndex
                )
        );

        return summary;
    }

    /**
     * Crea los KPIs normalizados que explican los valores transversales y sus
     * componentes por plataforma.
     */
    private List<KpiResultDto> buildMainKpis(
            MainDashboardSummary summary,
            int arubaHealthIndex,
            int citrixHealthIndex,
            int microsoft365HealthIndex,
            int glpiHealthIndex
    ) {

        LocalDateTime timestamp =
                summary.getLastUpdated();

        String freshness =
                summary.getDataStatus();

        List<KpiResultDto> platformComponents =
                List.of(
                        kpiScoringService.component(
                                "aruba_network_affectation",
                                "Aruba estado de red",
                                arubaHealthIndex,
                                kpiScoringService.statusFromAffection(
                                        arubaHealthIndex
                                ),
                                arubaHealthIndex
                        ),
                        kpiScoringService.component(
                                "citrix_health",
                                "Indice de salud Citrix",
                                citrixHealthIndex,
                                kpiScoringService.statusFromAffection(
                                        citrixHealthIndex
                                ),
                                citrixHealthIndex
                        ),
                        kpiScoringService.component(
                                "microsoft365_health",
                                "Indice de salud Microsoft 365",
                                microsoft365HealthIndex,
                                kpiScoringService.statusFromAffection(
                                        microsoft365HealthIndex
                                ),
                                microsoft365HealthIndex
                        ),
                        kpiScoringService.component(
                                "glpi_health",
                                "Indice de salud GLPI",
                                glpiHealthIndex,
                                kpiScoringService.statusFromAffection(
                                        glpiHealthIndex
                                ),
                                glpiHealthIndex
                        )
                );

        return List.of(
                kpiScoringService.kpi(
                        "global_status",
                        "Estado global",
                        summary.getGlobalHealthPercentage(),
                        summary.getGlobalHealthStatus(),
                        "Estado global ponderado de la infraestructura monitorizada.",
                        formula(kpiProperties.getWeights().getGlobalStatus()),
                        timestamp,
                        freshness,
                        platformComponents
                ),
                kpiScoringService.kpi(
                        "global_criticality",
                        "Criticidad global",
                        summary.getGlobalCriticality(),
                        summary.getGlobalCriticalityStatus(),
                        "Media de indicadores criticos normalizados.",
                        "Promedio de senales rojas/amarillas/verdes de Aruba, Citrix, Microsoft 365 y GLPI.",
                        timestamp,
                        freshness,
                        platformComponents
                ),
                kpiScoringService.kpi(
                        "global_availability",
                        "Disponibilidad global",
                        summary.getGlobalAvailability(),
                        summary.getGlobalAvailabilityStatus(),
                        "Afeccion sobre la disponibilidad de los servicios principales.",
                        formula(kpiProperties.getWeights().getAvailability()),
                        timestamp,
                        freshness,
                        platformComponents
                ),
                kpiScoringService.kpi(
                        "operational_pressure",
                        "Presion operativa",
                        summary.getOperationalPressure(),
                        summary.getOperationalPressureStatus(),
                        "Carga de trabajo tecnica y operativa acumulada.",
                        formula(
                                "GLPI",
                                kpiProperties.getWeights().getOperationalPressure().getGlpi(),
                                "Citrix",
                                kpiProperties.getWeights().getOperationalPressure().getCitrix(),
                                "Microsoft365",
                                kpiProperties.getWeights().getOperationalPressure().getMicrosoft365(),
                                "Aruba",
                                kpiProperties.getWeights().getOperationalPressure().getAruba()
                        ),
                        timestamp,
                        freshness,
                        platformComponents
                ),
                kpiScoringService.kpi(
                        "technical_degradation",
                        "Degradacion tecnica",
                        summary.getTechnicalDegradation(),
                        summary.getTechnicalDegradationStatus(),
                        "Deterioro tecnico aunque no exista caida total.",
                        formula(kpiProperties.getWeights().getTechnicalDegradation()),
                        timestamp,
                        freshness,
                        platformComponents
                ),
                kpiScoringService.kpi(
                        "sla_risk",
                        "Riesgo SLA",
                        summary.getSlaRisk(),
                        summary.getSlaRiskStatus(),
                        "Riesgo de incumplir niveles de servicio.",
                        formula(
                                "Citrix",
                                kpiProperties.getWeights().getSlaRisk().getCitrix(),
                                "Aruba",
                                kpiProperties.getWeights().getSlaRisk().getAruba(),
                                "GLPI",
                                kpiProperties.getWeights().getSlaRisk().getGlpi(),
                                "Microsoft365",
                                kpiProperties.getWeights().getSlaRisk().getMicrosoft365()
                        ),
                        timestamp,
                        freshness,
                        platformComponents
                ),
                kpiScoringService.kpi(
                        "operational_backlog",
                        "Backlog operativo",
                        summary.getOperationalBacklog(),
                        summary.getOperationalBacklogStatus(),
                        "Trabajo pendiente acumulado.",
                        formula(
                                "GLPI",
                                kpiProperties.getWeights().getOperationalBacklog().getGlpi(),
                                "Microsoft365",
                                kpiProperties.getWeights().getOperationalBacklog().getMicrosoft365(),
                                "Aruba",
                                kpiProperties.getWeights().getOperationalBacklog().getAruba(),
                                "Citrix",
                                kpiProperties.getWeights().getOperationalBacklog().getCitrix()
                        ),
                        timestamp,
                        freshness,
                        platformComponents
                ),
                kpiScoringService.kpi(
                        "user_impact",
                        "Impacto en usuarios",
                        summary.getUserImpact(),
                        summary.getUserImpactStatus(),
                        "Afeccion que pueden percibir los usuarios.",
                        formula(
                                "Citrix",
                                kpiProperties.getWeights().getUserImpact().getCitrix(),
                                "Aruba",
                                kpiProperties.getWeights().getUserImpact().getAruba(),
                                "Microsoft365",
                                kpiProperties.getWeights().getUserImpact().getMicrosoft365(),
                                "GLPI",
                                kpiProperties.getWeights().getUserImpact().getGlpi()
                        ),
                        timestamp,
                        freshness,
                        platformComponents
                ),
                kpiScoringService.kpi(
                        "affected_services",
                        "Servicios afectados",
                        summary.getAffectedServicesPercent(),
                        summary.getAffectedServicesStatus(),
                        "Porcentaje de plataformas afectadas.",
                        "Cada plataforma en amarillo o rojo suma "
                                + affectedServiceContributionPercent()
                                + "%.",
                        timestamp,
                        freshness,
                        platformComponents
                )
        );
    }

    private String formula(KpiProperties.PlatformWeights weights) {

        return formula(
                "Aruba",
                weights.getAruba(),
                "Citrix",
                weights.getCitrix(),
                "Microsoft365",
                weights.getMicrosoft365(),
                "GLPI",
                weights.getGlpi()
        );
    }

    private String formula(
            String firstName,
            double firstWeight,
            String secondName,
            double secondWeight,
            String thirdName,
            double thirdWeight,
            String fourthName,
            double fourthWeight
    ) {

        return formulaTerm(firstName, firstWeight)
                + " + "
                + formulaTerm(secondName, secondWeight)
                + " + "
                + formulaTerm(thirdName, thirdWeight)
                + " + "
                + formulaTerm(fourthName, fourthWeight);
    }

    private String formulaTerm(String platform,double weight) {

        return platform + "*" + kpiProperties.formatWeight(weight);
    }

    private int affectedServiceContributionPercent() {

        return kpiProperties.getStatus().getMax() / MONITORED_PLATFORM_COUNT;
    }

}
