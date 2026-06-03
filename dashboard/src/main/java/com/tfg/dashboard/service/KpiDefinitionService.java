package com.tfg.dashboard.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.KpiDefinitionDto;
import com.tfg.dashboard.dto.KpiThresholdDto;

/**
 * Proporciona definiciones documentales de los KPIs expuestos por el backend.
 *
 * No calcula valores en tiempo real: explica fórmulas, fuentes y umbrales para
 * el endpoint /api/kpis/definitions, alineando los textos con KpiProperties
 * cuando las reglas están configuradas.
 */
@Service
public class KpiDefinitionService {

    private final KpiProperties kpiProperties;

    public KpiDefinitionService(KpiProperties kpiProperties) {

        this.kpiProperties = kpiProperties;
    }

    /**
     * Devuelve el catálogo de definiciones de KPIs de plataforma, dashboard y
     * análisis.
     */
    public List<KpiDefinitionDto> getDefinitions() {

        return List.of(
                definition(
                        "aruba_network_status",
                        "Aruba estado de red",
                        "PLATFORM",
                        "ARUBA",
                        "Mide la afección de red Aruba combinando Access Points, switches y clientes WiFi.",
                        arubaNetworkStatusFormula(),
                        commonAffectionThresholds(),
                        List.of(
                                "Access Points totales, caidos, inactivos y firmware pendiente",
                                "Clientes WiFi totales, MUTUALIA-APs y MUTUALIA-WIFI",
                                "Switches totales, caidos y firmware pendiente"
                        )
                ),
                definition(
                        "citrix_health",
                        "Citrix indice de salud Citrix",
                        "PLATFORM",
                        "CITRIX",
                        "Mide la afección del entorno Citrix a partir de indicadores internos normalizados.",
                        citrixHealthFormula(),
                        commonAffectionThresholds(),
                        List.of(
                                "Sesiones activas",
                                "Delivery Controllers disponibles",
                                "Average Logon Duration",
                                "Carga de servidores",
                                "Errores de inicio"
                        )
                ),
                definition(
                        "microsoft365_health",
                        "Microsoft 365 indice de salud Microsoft 365",
                        "PLATFORM",
                        "MICROSOFT365",
                        "Mide la afección de Microsoft 365 a partir de capacidad, identidad, seguridad y dispositivos.",
                        microsoft365HealthFormula(),
                        commonAffectionThresholds(),
                        List.of(
                                "Almacenamiento SharePoint",
                                "Usuarios sin MFA",
                                "Secretos proximos a caducar",
                                "Equipos no conformes",
                                "Windows desactualizados",
                                "Equipos sin cifrado"
                        )
                ),
                definition(
                        "glpi_health",
                        "GLPI indice de salud GLPI",
                        "PLATFORM",
                        "GLPI",
                        "Mide la afección operativa de GLPI a partir de volumen y capacidad de cierre de tickets.",
                        glpiHealthFormula(),
                        commonAffectionThresholds(),
                        List.of(
                                "Tickets abiertos",
                                "Tickets críticos abiertos",
                                "Porcentaje de tickets cerrados",
                                "Porcentaje de tickets cerrados semana"
                        )
                ),
                definition(
                        "global_status",
                        "Estado global",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide la afección global ponderada de la infraestructura monitorizada.",
                        platformWeightFormula(
                                "Aruba estado de red",
                                "Citrix indice de salud",
                                "Microsoft 365 indice de salud",
                                "GLPI indice de salud",
                                kpiProperties.getWeights().getGlobalStatus()),
                        transversalRiskThresholds("transversal.globalStatus"),
                        platformHealthSources()
                ),
                definition(
                        "global_criticality",
                        "Criticidad global",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide la presencia de condiciones críticas en rojo dentro de las plataformas.",
                        "Media de indicadores críticos normalizados: correcto 0, advertencia 50 y critico 100.",
                        transversalRiskThresholds("transversal.globalCriticality"),
                        List.of(
                                "Condiciones críticas Aruba",
                                "Condiciones críticas Citrix",
                                "Condiciones críticas Microsoft 365",
                                "Condiciones críticas GLPI"
                        )
                ),
                definition(
                        "global_availability",
                        "Disponibilidad global",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide la disponibilidad estimada de los servicios principales.",
                        platformWeightFormula(
                                "Aruba disponibilidad",
                                "Citrix disponibilidad",
                                "Microsoft 365 disponibilidad",
                                "GLPI disponibilidad",
                                kpiProperties.getWeights().getAvailability()),
                        transversalHealthThresholds("transversal.globalAvailability"),
                        List.of(
                                "Disponibilidad APs y switches Aruba",
                                "Sesiones activas y Delivery Controllers Citrix",
                                "SharePoint y secretos Microsoft 365",
                                "GLPI como soporte operativo"
                        )
                ),
                definition(
                        "operational_pressure",
                        "Presión operativa",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide la carga de trabajo técnica y operativa acumulada.",
                        platformWeightFormula(
                                "Aruba",
                                "Citrix",
                                "Microsoft 365",
                                "GLPI",
                                kpiProperties.getWeights().getOperationalPressure()),
                        transversalRiskThresholds("transversal.operationalPressure"),
                        List.of(
                                "Tickets GLPI",
                                "Errores y carga Citrix",
                                "Dispositivos Microsoft 365",
                                "APs inactivos y firmware Aruba"
                        )
                ),
                definition(
                        "technical_degradation",
                        "Degradación técnica",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide deterioro tecnico aunque no exista una caida total.",
                        platformWeightFormula(
                                "Aruba",
                                "Citrix",
                                "Microsoft 365",
                                "GLPI",
                                kpiProperties.getWeights().getTechnicalDegradation()),
                        transversalRiskThresholds("transversal.technicalDegradation"),
                        List.of(
                                "Firmware e inactividad Aruba",
                                "Logon, carga y errores Citrix",
                                "SharePoint, secretos y dispositivos Microsoft 365",
                                "Tickets críticos GLPI"
                        )
                ),
                definition(
                        "sla_risk",
                        "Riesgo SLA",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide el riesgo de incumplir niveles de servicio.",
                        platformWeightFormula(
                                "Aruba",
                                "Citrix",
                                "Microsoft 365",
                                "GLPI",
                                kpiProperties.getWeights().getSlaRisk()),
                        transversalRiskThresholds("transversal.slaRisk"),
                        List.of(
                                "Logon, sesiones, Delivery Controllers y errores Citrix",
                                "Estado de red Aruba",
                                "Tickets críticos y cierre GLPI",
                                "SharePoint, secretos y equipos Microsoft 365"
                        )
                ),
                definition(
                        "operational_backlog",
                        "Backlog operativo",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide el trabajo pendiente acumulado.",
                        platformWeightFormula(
                                "Aruba",
                                "Citrix",
                                "Microsoft 365",
                                "GLPI",
                                kpiProperties.getWeights().getOperationalBacklog()),
                        transversalRiskThresholds("transversal.operationalBacklog"),
                        List.of(
                                "Tickets GLPI",
                                "Equipos Microsoft 365",
                                "Firmware Aruba",
                                "Errores Citrix"
                        )
                ),
                definition(
                        "user_impact",
                        "Impacto en usuarios",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide la afección que pueden percibir los usuarios.",
                        platformWeightFormula(
                                "Aruba",
                                "Citrix",
                                "Microsoft 365",
                                "GLPI",
                                kpiProperties.getWeights().getUserImpact()),
                        transversalRiskThresholds("transversal.userImpact"),
                        List.of(
                                "Clientes WiFi, APs y switches Aruba",
                                "Sesiones, logon, errores y Delivery Controllers Citrix",
                                "SharePoint, MFA y dispositivos Microsoft 365",
                                "Tickets abiertos y críticos GLPI"
                        )
                ),
                definition(
                        "affected_services",
                        "Servicios afectados",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide cuantas plataformas están afectadas.",
                        affectedServicesFormula(),
                        transversalRiskThresholds("transversal.affectedServices"),
                        platformHealthSources()
                ),
                definition(
                        "glpi_operational_pressure",
                        "Presión operativa GLPI",
                        "ANALYSIS",
                        "GLPI",
                        "Mide la presión operativa de GLPI como consecuencia observable.",
                        glpiOperationalPressureFormula(),
                        transversalRiskThresholds("transversal.operationalPressure"),
                        List.of(
                                "Tickets abiertos",
                                "Tickets críticos abiertos",
                                "Porcentaje de cierre diario",
                                "Porcentaje de cierre semanal"
                        )
                ),
                relationDefinition(
                        "aruba_glpi_relation",
                        "Relacion Aruba-GLPI",
                        "Aruba estado de red"
                ),
                relationDefinition(
                        "citrix_glpi_relation",
                        "Relacion Citrix-GLPI",
                        "Citrix indice de salud Citrix"
                ),
                relationDefinition(
                        "microsoft365_glpi_relation",
                        "Relacion Microsoft365-GLPI",
                        "Microsoft 365 indice de salud Microsoft 365"
                ),
                definition(
                        "analysis_technical_degradation",
                        "Degradación técnica",
                        "ANALYSIS",
                        "GLOBAL",
                        "Mide la afección técnica conjunta de Aruba, Citrix y Microsoft 365 para el panel de análisis.",
                        platformWeightFormula(
                                "Aruba estado de red",
                                "Citrix indice de salud",
                                "Microsoft 365 indice de salud",
                                "GLPI",
                                kpiProperties.getWeights().getAnalysisTechnicalDegradation()),
                        transversalRiskThresholds("transversal.technicalDegradation"),
                        List.of(
                                "Aruba estado de red",
                                "Citrix indice de salud Citrix",
                                "Microsoft 365 indice de salud Microsoft 365"
                        )
                ),
                definition(
                        "analysis_user_impact",
                        "Impacto en usuarios",
                        "ANALYSIS",
                        "GLOBAL",
                        "Mide si la degradación técnica se traduce en afección visible para usuarios.",
                        platformWeightFormula(
                                "Aruba impacto usuario",
                                "Citrix impacto usuario",
                                "Microsoft 365 impacto usuario",
                                "presión GLPI",
                                kpiProperties.getWeights().getAnalysisUserImpact()),
                        transversalRiskThresholds("transversal.userImpact"),
                        List.of(
                                "Impacto usuario Aruba",
                                "Impacto usuario Citrix",
                                "Impacto usuario Microsoft 365",
                                "Presión operativa GLPI"
                        )
                ),
                definition(
                        "technical_operational_conversion",
                        "Conversion técnica-operativa",
                        "ANALYSIS",
                        "GLOBAL",
                        "Mide la coincidencia entre degradación técnica e impacto operativo.",
                        "min(Degradación técnica, Impacto en usuarios)",
                        commonAffectionThresholds(),
                        List.of(
                                "Degradación técnica",
                                "Impacto en usuarios"
                        )
                ),
                definition(
                        "high_high_cooccurrence",
                        "Co-ocurrencia alta-alta",
                        "ANALYSIS",
                        "GLOBAL",
                        "Mide el porcentaje de snapshots donde coinciden plataforma afectada y GLPI alto.",
                        "Snapshots con plataforma >= " + kpiProperties.getStatus().getYellowMin()
                                + " y GLPI >= " + kpiProperties.getStatus().getYellowMin()
                                + " dividido entre total de snapshots.",
                        commonAffectionThresholds(),
                        List.of(
                                "Snapshots de análisis",
                                "Afección de plataforma seleccionada",
                                "Presión operativa GLPI"
                        )
                )
        );
    }

