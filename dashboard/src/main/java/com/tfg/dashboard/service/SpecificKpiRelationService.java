package com.tfg.dashboard.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.SpecificKpiRelationDto;
import java.util.ArrayList;
import com.tfg.dashboard.dto.SpecificKpiRelationPointDto;
import com.tfg.dashboard.model.AnalysisSnapshot;

/**
 * Construye relaciones exploratorias entre indicadores concretos.
 *
 * Los puntos se agregan por dia para evitar nubes de snapshots demasiado densas
 * y la lectura distingue co-ocurrencia de tendencia visual.
 */
@Service
public class SpecificKpiRelationService {

    private static final int MINIMUM_POINTS = 3;
    private static final int HOURS_PER_BUCKET = 6;
    private static final int HIGH_RELATION_PERCENT = 60;
    private static final int MODERATE_RELATION_PERCENT = 35;
    private static final double MINIMUM_ABSOLUTE_VARIATION = 5.0;
    private static final double MINIMUM_RELATIVE_VARIATION = 0.10;
    private static final double STRONG_POSITIVE_TREND = 0.45;
    private static final double MODERATE_POSITIVE_TREND = 0.25;
    private static final String UNKNOWN_STATUS = "UNKNOWN";

    private final KpiProperties kpiProperties;
    private final KpiScoringService kpiScoringService;

    public SpecificKpiRelationService(
            KpiProperties kpiProperties,
            KpiScoringService kpiScoringService) {

        this.kpiProperties = kpiProperties;
        this.kpiScoringService = kpiScoringService;
    }

