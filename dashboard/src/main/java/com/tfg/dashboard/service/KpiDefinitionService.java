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
                        "Mide la afeccion de red Aruba combinando Access Points, switches y clientes WiFi.",
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
                        "Mide la afeccion del entorno Citrix a partir de indicadores internos normalizados.",
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
                        "Mide la afeccion de Microsoft 365 a partir de capacidad, identidad, seguridad y dispositivos.",
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
                        "Mide la afeccion operativa de GLPI a partir de volumen y capacidad de cierre de tickets.",
                        glpiHealthFormula(),
                        commonAffectionThresholds(),
                        List.of(
                                "Tickets abiertos",
                                "Tickets criticos abiertos",
                                "Porcentaje de tickets cerrados",
                                "Porcentaje de tickets cerrados semana"
                        )
                ),
                definition(
                        "global_status",
                        "Estado global",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide la afeccion global ponderada de la infraestructura monitorizada.",
                        platformWeightFormula(
                                "Aruba estado de red",
                                "Citrix indice de salud",
                                "Microsoft 365 indice de salud",
                                "GLPI indice de salud",
                                kpiProperties.getWeights().getGlobalStatus()),
                        commonAffectionThresholds(),
                        platformHealthSources()
                ),
                definition(
                        "global_criticality",
                        "Criticidad global",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide la presencia de condiciones criticas en rojo dentro de las plataformas.",
                        "Media de indicadores criticos normalizados: correcto 0, advertencia 50 y critico 100.",
                        commonAffectionThresholds(),
                        List.of(
                                "Condiciones criticas Aruba",
                                "Condiciones criticas Citrix",
                                "Condiciones criticas Microsoft 365",
                                "Condiciones criticas GLPI"
                        )
                ),
                definition(
                        "global_availability",
                        "Disponibilidad global",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide la afeccion sobre la disponibilidad de los servicios principales.",
                        platformWeightFormula(
                                "Aruba disponibilidad",
                                "Citrix disponibilidad",
                                "Microsoft 365 disponibilidad",
                                "GLPI disponibilidad",
                                kpiProperties.getWeights().getAvailability()),
                        commonAffectionThresholds(),
                        List.of(
                                "Disponibilidad APs y switches Aruba",
                                "Sesiones activas y Delivery Controllers Citrix",
                                "SharePoint y secretos Microsoft 365",
                                "GLPI como soporte operativo"
                        )
                ),
                definition(
                        "operational_pressure",
                        "Presion operativa",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide la carga de trabajo tecnica y operativa acumulada.",
                        platformWeightFormula(
                                "Aruba",
                                "Citrix",
                                "Microsoft 365",
                                "GLPI",
                                kpiProperties.getWeights().getOperationalPressure()),
                        commonAffectionThresholds(),
                        List.of(
                                "Tickets GLPI",
                                "Errores y carga Citrix",
                                "Dispositivos Microsoft 365",
                                "APs inactivos y firmware Aruba"
                        )
                ),
                definition(
                        "technical_degradation",
                        "Degradacion tecnica",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide deterioro tecnico aunque no exista una caida total.",
                        platformWeightFormula(
                                "Aruba",
                                "Citrix",
                                "Microsoft 365",
                                "GLPI",
                                kpiProperties.getWeights().getTechnicalDegradation()),
                        commonAffectionThresholds(),
                        List.of(
                                "Firmware e inactividad Aruba",
                                "Logon, carga y errores Citrix",
                                "SharePoint, secretos y dispositivos Microsoft 365",
                                "Tickets criticos GLPI"
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
                        commonAffectionThresholds(),
                        List.of(
                                "Logon, sesiones, Delivery Controllers y errores Citrix",
                                "Estado de red Aruba",
                                "Tickets criticos y cierre GLPI",
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
                        commonAffectionThresholds(),
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
                        "Mide la afeccion que pueden percibir los usuarios.",
                        platformWeightFormula(
                                "Aruba",
                                "Citrix",
                                "Microsoft 365",
                                "GLPI",
                                kpiProperties.getWeights().getUserImpact()),
                        commonAffectionThresholds(),
                        List.of(
                                "Clientes WiFi, APs y switches Aruba",
                                "Sesiones, logon, errores y Delivery Controllers Citrix",
                                "SharePoint, MFA y dispositivos Microsoft 365",
                                "Tickets abiertos y criticos GLPI"
                        )
                ),
                definition(
                        "affected_services",
                        "Servicios afectados",
                        "TRANSVERSAL",
                        "GLOBAL",
                        "Mide cuantas plataformas estan afectadas.",
                        affectedServicesFormula(),
                        commonAffectionThresholds(),
                        platformHealthSources()
                ),
                definition(
                        "glpi_operational_pressure",
                        "Presion operativa GLPI",
                        "ANALYSIS",
                        "GLPI",
                        "Mide la presion operativa de GLPI como consecuencia observable.",
                        glpiOperationalPressureFormula(),
                        commonAffectionThresholds(),
                        List.of(
                                "Tickets abiertos",
                                "Tickets criticos abiertos",
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
                        "Degradacion tecnica",
                        "ANALYSIS",
                        "GLOBAL",
                        "Mide la afeccion tecnica conjunta de Aruba, Citrix y Microsoft 365 para el panel de analisis.",
                        platformWeightFormula(
                                "Aruba estado de red",
                                "Citrix indice de salud",
                                "Microsoft 365 indice de salud",
                                "GLPI",
                                kpiProperties.getWeights().getAnalysisTechnicalDegradation()),
                        commonAffectionThresholds(),
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
                        "Mide si la degradacion tecnica se traduce en afeccion visible para usuarios.",
                        platformWeightFormula(
                                "Aruba impacto usuario",
                                "Citrix impacto usuario",
                                "Microsoft 365 impacto usuario",
                                "presion GLPI",
                                kpiProperties.getWeights().getAnalysisUserImpact()),
                        commonAffectionThresholds(),
                        List.of(
                                "Impacto usuario Aruba",
                                "Impacto usuario Citrix",
                                "Impacto usuario Microsoft 365",
                                "Presion operativa GLPI"
                        )
                ),
                definition(
                        "technical_operational_conversion",
                        "Conversion tecnica-operativa",
                        "ANALYSIS",
                        "GLOBAL",
                        "Mide la coincidencia entre degradacion tecnica e impacto operativo.",
                        "min(Degradacion tecnica, Impacto en usuarios)",
                        commonAffectionThresholds(),
                        List.of(
                                "Degradacion tecnica",
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
                                "Snapshots de analisis",
                                "Afeccion de plataforma seleccionada",
                                "Presion operativa GLPI"
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
                "Mide relacion operativa aparente entre una plataforma tecnica y la presion GLPI.",
                "min(Presion operativa GLPI, " + platformSource + ")",
                commonAffectionThresholds(),
                List.of(
                        "Presion operativa GLPI",
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
                + " + tickets criticos * " + kpiProperties.formatWeight(weights.getCriticalTickets())
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
                + kpiProperties.getAruba().getBlockRedContribution()
                + " puntos y switches hasta "
                + kpiProperties.getAruba().getBlockRedContribution()
                + " puntos. El bloque amarillo aporta "
                + kpiProperties.getAruba().getBlockYellowContribution()
                + " puntos. AP rojo si caen todos, si caen al menos el "
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
                + " rojo.";
    }

    private String glpiHealthFormula() {

        return "Media uniforme de tickets abiertos, tickets criticos, porcentaje de cierre diario y porcentaje de cierre semanal. "
                + "Tickets abiertos: >= "
                + kpiProperties.getGlpi().getOpenTicketsRedMin()
                + " rojo, >= "
                + kpiProperties.getGlpi().getOpenTicketsYellowMin()
                + " amarillo. Tickets criticos: > "
                + kpiProperties.getGlpi().getCriticalTicketsRedAbove()
                + " rojo, > "
                + kpiProperties.getGlpi().getCriticalTicketsYellowAbove()
                + " amarillo. Porcentaje de cierre diario/semanal: >= "
                + kpiProperties.getGlpi().getClosedPercentGreenMin()
                + "% verde, por debajo amarillo.";
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