    private KpiDefinitionDto relationDefinition(
            String id,
            String name,
            String platformSource
    ) {

        return definition(
                id,
                name,
                "ANALYSIS",
                "GLOBAL",
                "Mide relación operativa aparente entre una plataforma técnica y la presión GLPI.",
                "min(Presión operativa GLPI, " + platformSource + ")",
                commonAffectionThresholds(),
                List.of(
                        "Presión operativa GLPI",
                        platformSource
                )
        );
    }

    private KpiDefinitionDto definition(
            String id,
            String name,
            String type,
            String platform,
            String description,
            String formula,
            KpiThresholdDto thresholds,
            List<String> sources
    ) {

        return new KpiDefinitionDto(
                id,
                name,
                type,
                platform,
                description,
                formula,
                thresholds,
                sources
        );
    }

    private List<String> platformHealthSources() {

        return List.of(
                "Aruba estado de red",
                "Citrix indice de salud Citrix",
                "Microsoft 365 indice de salud Microsoft 365",
                "GLPI indice de salud GLPI"
        );
    }

    private String platformWeightFormula(
            String arubaLabel,
            String citrixLabel,
            String microsoft365Label,
            String glpiLabel,
            KpiProperties.PlatformWeights weights
    ) {

        List<String> parts = new java.util.ArrayList<>();

        addWeightedPart(parts, arubaLabel, weights.getAruba());
        addWeightedPart(parts, citrixLabel, weights.getCitrix());
        addWeightedPart(parts, microsoft365Label, weights.getMicrosoft365());
        addWeightedPart(parts, glpiLabel, weights.getGlpi());

        return String.join(" + ", parts);
    }