    /**
     * Devuelve las cinco relaciones especificas predefinidas para el bloque
     * final del panel de analisis.
     */
        public List<SpecificKpiRelationDto> buildRelations(List<AnalysisSnapshot> snapshots) {
                List<SpecificKpiRelationDto> relations = new ArrayList<>(List.of(
                                highHighRelation(
                        "aruba_network_vs_citrix_logon",
                        "Aruba estado de red vs Citrix logon",
                        "Afección de red Aruba",
                        "Duración media de logon Citrix",
                        "%",
                        "s",
                        "Permite ver si una mayor afección de red Aruba coincide con inicios de sesión Citrix más lentos.",
                        "Si los puntos suben hacia la derecha, existe una coincidencia aparente entre degradación de red y lentitud en el acceso a aplicaciones.",
                        snapshots,
                        AnalysisSnapshot::getArubaHealth,
                        AnalysisSnapshot::getCitrixAverageLogonDurationSeconds,
                        (double) kpiProperties.getStatus().getYellowMin(),
                        (double) kpiProperties.getCitrix().getLogonDurationYellowAboveSeconds() + 1),
                lowLowRelation(
                        "aruba_wifi_clients_vs_citrix_sessions",
                        "Aruba clientes WiFi vs Citrix sesiones activas",
                        "Clientes WiFi Aruba",
                        "Sesiones activas Citrix",
                        "",
                        "",
                        "Ayuda a revisar si una reducción de clientes WiFi coincide con menor uso de Citrix.",
                        "Los puntos bajos en ambos ejes indican coincidencia aparente entre menor conectividad WiFi y menor actividad Citrix.",
                        snapshots,
                        AnalysisSnapshot::getArubaWifiClients,
                        AnalysisSnapshot::getCitrixActiveSessions),
                highHighRelation(
                        "citrix_failed_logons_vs_citrix_open_tickets",
                        "Citrix errores de inicio vs tickets abiertos Citrix",
                        "Errores de inicio Citrix",
                        "Tickets abiertos Citrix",
                        "",
                        "",
                        "Conecta fallos técnicos de acceso con tickets GLPI clasificados como Citrix.",
                        "Si ambos valores crecen a la vez, puede orientar la revisión de acceso Citrix y tickets asociados.",
                        snapshots,
                        AnalysisSnapshot::getCitrixFailedLogons,
                        AnalysisSnapshot::getCitrixOpenTickets,
                        (double) kpiProperties.getCitrix().getFailedLogonsYellowAbove() + 1,
                        (double) kpiProperties.getGlpi().getOpenTicketsYellowMin()),
                highHighRelation(
                        "microsoft365_non_compliant_devices_vs_microsoft365_open_tickets",
                        "Microsoft 365 equipos no conformes vs tickets abiertos Microsoft 365",
                        "Equipos no conformes Microsoft 365",
                        "Tickets abiertos Microsoft 365",
                        "",
                        "",
                        "Relaciona señales de cumplimiento Microsoft 365 con tickets GLPI clasificados como Microsoft 365.",
                        "Una coincidencia alta puede orientar la revisión de cumplimiento y su reflejo operativo en IT.",
                        snapshots,
                        AnalysisSnapshot::getMicrosoft365NonCompliantDevices,
                        AnalysisSnapshot::getMicrosoft365OpenTickets,
                        (double) kpiProperties.getMicrosoft365().getNonCompliantDevicesYellowAbove() + 1,
                        (double) kpiProperties.getGlpi().getOpenTicketsYellowMin()),
                highHighRelation(
                        "affected_services_vs_glpi_pressure",
                        "Servicios afectados vs presión operativa GLPI",
                        "Servicios afectados",
                        "Presión operativa GLPI",
                        "%",
                        "%",
                        "Muestra si una afección transversal del entorno coincide con mayor presión en GLPI.",
                        "Si los puntos se concentran en valores altos de ambos ejes, la presión operativa coincide con más plataformas afectadas.",
                        snapshots,
                        AnalysisSnapshot::getAffectedServicesPercent,
                        AnalysisSnapshot::getGlpiOperationalPressure,
                        (double) kpiProperties.getStatus().getYellowMin(),
                        (double) kpiProperties.getStatus().getYellowMin())));

        // Añadimos relación: Degradación técnica vs impacto en usuarios
        relations.add(highHighRelation(
                "technical_degradation_vs_user_impact",
                "Degradación técnica vs impacto en usuarios",
                "Degradación técnica",
                "Impacto en usuarios",
                "%",
                "%",
                        "Permite ver si los momentos con mayor deterioro técnico coinciden con mayor impacto potencial sobre los usuarios.",
                        "No demuestra causalidad: la coincidencia observada puede orientar la revisión y ayudar a priorizar investigaciones. Usa expresiones como 'coincide con', 'puede orientar la revisión' y 'relación aparente'.",
                snapshots,
                AnalysisSnapshot::getTechnicalDegradation,
                AnalysisSnapshot::getUserImpact,
                (double) kpiProperties.getStatus().getYellowMin(),
                (double) kpiProperties.getStatus().getYellowMin()));

        // Añadimos relación: Afección Aruba vs presión operativa GLPI
        relations.add(highHighRelation(
                "aruba_affectation_vs_glpi_pressure",
                "Afección Aruba vs presión operativa GLPI",
                "Afección Aruba",
                "Presión operativa GLPI",
                "%",
                "%",
                        "Permite comprobar si los periodos con mayor afección de red Aruba coinciden con mayor carga operativa reflejada en GLPI.",
                        "Si existen tickets categorizados por plataforma, se recomienda interpretar esta relación con la presión/tickets asociados a Aruba; en ausencia de dicha categorización, muestra la presión operativa GLPI general. No demuestra causalidad, solo indica coincidencias aparentes.",
                snapshots,
                AnalysisSnapshot::getArubaHealth,
                AnalysisSnapshot::getGlpiOperationalPressure,
                (double) kpiProperties.getStatus().getYellowMin(),
                (double) kpiProperties.getStatus().getYellowMin()));

        relations.add(highHighRelation(
                "citrix_affectation_vs_operational_pressure",
                "Afección Citrix vs presión operativa GLPI",
                "Afección Citrix",
                "Presión operativa GLPI",
                "%",
                "%",
                        "Permite comprobar si los periodos con mayor afección Citrix coinciden con mayor carga operativa reflejada en GLPI.",
                        "Si ambos valores crecen a la vez, puede orientar la revisión de tickets y cargas asociados a Citrix. No demuestra causalidad, solo indica coincidencias aparentes.",
                snapshots,
                AnalysisSnapshot::getCitrixHealth,
                AnalysisSnapshot::getGlpiOperationalPressure,
                (double) kpiProperties.getStatus().getYellowMin(),
                (double) kpiProperties.getStatus().getYellowMin()));

        relations.add(highHighRelation(
                "microsoft365_affectation_vs_operational_pressure",
                "Afección Microsoft 365 vs presión operativa GLPI",
                "Afección Microsoft 365",
                "Presión operativa GLPI",
                "%",
                "%",
                        "Permite comprobar si los periodos con mayor afección Microsoft 365 coinciden con mayor carga operativa reflejada en GLPI.",
                        "Si ambos valores crecen a la vez, puede orientar la revisión de tickets y cargas asociados a Microsoft 365. No demuestra causalidad, solo indica coincidencias aparentes.",
                snapshots,
                AnalysisSnapshot::getMicrosoft365Health,
                AnalysisSnapshot::getGlpiOperationalPressure,
                (double) kpiProperties.getStatus().getYellowMin(),
                (double) kpiProperties.getStatus().getYellowMin()));

        return relations;
    }

