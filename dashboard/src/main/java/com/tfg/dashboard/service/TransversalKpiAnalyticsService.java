package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.dto.AnalyticsComparePoint;
import com.tfg.dashboard.dto.AnalyticsCompareResponse;
import com.tfg.dashboard.dto.ArubaNetworkStatusDto;
import com.tfg.dashboard.dto.TransversalKpiCatalogItem;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.model.MainDashboardSummary;
import com.tfg.dashboard.model.TransversalKpiHistory;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;
import com.tfg.dashboard.repository.TransversalKpiHistoryRepository;

@Service
public class TransversalKpiAnalyticsService {

    private static final String GLOBAL_HEALTH = "global_health";

    private static final String GLOBAL_CRITICALITY = "global_criticality";

    private static final String GLOBAL_AVAILABILITY = "global_availability";

    private static final String USER_IMPACT = "user_impact";

    private static final String AFFECTED_SERVICES = "affected_services";

    private static final String TECHNICAL_DEGRADATION =
            "technical_degradation";

    private static final String OPERATIONAL_PRESSURE =
            "operational_pressure";

    private static final String OPERATIONAL_BACKLOG =
            "operational_backlog";

    private static final String SLA_RISK = "sla_risk";

    private static final String ENVIRONMENT_STABILITY =
            "environment_stability";

    private static final String OPERATIONAL_PRIORITY =
            "operational_priority";

    private static final String ARUBA_NETWORK_AFFECTATION =
            "aruba_network_affectation";

    private static final String ARUBA_NETWORK_DEGRADATION =
            "aruba_network_degradation";

    private static final String ARUBA_NETWORK_HEALTH =
            "aruba_network_health";

    private final MainDashboardService mainDashboardService;

    private final ArubaService arubaService;

    private final GlpiMetricsHistoryRepository glpiRepository;

    private final TransversalKpiHistoryRepository historyRepository;

    public TransversalKpiAnalyticsService(
            MainDashboardService mainDashboardService,
            ArubaService arubaService,
            GlpiMetricsHistoryRepository glpiRepository,
            TransversalKpiHistoryRepository historyRepository
    ) {

        this.mainDashboardService = mainDashboardService;
        this.arubaService = arubaService;
        this.glpiRepository = glpiRepository;
        this.historyRepository = historyRepository;
    }

    public List<TransversalKpiCatalogItem> getTransversalKpis() {

        Map<String, Double> values =
                calculateCurrentValues();

        return definitions().values().stream()
                .map(definition -> new TransversalKpiCatalogItem(
                        definition.code(),
                        definition.name(),
                        definition.description(),
                        definition.unit(),
                        values.get(definition.code()),
                        definition.relatedKpis()
                ))
                .toList();
    }

    public AnalyticsCompareResponse compare(
            String kpiX,
            String kpiY,
            String period
    ) {

        Map<String, KpiDefinition> definitions =
                definitions();

        KpiDefinition xDefinition =
                definitions.get(kpiX);

        KpiDefinition yDefinition =
                definitions.get(kpiY);

        if (xDefinition == null
                || yDefinition == null) {

            return emptyResponse(kpiX, kpiY);
        }

        List<AnalyticsComparePoint> points =
                loadComparisonPoints(kpiX, kpiY, period);

        boolean demoData =
                false;

        if (points.size() < 2) {

            // Si todavia no existe historico
            // transversal suficiente, se
            // generan puntos demo controlados
            // para que el modulo sea usable
            // desde el primer arranque.
            points = buildDemoPoints(
                    xDefinition,
                    yDefinition,
                    period
            );
            demoData = true;
        }

        Double correlation =
                calculatePearson(points);

        AnalyticsCompareResponse response =
                new AnalyticsCompareResponse();

        response.setKpiX(kpiX);
        response.setKpiY(kpiY);
        response.setKpiXName(xDefinition.name());
        response.setKpiYName(yDefinition.name());
        response.setPoints(points);
        response.setCorrelation(correlation);
        response.setCorrelationLabel(correlationLabel(correlation));
        response.setInterpretation(
                buildInterpretation(
                        kpiX,
                        kpiY,
                        correlation,
                        xDefinition,
                        yDefinition
                )
        );
        response.setDemoData(demoData);

        return response;
    }