    private void addWeightedPart(List<String> parts, String label, double weight) {

        if (weight == 0) {
            return;
        }

        parts.add(label + " * " + kpiProperties.formatWeight(weight));
    }

    private String glpiOperationalPressureFormula() {

        KpiProperties.GlpiPressureWeights weights =
                kpiProperties.getWeights().getGlpiOperationalPressure();

        return "Tickets abiertos * " + kpiProperties.formatWeight(weights.getOpenTickets())
                + " + cierre diario * " + kpiProperties.formatWeight(weights.getClosedTodayPercent())
                + " + tickets críticos * " + kpiProperties.formatWeight(weights.getCriticalTickets())
                + " + cierre semanal * " + kpiProperties.formatWeight(weights.getClosedWeekPercent());
    }

    private String affectedServicesFormula() {

        int platformContribution =
                kpiProperties.getStatus().getMax() / platformHealthSources().size();

        return "Cada plataforma en amarillo o rojo suma " + platformContribution
                + "%: Aruba, Citrix, Microsoft 365 y GLPI.";
    }

    private String arubaNetworkStatusFormula() {

        return "Access Points aportan hasta "
                + kpiProperties.getAruba().getAccessPointBlockWeight()
                + " puntos y switches hasta "
                + kpiProperties.getAruba().getSwitchBlockWeight()
                + " puntos. El bloque amarillo aporta la mitad del peso interno configurado. AP amarillo desde el "
                + kpiProperties.getAruba().getAccessPointDownYellowPercent()
                + "% y rojo desde el "
                + kpiProperties.getAruba().getAccessPointDownRedPercent()
                + "% o si no hay clientes WiFi/MUTUALIA. Switches amarillo desde "
                + kpiProperties.getAruba().getSwitchDownYellowMin()
                + " switches caidos o con upgrade pendiente. Una condicion roja prevalece sobre el porcentaje.";
    }