    private SpecificKpiRelationDto highHighRelation(
            String code,
            String title,
            String xLabel,
            String yLabel,
            String xUnit,
            String yUnit,
            String description,
            String interpretation,
            List<AnalysisSnapshot> snapshots,
            Function<AnalysisSnapshot, Integer> xExtractor,
            Function<AnalysisSnapshot, Integer> yExtractor,
            Double xHighThreshold,
            Double yHighThreshold) {

        return buildRelation(
                code,
                title,
                xLabel,
                yLabel,
                xUnit,
                yUnit,
                description,
                interpretation,
                snapshots,
                xExtractor,
                yExtractor,
                values -> values.stream()
                        .filter(point -> point.getX() >= xHighThreshold && point.getY() >= yHighThreshold)
                        .count(),
                "co-ocurrencia de valores altos");
    }

    private SpecificKpiRelationDto lowLowRelation(
            String code,
            String title,
            String xLabel,
            String yLabel,
            String xUnit,
            String yUnit,
            String description,
            String interpretation,
            List<AnalysisSnapshot> snapshots,
            Function<AnalysisSnapshot, Integer> xExtractor,
            Function<AnalysisSnapshot, Integer> yExtractor) {

        return buildRelation(
                code,
                title,
                xLabel,
                yLabel,
                xUnit,
                yUnit,
                description,
                interpretation,
                snapshots,
                xExtractor,
                yExtractor,
                values -> {
                    double averageX = average(values, SpecificKpiRelationPointDto::getX);
                    double averageY = average(values, SpecificKpiRelationPointDto::getY);

                    return values.stream()
                            .filter(point -> point.getX() <= averageX && point.getY() <= averageY)
                            .count();
                },
                "coincidencia de valores bajos");
    }