    public void saveCurrentSnapshot(
            LocalDateTime collectedAt
    ) {

        Map<String, Double> values =
                calculateCurrentValues();

        List<TransversalKpiHistory> histories =
                new ArrayList<>();

        for (KpiDefinition definition : definitions().values()) {

            TransversalKpiHistory history =
                    new TransversalKpiHistory();

            history.setKpiCode(definition.code());
            history.setKpiName(definition.name());
            history.setUnit(definition.unit());
            history.setValue(values.get(definition.code()));
            history.setCollectedAt(collectedAt);

            histories.add(history);
        }

        historyRepository.saveAll(histories);
    }

    private List<AnalyticsComparePoint> loadComparisonPoints(
            String kpiX,
            String kpiY,
            String period
    ) {

        LocalDateTime since =
                LocalDateTime.now().minusDays(daysFromPeriod(period));

        List<TransversalKpiHistory> rows =
                historyRepository
                        .findByKpiCodeInAndCollectedAtAfterOrderByCollectedAtAsc(
                                List.of(kpiX, kpiY),
                                since
                        );

        Map<LocalDateTime, Map<String, Double>> grouped =
                new LinkedHashMap<>();

        for (TransversalKpiHistory row : rows) {

            grouped.computeIfAbsent(
                    row.getCollectedAt(),
                    key -> new LinkedHashMap<>()
            ).put(
                    row.getKpiCode(),
                    normalizeStoredValue(row.getKpiCode(), row.getValue())
            );
        }

        return grouped.entrySet().stream()
                .filter(entry -> entry.getValue().containsKey(kpiX)
                        && entry.getValue().containsKey(kpiY))
                .map(entry -> new AnalyticsComparePoint(
                        entry.getKey(),
                        entry.getValue().get(kpiX),
                        entry.getValue().get(kpiY)
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Double> calculateCurrentValues() {

        MainDashboardSummary summary =
                mainDashboardService.getSummary();

        ArubaNetworkStatusDto arubaNetworkStatus =
                arubaService.getNetworkStatus();

        double arubaNetworkAffectation =
                arubaNetworkStatus == null
                        ? 0
                        : arubaNetworkStatus.getPercentage();

        Optional<GlpiMetricsHistory> glpiSnapshot =
                glpiRepository.findTopByOrderByCollectedAtDesc();

        int operationalBacklog =
                glpiSnapshot
                        .map(GlpiMetricsHistory::getOperationalBacklog)
                        .orElse(0);

        int slaBreachedTickets =
                glpiSnapshot
                        .map(GlpiMetricsHistory::getSlaBreachedTickets)
                        .orElse(0);

        Map<String, Double> values =
                new LinkedHashMap<>();

        double globalHealthScore =
                clamp(100 - summary.getGlobalHealthPercentage());

        double criticality =
                summary.getGlobalCriticality();

        double availability =
                clamp(100 - summary.getGlobalAvailability());

        double userImpact =
                summary.getUserImpact();

        double affectedServices =
                summary.getAffectedServicesPercent();

        double technicalDegradation =
                summary.getTechnicalDegradation();

        double backlogIndex =
                summary.getOperationalBacklog();

        double slaRisk =
                summary.getSlaRisk();

        double environmentStability =
                clamp(100 - technicalDegradation);

        double operationalPriority =
                clamp(
                        criticality * 0.35
                                + slaRisk * 0.25
                                + summary.getOperationalPressure() * 0.25
                                + userImpact * 0.15
                );

        values.put(GLOBAL_HEALTH, globalHealthScore);
        values.put(GLOBAL_CRITICALITY, criticality);
        values.put(GLOBAL_AVAILABILITY, availability);
        values.put(USER_IMPACT, userImpact);
        values.put(AFFECTED_SERVICES, affectedServices);
        values.put(TECHNICAL_DEGRADATION, technicalDegradation);
        values.put(OPERATIONAL_PRESSURE, (double) summary.getOperationalPressure());
        values.put(OPERATIONAL_BACKLOG, backlogIndex);
        values.put(SLA_RISK, slaRisk);
        values.put(ENVIRONMENT_STABILITY, environmentStability);
        values.put(OPERATIONAL_PRIORITY, operationalPriority);
        values.put(ARUBA_NETWORK_AFFECTATION, arubaNetworkAffectation);
        values.put(ARUBA_NETWORK_DEGRADATION, arubaNetworkAffectation);
        values.put(ARUBA_NETWORK_HEALTH, 100 - arubaNetworkAffectation);

        return values;
    }

    private Map<String, KpiDefinition> definitions() {

        Map<String, KpiDefinition> definitions =
                new LinkedHashMap<>();

        definitions.put(GLOBAL_HEALTH, new KpiDefinition(
                GLOBAL_HEALTH,
                "Salud global",
                "Indice numerico derivado del estado global del dashboard.",
                "%",
                List.of(
                        GLOBAL_CRITICALITY,
                        USER_IMPACT,
                        AFFECTED_SERVICES,
                        ENVIRONMENT_STABILITY,
                        GLOBAL_AVAILABILITY,
                        ARUBA_NETWORK_HEALTH,
                        ARUBA_NETWORK_AFFECTATION
                )
        ));

        definitions.put(GLOBAL_CRITICALITY, new KpiDefinition(
                GLOBAL_CRITICALITY,
                "Criticidad global",
                "Nivel agregado de riesgo operativo observado.",
                "índice 0-100",
                List.of(
                        GLOBAL_HEALTH,
                        AFFECTED_SERVICES,
                        OPERATIONAL_PRIORITY,
                        ENVIRONMENT_STABILITY,
                        ARUBA_NETWORK_AFFECTATION,
                        ARUBA_NETWORK_DEGRADATION
                )
        ));

        definitions.put(GLOBAL_AVAILABILITY, new KpiDefinition(
                GLOBAL_AVAILABILITY,
                "Disponibilidad global",
                "Estimacion agregada de disponibilidad del entorno.",
                "%",
                List.of(
                        USER_IMPACT,
                        GLOBAL_HEALTH,
                        AFFECTED_SERVICES,
                        ARUBA_NETWORK_HEALTH
                )
        ));

        definitions.put(USER_IMPACT, new KpiDefinition(
                USER_IMPACT,
                "Impacto en usuarios",
                "Impacto relativo estimado a partir de la actividad agregada.",
                "%",
                List.of(
                        GLOBAL_HEALTH,
                        GLOBAL_AVAILABILITY,
                        TECHNICAL_DEGRADATION,
                        OPERATIONAL_PRIORITY,
                        ARUBA_NETWORK_AFFECTATION
                )
        ));

        definitions.put(AFFECTED_SERVICES, new KpiDefinition(
                AFFECTED_SERVICES,
                "Servicios afectados",
                "Porcentaje de plataformas monitorizadas con alerta.",
                "%",
                List.of(
                        GLOBAL_CRITICALITY,
                        GLOBAL_HEALTH,
                        GLOBAL_AVAILABILITY,
                        TECHNICAL_DEGRADATION
                )
        ));

        definitions.put(TECHNICAL_DEGRADATION, new KpiDefinition(
                TECHNICAL_DEGRADATION,
                "Degradacion tecnica",
                "Peso de elementos tecnicos que requieren actuacion.",
                "índice 0-100",
                List.of(
                        OPERATIONAL_PRESSURE,
                        USER_IMPACT,
                        AFFECTED_SERVICES,
                        GLOBAL_CRITICALITY,
                        ARUBA_NETWORK_DEGRADATION
                )
        ));

        definitions.put(OPERATIONAL_PRESSURE, new KpiDefinition(
                OPERATIONAL_PRESSURE,
                "Presion operativa",
                "Presion agregada sobre recursos y operacion.",
                "índice 0-100",
                List.of(
                        TECHNICAL_DEGRADATION,
                        OPERATIONAL_BACKLOG,
                        SLA_RISK,
                        ARUBA_NETWORK_AFFECTATION
                )
        ));

        definitions.put(OPERATIONAL_BACKLOG, new KpiDefinition(
                OPERATIONAL_BACKLOG,
                "Backlog operativo",
                "Indice normalizado del volumen de trabajo GLPI pendiente.",
                "índice 0-100",
                List.of(
                        SLA_RISK,
                        OPERATIONAL_PRESSURE,
                        OPERATIONAL_PRIORITY
                )
        ));

        definitions.put(SLA_RISK, new KpiDefinition(
                SLA_RISK,
                "Riesgo de SLA",
                "Riesgo derivado de tickets con SLA vencido.",
                "índice 0-100",
                List.of(
                        OPERATIONAL_BACKLOG,
                        OPERATIONAL_PRESSURE,
                        OPERATIONAL_PRIORITY
                )
        ));

        definitions.put(ENVIRONMENT_STABILITY, new KpiDefinition(
                ENVIRONMENT_STABILITY,
                "Estabilidad del entorno",
                "Indicador inverso al riesgo y alertas activas.",
                "%",
                List.of(
                        GLOBAL_HEALTH,
                        GLOBAL_CRITICALITY,
                        GLOBAL_AVAILABILITY,
                        ARUBA_NETWORK_HEALTH
                )
        ));

        definitions.put(OPERATIONAL_PRIORITY, new KpiDefinition(
                OPERATIONAL_PRIORITY,
                "Prioridad operativa",
                "Prioridad de actuacion segun acciones, seguridad y SLA.",
                "índice 0-100",
                List.of(
                        GLOBAL_CRITICALITY,
                        USER_IMPACT,
                        OPERATIONAL_BACKLOG,
                        SLA_RISK,
                        ARUBA_NETWORK_AFFECTATION
                )
        ));

        definitions.put(ARUBA_NETWORK_AFFECTATION, new KpiDefinition(
                ARUBA_NETWORK_AFFECTATION,
                "Afectacion de red Aruba",
                "Porcentaje normalizado de riesgo o afectacion de la red Aruba.",
                "%",
                List.of(
                        OPERATIONAL_PRESSURE,
                        GLOBAL_CRITICALITY,
                        USER_IMPACT,
                        GLOBAL_HEALTH,
                        ARUBA_NETWORK_HEALTH,
                        ARUBA_NETWORK_DEGRADATION
                )
        ));

        definitions.put(ARUBA_NETWORK_DEGRADATION, new KpiDefinition(
                ARUBA_NETWORK_DEGRADATION,
                "Degradacion de red Aruba",
                "Indice de degradacion tecnica especifico de Aruba.",
                "indice 0-100",
                List.of(
                        TECHNICAL_DEGRADATION,
                        OPERATIONAL_PRESSURE,
                        GLOBAL_CRITICALITY,
                        ARUBA_NETWORK_AFFECTATION
                )
        ));

        definitions.put(ARUBA_NETWORK_HEALTH, new KpiDefinition(
                ARUBA_NETWORK_HEALTH,
                "Salud de red Aruba",
                "Indicador inverso a la afectacion de red Aruba.",
                "%",
                List.of(
                        GLOBAL_HEALTH,
                        ENVIRONMENT_STABILITY,
                        GLOBAL_AVAILABILITY,
                        ARUBA_NETWORK_AFFECTATION
                )
        ));

        return definitions;
    }

    private AnalyticsCompareResponse emptyResponse(
            String kpiX,
            String kpiY
    ) {

        AnalyticsCompareResponse response =
                new AnalyticsCompareResponse();

        response.setKpiX(kpiX);
        response.setKpiY(kpiY);
        response.setPoints(List.of());
        response.setInterpretation(
                "No hay datos suficientes para generar la comparacion seleccionada."
        );

        return response;
    }

    private List<AnalyticsComparePoint> buildDemoPoints(
            KpiDefinition xDefinition,
            KpiDefinition yDefinition,
            String period
    ) {

        Map<String, Double> currentValues =
                calculateCurrentValues();

        double baseX =
                currentValues.getOrDefault(xDefinition.code(), 50.0);

        double baseY =
                currentValues.getOrDefault(yDefinition.code(), 50.0);

        Random random =
                new Random((xDefinition.code() + yDefinition.code()).hashCode());

        int days =
                daysFromPeriod(period);

        double relation =
                relationDirection(xDefinition.code(), yDefinition.code());

        List<AnalyticsComparePoint> points =
                new ArrayList<>();

        for (int index = 11; index >= 0; index--) {

            double variation =
                    (random.nextDouble() - 0.5) * 30;

            double x =
                    normalizeForUnit(
                            xDefinition.unit(),
                            baseX + variation
                    );

            double y =
                    normalizeForUnit(
                            yDefinition.unit(),
                            baseY + variation * relation
                                    + (random.nextDouble() - 0.5) * 16
                    );

            points.add(new AnalyticsComparePoint(
                    LocalDateTime.now().minusDays(
                            Math.max(1, days / 12) * index
                    ),
                    round(x),
                    round(y)
            ));
        }

        return points;
    }

    private double relationDirection(
            String kpiX,
            String kpiY
    ) {

        if ((GLOBAL_HEALTH.equals(kpiX)
                && GLOBAL_CRITICALITY.equals(kpiY))
                || (GLOBAL_CRITICALITY.equals(kpiX)
                && GLOBAL_HEALTH.equals(kpiY))
                || (ARUBA_NETWORK_AFFECTATION.equals(kpiX)
                && ARUBA_NETWORK_HEALTH.equals(kpiY))
                || (ARUBA_NETWORK_HEALTH.equals(kpiX)
                && ARUBA_NETWORK_AFFECTATION.equals(kpiY))
                || (ENVIRONMENT_STABILITY.equals(kpiX)
                && GLOBAL_CRITICALITY.equals(kpiY))
                || (GLOBAL_CRITICALITY.equals(kpiX)
                && ENVIRONMENT_STABILITY.equals(kpiY))) {

            return -0.8;
        }

        return 0.8;
    }

    private Double calculatePearson(
            List<AnalyticsComparePoint> points
    ) {

        if (points.size() < 2) {

            return null;
        }

        double meanX =
                points.stream()
                        .mapToDouble(AnalyticsComparePoint::getX)
                        .average()
                        .orElse(0);

        double meanY =
                points.stream()
                        .mapToDouble(AnalyticsComparePoint::getY)
                        .average()
                        .orElse(0);

        double numerator =
                0;
        double denominatorX =
                0;
        double denominatorY =
                0;

        for (AnalyticsComparePoint point : points) {

            double diffX =
                    point.getX() - meanX;
            double diffY =
                    point.getY() - meanY;

            numerator += diffX * diffY;
            denominatorX += diffX * diffX;
            denominatorY += diffY * diffY;
        }

        if (denominatorX == 0
                || denominatorY == 0) {

            return null;
        }

        return round(numerator / Math.sqrt(denominatorX * denominatorY));
    }

    private String correlationLabel(Double correlation) {

        if (correlation == null) {

            return "No calculable";
        }

        if (correlation >= 0.8) {

            return "Relacion positiva fuerte";
        }

        if (correlation >= 0.5) {

            return "Relacion positiva moderada";
        }

        if (correlation >= 0.2) {

            return "Relacion positiva debil";
        }

        if (correlation > -0.2) {

            return "Sin relacion clara";
        }

        if (correlation > -0.8) {

            return "Relacion negativa moderada";
        }

        return "Relacion negativa fuerte";
    }

    private String buildInterpretation(
            String kpiX,
            String kpiY,
            Double correlation,
            KpiDefinition xDefinition,
            KpiDefinition yDefinition
    ) {

        String pair =
                kpiX + ":" + kpiY;

        if (pair.equals(GLOBAL_CRITICALITY + ":" + GLOBAL_HEALTH)
                || pair.equals(GLOBAL_HEALTH + ":" + GLOBAL_CRITICALITY)) {

            return "Esta comparacion permite comprobar si los momentos de mayor criticidad coinciden con una reduccion del indice global de salud del entorno. "
                    + trendSentence(correlation, xDefinition, yDefinition);
        }

        if (pair.equals(TECHNICAL_DEGRADATION + ":" + OPERATIONAL_PRESSURE)
                || pair.equals(OPERATIONAL_PRESSURE + ":"
                + TECHNICAL_DEGRADATION)) {

            return "Esta relacion permite analizar si los problemas tecnicos detectados generan un aumento de la carga operativa del area de soporte. "
                    + trendSentence(correlation, xDefinition, yDefinition);
        }

        if (pair.equals(OPERATIONAL_BACKLOG + ":" + SLA_RISK)
                || pair.equals(SLA_RISK + ":" + OPERATIONAL_BACKLOG)) {

            return "Esta comparacion permite observar si la acumulacion de incidencias pendientes aumenta el riesgo de incumplimiento de los tiempos de resolucion. "
                    + trendSentence(correlation, xDefinition, yDefinition);
        }

        if (pair.equals(ARUBA_NETWORK_AFFECTATION + ":"
                + GLOBAL_CRITICALITY)
                || pair.equals(GLOBAL_CRITICALITY + ":"
                + ARUBA_NETWORK_AFFECTATION)) {

            return "Esta comparacion permite observar si una mayor afectacion de la red Aruba coincide con un aumento de la criticidad global del entorno. "
                    + trendSentence(correlation, xDefinition, yDefinition);
        }

        return "Esta comparacion ayuda a explorar si ambos indicadores evolucionan de forma relacionada y si conviene analizarlos conjuntamente en la toma de decisiones. "
                + trendSentence(correlation, xDefinition, yDefinition);
    }

    private String trendSentence(
            Double correlation,
            KpiDefinition xDefinition,
            KpiDefinition yDefinition
    ) {

        if (correlation == null) {

            return "No hay variabilidad suficiente para calcular una correlacion fiable.";
        }

        String label =
                correlationLabel(correlation).toLowerCase();

        if (correlation > 0.2) {

            return "Se observa una " + label
                    + ". En las capturas analizadas, los valores mas altos de "
                    + xDefinition.name().toLowerCase()
                    + " tienden a coincidir con valores mas altos de "
                    + yDefinition.name().toLowerCase() + ".";
        }

        if (correlation < -0.2) {

            return "Se observa una " + label
                    + ". En las capturas analizadas, los valores mas altos de "
                    + xDefinition.name().toLowerCase()
                    + " tienden a coincidir con valores mas bajos de "
                    + yDefinition.name().toLowerCase() + ".";
        }

        return "No se observa una relacion clara entre ambos indicadores en las capturas analizadas.";
    }

    private int daysFromPeriod(String period) {

        if ("7d".equalsIgnoreCase(period)) {

            return 7;
        }

        if ("90d".equalsIgnoreCase(period)) {

            return 90;
        }

        return 30;
    }

    private double healthScore(String status) {

        if ("GREEN".equalsIgnoreCase(status)) {

            return 100;
        }

        if ("YELLOW".equalsIgnoreCase(status)) {

            return 60;
        }

        if ("RED".equalsIgnoreCase(status)) {

            return 20;
        }

        return 0;
    }

    private double normalizeForUnit(
            String unit,
            double value
    ) {

        if ("%".equals(unit)
                || "índice 0-100".equalsIgnoreCase(unit)
                || "indice 0-100".equalsIgnoreCase(unit)) {

            return clamp(value);
        }

        return Math.max(0, value);
    }

    private double normalizeStoredValue(
            String code,
            Double value
    ) {

        if (value == null) {

            return 0;
        }

        // Compatibilidad con posibles
        // snapshots anteriores a la
        // normalizacion del modulo de
        // analisis. Desde ahora todos los
        // KPIs transversales se expresan
        // en escala 0-100 o porcentaje.

        if (AFFECTED_SERVICES.equals(code)
                && value <= 4) {

            return clamp(value * 100.0 / 4.0);
        }

        if (USER_IMPACT.equals(code)
                && value > 100) {

            return clamp(value * 100.0 / 2000.0);
        }

        if (OPERATIONAL_BACKLOG.equals(code)
                && value > 100) {

            return value >= 200
                    ? 100
                    : clamp(value * 100.0 / 200.0);
        }

        return clamp(value);
    }

    private double clamp(double value) {

        if (value < 0) {

            return 0;
        }

        if (value > 100) {

            return 100;
        }

        return value;
    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }

    private record KpiDefinition(
            String code,
            String name,
            String description,
            String unit,
            List<String> relatedKpis
    ) {
    }
}