    private String citrixHealthFormula() {

        return "Media uniforme de sesiones activas, Delivery Controllers, Average Logon Duration, carga de servidores y errores de inicio. "
                + "Sin sesiones activas es rojo. Delivery Controllers: 0 disponibles rojo, menos del "
                + kpiProperties.getCitrix().getDeliveryControllerYellowBelowPercent()
                + "% amarillo. Logon: > "
                + kpiProperties.getCitrix().getLogonDurationRedAboveSeconds()
                + "s rojo, > "
                + kpiProperties.getCitrix().getLogonDurationYellowAboveSeconds()
                + "s amarillo. Carga: >= "
                + kpiProperties.getCitrix().getServerLoadRedMin()
                + "% rojo, >= "
                + kpiProperties.getCitrix().getServerLoadYellowMin()
                + "% amarillo. Errores: > "
                + kpiProperties.getCitrix().getFailedLogonsRedAbove()
                + " rojo, > "
                + kpiProperties.getCitrix().getFailedLogonsYellowAbove()
                + " amarillo.";
    }

    private String microsoft365HealthFormula() {

        return "Media uniforme de SharePoint, usuarios sin MFA, secretos proximos a caducar, equipos no conformes, Windows desactualizados y equipos sin cifrado. "
                + "SharePoint: > "
                + kpiProperties.getMicrosoft365().getSharePointRedAbove()
                + "% rojo, >= "
                + kpiProperties.getMicrosoft365().getSharePointYellowMin()
                + "% amarillo. Usuarios sin MFA: > "
                + kpiProperties.getMicrosoft365().getUsersWithoutMfaRedAbove()
                + " rojo, > "
                + kpiProperties.getMicrosoft365().getUsersWithoutMfaYellowAbove()
                + " amarillo. Secretos proximos a caducar: > "
                + kpiProperties.getMicrosoft365().getSecretsYellowAbove()
                + " amarillo. Equipos no conformes: > "
                + kpiProperties.getMicrosoft365().getNonCompliantDevicesRedAbove()
                + " rojo, > "
                + kpiProperties.getMicrosoft365().getNonCompliantDevicesYellowAbove()
                + " amarillo. Windows desactualizados: > "
                + kpiProperties.getMicrosoft365().getOutdatedWindowsYellowAbove()
                + " amarillo. Equipos sin cifrado: > "
                + kpiProperties.getMicrosoft365().getDevicesWithoutEncryptionRedAbove()
                + " rojo, > "
                + kpiProperties.getMicrosoft365().getDevicesWithoutEncryptionYellowAbove()
                + " amarillo.";
    }