    private SpecificKpiRelationDto buildRelation(
            String code,
            String title,
            String xLabel,
            String yLabel,
            String xUnit,
            String yUnit,
            String description,
            String interpretation,
            List<AnalysisSnapshot> snapshots,
            Function<AnalysisSnapshot, Integer> xExtractor,
            Function<AnalysisSnapshot, Integer> yExtractor,
            Function<List<SpecificKpiRelationPointDto>, Long> matchingCounter,
            String coincidenceLabel) {

        List<SpecificKpiRelationPointDto> points = aggregateDaily(
                snapshots.stream()
                        .filter(snapshot -> snapshot.getTimestamp() != null)
                        .filter(snapshot -> xExtractor.apply(snapshot) != null && yExtractor.apply(snapshot) != null)
                        .map(snapshot -> new SpecificKpiRelationPointDto(
                                snapshot.getTimestamp(),
                                xExtractor.apply(snapshot).doubleValue(),
                                yExtractor.apply(snapshot).doubleValue(),
                                1,
                                Boolean.TRUE.equals(snapshot.isGeneratedScenario())))
                        .toList());

        SpecificKpiRelationDto relation = new SpecificKpiRelationDto();
        relation.setCode(code);
        relation.setTitle(title);
        relation.setXLabel(xLabel);
        relation.setYLabel(yLabel);
        relation.setXUnit(xUnit);
        relation.setYUnit(yUnit);
        relation.setDescription(description);
        relation.setPoints(points);
        relation.setHasEnoughData(points.size() >= MINIMUM_POINTS);

        if (!relation.isHasEnoughData()) {
            relation.setReading("Sin datos suficientes para valorar esta relación.");
            relation.setReadingStatus(KpiScoringService.NO_DATA);
            return relation;
        }

        boolean xHasVariation = hasMeaningfulVariation(points, SpecificKpiRelationPointDto::getX);
        boolean yHasVariation = hasMeaningfulVariation(points, SpecificKpiRelationPointDto::getY);

        if (!xHasVariation || !yHasVariation) {
            relation.setReading(insufficientVariationText(xHasVariation, yHasVariation));
            relation.setReadingStatus(UNKNOWN_STATUS);
            return relation;
        }

        int coincidencePercent = kpiScoringService.clampToInt(
                matchingCounter.apply(points) * 100.0 / points.size());
        double positiveTrend = pearson(points);

        relation.setReading(readingText(
                coincidencePercent,
                positiveTrend,
                interpretation,
                coincidenceLabel));
        relation.setReadingStatus(kpiScoringService.statusFromAffection(coincidencePercent));

        return relation;
    }