    private String glpiHealthFormula() {

        return "Media uniforme de tickets abiertos, tickets críticos, porcentaje de cierre diario y porcentaje de cierre semanal. "
                + "Tickets abiertos: >= "
                + kpiProperties.getGlpi().getOpenTicketsRedMin()
                + " rojo, >= "
                + kpiProperties.getGlpi().getOpenTicketsYellowMin()
                + " amarillo. Tickets críticos: > "
                + kpiProperties.getGlpi().getCriticalTicketsRedAbove()
                + " rojo, > "
                + kpiProperties.getGlpi().getCriticalTicketsYellowAbove()
                + " amarillo. Porcentaje de cierre diario/semanal: >= "
                + kpiProperties.getGlpi().getClosedPercentGreenMin()
                + "% verde, por debajo amarillo.";
    }

    private KpiThresholdDto transversalRiskThresholds(String metricKey) {

        KpiProperties.TransversalKpiThreshold threshold =
                kpiProperties.getTransversal().thresholdFor(metricKey);
        int yellowMin = threshold.getYellowMin();
        int redMin = threshold.getRedMin();
        int max = kpiProperties.getStatus().getMax();

        return new KpiThresholdDto(
                "0-" + (yellowMin - 1),
                yellowMin + "-" + (redMin - 1),
                redMin + "-" + max
        );
    }

    private KpiThresholdDto transversalHealthThresholds(String metricKey) {

        KpiProperties.TransversalKpiThreshold threshold =
                kpiProperties.getTransversal().thresholdFor(metricKey);
        int yellowMin = threshold.getYellowMin();
        int greenMin = threshold.getGreenMin();
        int max = kpiProperties.getStatus().getMax();

        return new KpiThresholdDto(
                greenMin + "-" + max,
                yellowMin + "-" + (greenMin - 1),
                "0-" + (yellowMin - 1)
        );
    }

    private KpiThresholdDto commonAffectionThresholds() {

        int yellowMin = kpiProperties.getStatus().getYellowMin();
        int redMin = kpiProperties.getStatus().getRedMin();
        int max = kpiProperties.getStatus().getMax();

        return new KpiThresholdDto(
                "0-" + (yellowMin - 1),
                yellowMin + "-" + (redMin - 1),
                redMin + "-" + max
        );
    }
}