    private List<SpecificKpiRelationPointDto> aggregateDaily(List<SpecificKpiRelationPointDto> rawPoints) {
        return rawPoints.stream()
                .collect(Collectors.groupingBy(point -> point.getTimestamp().toLocalDate()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> dailyPoint(entry.getKey(), entry.getValue()))
                .toList();
    }

    private SpecificKpiRelationPointDto dailyPoint(
            LocalDate day,
            List<SpecificKpiRelationPointDto> dayPoints) {

        List<SpecificKpiRelationPointDto> bucketPoints = dayPoints.stream()
                .collect(Collectors.groupingBy(point -> point.getTimestamp().getHour() / HOURS_PER_BUCKET))
                .values()
                .stream()
                .map(this::averageBucket)
                .sorted(Comparator.comparing(SpecificKpiRelationPointDto::getTimestamp))
                .toList();

        return new SpecificKpiRelationPointDto(
                day.atStartOfDay(),
                average(bucketPoints, SpecificKpiRelationPointDto::getX),
                average(bucketPoints, SpecificKpiRelationPointDto::getY),
                bucketPoints.size(),
                bucketPoints.stream().anyMatch(SpecificKpiRelationPointDto::isGeneratedScenario));
    }

    private SpecificKpiRelationPointDto averageBucket(List<SpecificKpiRelationPointDto> bucketPoints) {
        LocalDateTime timestamp = bucketPoints.get(0).getTimestamp();

        return new SpecificKpiRelationPointDto(
                timestamp,
                average(bucketPoints, SpecificKpiRelationPointDto::getX),
                average(bucketPoints, SpecificKpiRelationPointDto::getY),
                bucketPoints.size(),
                bucketPoints.stream().anyMatch(SpecificKpiRelationPointDto::isGeneratedScenario));
    }

    private String readingText(
            int coincidencePercent,
            double positiveTrend,
            String interpretation,
            String coincidenceLabel) {

        if (coincidencePercent >= HIGH_RELATION_PERCENT
                && positiveTrend >= STRONG_POSITIVE_TREND) {

            return "Relación alta: hay " + coincidenceLabel
                    + " y una tendencia visual positiva. "
                    + interpretation
                    + " Coincidencia observada: " + coincidencePercent
                    + "%. Es una relación aparente para orientar la revisión.";
        }

        if (coincidencePercent >= HIGH_RELATION_PERCENT) {
            return "Hay " + coincidenceLabel
                    + " en varios días, pero no una tendencia clara. "
                    + "Coincidencia observada: " + coincidencePercent
                    + "%. Puede orientar la revisión.";
        }

        if (coincidencePercent >= MODERATE_RELATION_PERCENT
                || positiveTrend >= STRONG_POSITIVE_TREND) {

            return "Relación moderada: se aprecia cierta coincidencia aparente entre ambos indicadores. "
                    + interpretation
                    + " Coincidencia observada: " + coincidencePercent
                    + "%. Sirve como señal exploratoria.";
        }

        if (positiveTrend >= MODERATE_POSITIVE_TREND) {
            return "Tendencia visual débil: algunos días apuntan en la misma dirección, "
                    + "pero la coincidencia observada es solo del " + coincidencePercent
                    + "%. Se necesita más histórico para interpretar la relación.";
        }

        return "Relación baja: no se aprecia un patrón claro en las capturas disponibles. "
                + "La coincidencia observada es del " + coincidencePercent
                + "% y solo debe usarse como referencia exploratoria.";
    }

    private String insufficientVariationText(boolean xHasVariation, boolean yHasVariation) {
        if (!xHasVariation && !yHasVariation) {
            return "No hay variación suficiente para interpretar una relación clara. "
                    + "Los snapshots tienen valores muy parecidos en ambos ejes; se necesita más histórico "
                    + "o mayor variación en los datos.";
        }

        if (!xHasVariation) {
            return "No hay variación suficiente para interpretar una relación clara. "
                    + "Los snapshots tienen valores muy parecidos en el eje X, por lo que no se puede "
                    + "interpretar una tendencia clara. Se necesita más histórico o mayor variación.";
        }

        return "No hay variación suficiente para interpretar una relación clara. "
                + "Los snapshots tienen valores muy parecidos en el eje Y, por lo que no se puede "
                + "interpretar una tendencia clara. Se necesita más histórico o mayor variación.";
    }

    private boolean hasMeaningfulVariation(
            List<SpecificKpiRelationPointDto> points,
            Function<SpecificKpiRelationPointDto, Double> extractor) {

        double min = points.stream()
                .mapToDouble(extractor::apply)
                .min()
                .orElse(0);
        double max = points.stream()
                .mapToDouble(extractor::apply)
                .max()
                .orElse(0);
        double range = max - min;
        double reference = Math.max(1.0, Math.max(Math.abs(min), Math.abs(max)));

        return range >= MINIMUM_ABSOLUTE_VARIATION
                || range / reference >= MINIMUM_RELATIVE_VARIATION;
    }

    private double pearson(List<SpecificKpiRelationPointDto> points) {
        double averageX = average(points, SpecificKpiRelationPointDto::getX);
        double averageY = average(points, SpecificKpiRelationPointDto::getY);
        double numerator = 0;
        double xVariance = 0;
        double yVariance = 0;

        for (SpecificKpiRelationPointDto point : points) {
            double xDiff = point.getX() - averageX;
            double yDiff = point.getY() - averageY;

            numerator += xDiff * yDiff;
            xVariance += xDiff * xDiff;
            yVariance += yDiff * yDiff;
        }

        double denominator = Math.sqrt(xVariance * yVariance);

        return denominator == 0 ? 0 : numerator / denominator;
    }

    private double average(
            List<SpecificKpiRelationPointDto> points,
            Function<SpecificKpiRelationPointDto, Double> extractor) {

        return points.stream()
                .mapToDouble(extractor::apply)
                .average()
                .orElse(0);
    }
}
