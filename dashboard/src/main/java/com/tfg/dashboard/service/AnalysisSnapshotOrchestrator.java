package com.tfg.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.AnalyticsComparePoint;
import com.tfg.dashboard.dto.KpiResultDto;
import com.tfg.dashboard.dto.OperationalImpactAnalysisResponse;
import com.tfg.dashboard.dto.OperationalImpactBucketDto;
import com.tfg.dashboard.dto.TechnicalTimelinePointDto;
import com.tfg.dashboard.dto.summary.ArubaSummary;
import com.tfg.dashboard.model.AnalysisSnapshot;
import com.tfg.dashboard.model.CitrixMetricsHistory;
import com.tfg.dashboard.model.GlpiMetricsHistory;
import com.tfg.dashboard.model.Microsoft365MetricsHistory;
import com.tfg.dashboard.dto.summary.MainDashboardSummary;
import com.tfg.dashboard.repository.AnalysisSnapshotRepository;
import com.tfg.dashboard.repository.CitrixMetricsHistoryRepository;
import com.tfg.dashboard.repository.GlpiMetricsHistoryRepository;
import com.tfg.dashboard.repository.Microsoft365MetricsHistoryRepository;

/**
 * Orquesta los datos del panel de análisis exploratorio.
 *
 * Combina snapshots persistidos, escenarios demo identificados, relaciones
 * GLPI-plataforma, degradación técnica, impacto en usuarios y evolución
 * temporal sin cambiar los contratos JSON que consume React.
 */
@Service
public class AnalysisSnapshotOrchestrator {

        private static final int NO_DATA_COUNT = 0;
        private static final int NO_COMPARISON_INCREASE = 0;
        private static final int RANGE_BOUNDARY_OFFSET = 1;
        private static final double APPARENT_RELATION_INCREASE_WEIGHT = 0.5;
        private static final double APPARENT_RELATION_COOCCURRENCE_WEIGHT = 0.5;
        private static final double ESTIMATED_IMPACT_GLPI_WEIGHT = 0.5;
        private static final double ESTIMATED_IMPACT_TECHNICAL_WEIGHT = 0.5;
        private static final int MEANINGFUL_GLPI_INCREASE_POINTS = 10;
        private static final int STRONG_COOCCURRENCE_PERCENT = 50;

        private final AnalysisSnapshotRepository analysisSnapshotRepository;
        private final AnalysisSnapshotService analysisSnapshotService;
        private final AnalysisDemoScenarioService demoScenarioService;
        private final TransversalKpiHistoryService historyService;
        private final MainDashboardService mainDashboardService;
        private final KpiScoringService kpiScoringService;
        private final GlpiPlatformRelationService glpiPlatformRelationService;
        private final TechnicalImpactAnalysisService technicalImpactAnalysisService;
        private final TechnicalTimelineService technicalTimelineService;
        private final SpecificKpiRelationService specificKpiRelationService;
        private final ArubaService arubaService;
        private final CitrixMetricsHistoryRepository citrixRepository;
        private final Microsoft365MetricsHistoryRepository microsoft365Repository;
        private final GlpiMetricsHistoryRepository glpiRepository;
        private final KpiProperties kpiProperties;

        public AnalysisSnapshotOrchestrator(
                        AnalysisSnapshotRepository analysisSnapshotRepository,
                        AnalysisSnapshotService analysisSnapshotService,
                        AnalysisDemoScenarioService demoScenarioService,
                        TransversalKpiHistoryService historyService,
                        MainDashboardService mainDashboardService,
                        KpiScoringService kpiScoringService,
                        GlpiPlatformRelationService glpiPlatformRelationService,
                        TechnicalImpactAnalysisService technicalImpactAnalysisService,
                        TechnicalTimelineService technicalTimelineService,
                        SpecificKpiRelationService specificKpiRelationService,
                        ArubaService arubaService,
                        CitrixMetricsHistoryRepository citrixRepository,
                        Microsoft365MetricsHistoryRepository microsoft365Repository,
                        GlpiMetricsHistoryRepository glpiRepository,
                        KpiProperties kpiProperties) {

                this.analysisSnapshotRepository = analysisSnapshotRepository;
                this.analysisSnapshotService = analysisSnapshotService;
                this.demoScenarioService = demoScenarioService;
                this.historyService = historyService;
                this.mainDashboardService = mainDashboardService;
                this.kpiScoringService = kpiScoringService;
                this.glpiPlatformRelationService = glpiPlatformRelationService;
                this.technicalImpactAnalysisService = technicalImpactAnalysisService;
                this.technicalTimelineService = technicalTimelineService;
                this.specificKpiRelationService = specificKpiRelationService;
                this.arubaService = arubaService;
                this.citrixRepository = citrixRepository;
                this.microsoft365Repository = microsoft365Repository;
                this.glpiRepository = glpiRepository;
                this.kpiProperties = kpiProperties;
        }

        /**
         * Asegura que exista histórico suficiente para el periodo y devuelve los
         * snapshots ordenados.
         */
        public List<AnalysisSnapshot> getAnalysisSnapshots(String period) {

                demoScenarioService.ensureAnalysisSnapshots(period);
                return analysisSnapshotService.getSnapshots(period);
        }

        /**
         * Construye la respuesta principal del panel: GLPI como consecuencia
         * operativa y Aruba/Citrix/Microsoft 365 como posibles orígenes técnicos.
         */
        public OperationalImpactAnalysisResponse getGlpiPlatformRelation(
                        String platform,
                        String period) {

                demoScenarioService.ensureAnalysisSnapshots(period);

                Map<String, Double> values = historyService.calculateCurrentValues();

                int glpiPressure = safeCurrentScore(values, TransversalKpiHistoryService.GLPI_OPERATIONAL_PRESSURE);
                int arubaAffection = safeCurrentScore(values, TransversalKpiHistoryService.ARUBA_NETWORK_AFFECTATION);
                int citrixAffection = safeCurrentScore(values, TransversalKpiHistoryService.CITRIX_TECHNICAL_AFFECTION);
                int microsoft365Affection = safeCurrentScore(values,
                                TransversalKpiHistoryService.MICROSOFT365_TECHNICAL_AFFECTION);

                int arubaRelation = Math.min(glpiPressure, arubaAffection);
                int citrixRelation = Math.min(glpiPressure, citrixAffection);
                int microsoft365Relation = Math.min(glpiPressure, microsoft365Affection);
                String normalizedPlatform = normalizePlatform(platform);
                int selectedAffection = selectedPlatformAffection(
                                normalizedPlatform,
                                arubaAffection,
                                citrixAffection,
                                microsoft365Affection);
                List<AnalyticsComparePoint> points = loadOperationalImpactPoints(normalizedPlatform, period);
                boolean demoData = points.stream()
                                .anyMatch(point -> analysisSnapshotService.isGeneratedScenario(point.getTimestamp()));
                List<OperationalImpactBucketDto> buckets = glpiPlatformRelationService.buildOperationalBuckets(points);
                List<AnalysisSnapshot> technicalSnapshots = getAnalysisSnapshots(period);
                int averageGlpiNormal = glpiPlatformRelationService.averageGlpiForPlatformRange(
                                points,
                                minimumAffection(),
                                normalAffectionMax());
                int averageGlpiAffected = glpiPlatformRelationService.averageGlpiForPlatformRange(
                                points,
                                affectedAffectionMin(),
                                maximumAffection());
                int normalSnapshots = glpiPlatformRelationService.countPlatformRange(
                                points,
                                minimumAffection(),
                                normalAffectionMax());
                int affectedSnapshots = glpiPlatformRelationService.countPlatformRange(
                                points,
                                affectedAffectionMin(),
                                maximumAffection());
                int averageGlpiIncrease = normalSnapshots == NO_DATA_COUNT || affectedSnapshots == NO_DATA_COUNT
                                ? NO_COMPARISON_INCREASE
                                : averageGlpiAffected - averageGlpiNormal;
                int highHighCooccurrence = glpiPlatformRelationService.highHighCooccurrencePercentage(points);
                int apparentOperationalRelation = kpiScoringService.clampToInt(
                                Math.max(
                                                NO_COMPARISON_INCREASE,
                                                averageGlpiIncrease)
                                                * APPARENT_RELATION_INCREASE_WEIGHT
                                                + highHighCooccurrence
                                                                * APPARENT_RELATION_COOCCURRENCE_WEIGHT);
                int highestTechnicalAffection = Math.max(arubaAffection,
                                Math.max(citrixAffection, microsoft365Affection));
                int technicalDegradation = technicalImpactAnalysisService.calculateTechnicalDegradation(
                                arubaAffection,
                                citrixAffection,
                                microsoft365Affection);
                int userImpact = technicalImpactAnalysisService.calculateUserImpact(
                                arubaAffection,
                                citrixAffection,
                                microsoft365Affection,
                                glpiPressure);

                OperationalImpactAnalysisResponse response = new OperationalImpactAnalysisResponse();

                response.setGlpiOperationalPressure(glpiPressure);
                response.setGlpiOperationalPressureColor(kpiScoringService.statusFromAffection(glpiPressure));
                response.setAverageGlpiIncreaseStatus(
                                kpiScoringService.statusFromAffection(
                                                Math.max(NO_COMPARISON_INCREASE, averageGlpiIncrease)));
                response.setHighHighCooccurrenceStatus(
                                kpiScoringService.statusFromAffection(highHighCooccurrence));
                response.setApparentOperationalRelationStatus(
                                kpiScoringService.statusFromAffection(apparentOperationalRelation));
                response.setArubaAffection(arubaAffection);
                response.setCitrixAffection(citrixAffection);
                response.setMicrosoft365Affection(microsoft365Affection);
                response.setArubaGlpiRelation(arubaRelation);
                response.setArubaGlpiRelationStatus(kpiScoringService.statusFromAffection(arubaRelation));
                response.setCitrixGlpiRelation(citrixRelation);
                response.setCitrixGlpiRelationStatus(kpiScoringService.statusFromAffection(citrixRelation));
                response.setMicrosoft365GlpiRelation(microsoft365Relation);
                response.setMicrosoft365GlpiRelationStatus(
                                kpiScoringService.statusFromAffection(microsoft365Relation));
                response.setHighestRelatedPlatform(
                                highestRelatedPlatform(arubaRelation, citrixRelation, microsoft365Relation));
                response.setHighestRelationValue(
                                Math.max(arubaRelation, Math.max(citrixRelation, microsoft365Relation)));
                response.setHighestRelationStatus(
                                kpiScoringService.statusFromAffection(response.getHighestRelationValue()));
                response.setEstimatedOperationalImpact(
                                kpiScoringService.clampToInt(
                                                glpiPressure * ESTIMATED_IMPACT_GLPI_WEIGHT
                                                                + highestTechnicalAffection
                                                                                * ESTIMATED_IMPACT_TECHNICAL_WEIGHT));
                response.setEstimatedOperationalImpactStatus(
                                kpiScoringService.statusFromAffection(response.getEstimatedOperationalImpact()));
                response.setAverageGlpiWhenPlatformNormal(averageGlpiNormal);
                response.setAverageGlpiWhenPlatformAffected(averageGlpiAffected);
                response.setNormalSnapshots(normalSnapshots);
                response.setAffectedSnapshots(affectedSnapshots);
                response.setAverageGlpiIncreaseWhenAffected(averageGlpiIncrease);
                response.setHighHighCooccurrencePercentage(highHighCooccurrence);
                response.setApparentOperationalRelation(apparentOperationalRelation);
                response.setSelectedPlatform(normalizedPlatform);
                response.setSelectedPlatformAffection(selectedAffection);
                response.setSelectedPlatformAffectionStatus(
                                kpiScoringService.statusFromAffection(selectedAffection));
                response.setPoints(points);
                response.setBuckets(buckets);
                response.setTechnicalRelations(
                                glpiPlatformRelationService.buildTechnicalRelations(technicalSnapshots));
                response.setTechnicalDegradation(technicalDegradation);
                response.setTechnicalDegradationStatus(
                                kpiScoringService.statusFromAffection(technicalDegradation));
                response.setUserImpact(userImpact);
                response.setUserImpactStatus(kpiScoringService.statusFromAffection(userImpact));
                response.setTechnicalOperationalConversion(Math.min(technicalDegradation, userImpact));
                response.setTechnicalOperationalConversionStatus(
                                kpiScoringService.statusFromAffection(
                                                response.getTechnicalOperationalConversion()));
                response.setTechnicalImpactPoints(
                                technicalImpactAnalysisService.buildTechnicalImpactPoints(technicalSnapshots));
                response.setTechnicalTimeline(
                                technicalTimelineService.buildPlatformEvolution(technicalSnapshots));
                response.setSpecificKpiRelations(
                                specificKpiRelationService.buildRelations(technicalSnapshots));
                response.setDemoData(demoData);
                response.setInterpretation(
                                operationalInterpretation(
                                                response.getHighestRelatedPlatform(),
                                                response.getHighestRelationValue(),
                                                response.getEstimatedOperationalImpact(),
                                                averageGlpiIncrease,
                                                highHighCooccurrence,
                                                normalSnapshots,
                                                affectedSnapshots));
                response.setTechnicalImpactInterpretation(
                                technicalImpactAnalysisService.technicalImpactInterpretation(
                                                technicalDegradation,
                                                userImpact));
                response.setKpis(
                                buildOperationalImpactKpis(
                                                response,
                                                glpiPressure,
                                                selectedAffection,
                                                normalizedPlatform));

                return response;
        }

        public List<AnalyticsComparePoint> getTechnicalDegradationImpact(String period) {

                demoScenarioService.ensureAnalysisSnapshots(period);
                return technicalImpactAnalysisService.buildTechnicalImpactPoints(
                                getAnalysisSnapshots(period));
        }

        public List<TechnicalTimelinePointDto> getPlatformEvolution(String period) {

                demoScenarioService.ensureAnalysisSnapshots(period);
                return technicalTimelineService.buildPlatformEvolution(getAnalysisSnapshots(period));
        }

        /**
         * Persiste una captura real de análisis calculada a partir del estado
         * actual del dashboard.
         */
        public void saveAnalysisSnapshot(LocalDateTime collectedAt) {

                analysisSnapshotRepository.save(buildAnalysisSnapshot(collectedAt, false));
        }

        /**
         * Construye el snapshot persistido. Si falta un valor numérico actual,
         * se usa el mínimo solo para mantener el contrato de la respuesta, no
         * como dato real en el histórico transversal.
         */
        private AnalysisSnapshot buildAnalysisSnapshot(
                        LocalDateTime collectedAt,
                        boolean generatedScenario) {

                Map<String, Double> values = historyService.calculateCurrentValues();
                MainDashboardSummary summary = mainDashboardService.getSummary();
                ArubaSummary arubaSummary = arubaService.getSummary();
                CitrixMetricsHistory citrixSnapshot =
                                citrixRepository.findTopByOrderByCollectedAtDesc().orElse(null);
                Microsoft365MetricsHistory microsoft365Snapshot =
                                microsoft365Repository.findTopByOrderByCollectedAtDesc().orElse(null);
                GlpiMetricsHistory glpiSnapshot =
                                glpiRepository.findTopByOrderByCollectedAtDesc().orElse(null);
                AnalysisSnapshot snapshot = new AnalysisSnapshot();

                int aruba = safeCurrentScore(values, TransversalKpiHistoryService.ARUBA_NETWORK_AFFECTATION);
                int citrix = safeCurrentScore(values, TransversalKpiHistoryService.CITRIX_TECHNICAL_AFFECTION);
                int microsoft365 = safeCurrentScore(values,
                                TransversalKpiHistoryService.MICROSOFT365_TECHNICAL_AFFECTION);
                int glpi = kpiScoringService.clampToInt(
                                safeCurrentScore(
                                                values,
                                                TransversalKpiHistoryService.GLPI_OPERATIONAL_PRESSURE));

                snapshot.setTimestamp(collectedAt);
                snapshot.setArubaHealth(aruba);
                snapshot.setCitrixHealth(citrix);
                snapshot.setMicrosoft365Health(microsoft365);
                snapshot.setGlpiHealth(glpi);
                snapshot.setGlpiOperationalPressure(
                                safeCurrentScore(
                                                values,
                                                TransversalKpiHistoryService.GLPI_OPERATIONAL_PRESSURE));
                snapshot.setTechnicalDegradation(summary.getTechnicalDegradation());
                snapshot.setUserImpact(summary.getUserImpact());
                snapshot.setGlobalStatus(summary.getGlobalHealthPercentage());
                snapshot.setArubaWifiClients(
                                "NO_DATA".equalsIgnoreCase(arubaSummary.getDataStatus())
                                                ? null
                                                : arubaSummary.getTotalWifiClients());
                snapshot.setCitrixAverageLogonDurationSeconds(
                                citrixSnapshot == null
                                                ? null
                                                : citrixSnapshot.getAverageLogonDurationSeconds());
                snapshot.setCitrixActiveSessions(
                                citrixSnapshot == null ? null : citrixSnapshot.getActiveSessions());
                snapshot.setCitrixFailedLogons(
                                citrixSnapshot == null ? null : citrixSnapshot.getFailedLogons());
                snapshot.setGlpiOpenTickets(
                                glpiSnapshot == null ? null : glpiSnapshot.getOpenTickets());
                snapshot.setArubaOpenTickets(
                                glpiSnapshot == null ? null : glpiSnapshot.getArubaOpenTickets());
                snapshot.setCitrixOpenTickets(
                                glpiSnapshot == null ? null : glpiSnapshot.getCitrixOpenTickets());
                snapshot.setMicrosoft365OpenTickets(
                                glpiSnapshot == null ? null : glpiSnapshot.getMicrosoft365OpenTickets());
                snapshot.setMicrosoft365NonCompliantDevices(
                                microsoft365Snapshot == null
                                                ? null
                                                : microsoft365Snapshot.getNonCompliantDevices());
                snapshot.setAffectedServicesPercent(summary.getAffectedServicesPercent());
                snapshot.setArubaStatus(kpiScoringService.statusFromAffection(aruba));
                snapshot.setCitrixStatus(kpiScoringService.statusFromAffection(citrix));
                snapshot.setMicrosoft365Status(kpiScoringService.statusFromAffection(microsoft365));
                snapshot.setGlpiStatus(kpiScoringService.statusFromAffection(glpi));
                snapshot.setGeneratedScenario(generatedScenario);

                return snapshot;
        }

        /**
         * Añade KPIs explicativos a la respuesta de análisis para mantener una
         * estructura homogénea con el dashboard principal.
         */
        private List<KpiResultDto> buildOperationalImpactKpis(
                        OperationalImpactAnalysisResponse response,
                        int glpiPressure,
                        int selectedAffection,
                        String selectedPlatform) {

                LocalDateTime timestamp = LocalDateTime.now();

                List<KpiResultDto> glpiAndPlatform = List.of(
                                kpiScoringService.component(
                                                TransversalKpiHistoryService.GLPI_OPERATIONAL_PRESSURE,
                                                "Presion operativa GLPI",
                                                glpiPressure,
                                                response.getGlpiOperationalPressureColor(),
                                                glpiPressure),
                                kpiScoringService.component(
                                                platformCode(selectedPlatform),
                                                "Afeccion " + selectedPlatform,
                                                selectedAffection,
                                                response.getSelectedPlatformAffectionStatus(),
                                                selectedAffection));

                return List.of(
                                kpiScoringService.kpi(
                                                "glpi_operational_pressure",
                                                "Presion operativa GLPI",
                                                response.getGlpiOperationalPressure(),
                                                response.getGlpiOperationalPressureColor(),
                                                "Consecuencia operativa calculada con tickets y capacidad de cierre.",
                                                glpiOperationalPressureFormula(),
                                                timestamp,
                                                null,
                                                List.of()),
                                kpiScoringService.kpi(
                                                "aruba_glpi_relation",
                                                "Relacion Aruba-GLPI",
                                                response.getArubaGlpiRelation(),
                                                response.getArubaGlpiRelationStatus(),
                                                "Relacion aparente entre afeccion Aruba y presion GLPI.",
                                                "min(Presion operativa GLPI, Afeccion Aruba)",
                                                timestamp,
                                                null,
                                                glpiAndPlatform),
                                kpiScoringService.kpi(
                                                "citrix_glpi_relation",
                                                "Relacion Citrix-GLPI",
                                                response.getCitrixGlpiRelation(),
                                                response.getCitrixGlpiRelationStatus(),
                                                "Relacion aparente entre afeccion Citrix y presion GLPI.",
                                                "min(Presion operativa GLPI, Afeccion Citrix)",
                                                timestamp,
                                                null,
                                                glpiAndPlatform),
                                kpiScoringService.kpi(
                                                "microsoft365_glpi_relation",
                                                "Relacion Microsoft365-GLPI",
                                                response.getMicrosoft365GlpiRelation(),
                                                response.getMicrosoft365GlpiRelationStatus(),
                                                "Relacion aparente entre afeccion Microsoft 365 y presion GLPI.",
                                                "min(Presion operativa GLPI, Afeccion Microsoft 365)",
                                                timestamp,
                                                null,
                                                glpiAndPlatform),
                                kpiScoringService.kpi(
                                                "technical_vs_user_impact",
                                                "Degradacion tecnica vs impacto usuarios",
                                                response.getTechnicalOperationalConversion(),
                                                response.getTechnicalOperationalConversionStatus(),
                                                "Conversion de degradacion tecnica en impacto observado sobre usuarios.",
                                                "min(Degradacion tecnica, Impacto en usuarios)",
                                                timestamp,
                                                null,
                                                List.of(
                                                                kpiScoringService.component(
                                                                                "technical_degradation",
                                                                                "Degradacion tecnica",
                                                                                response.getTechnicalDegradation(),
                                                                                response.getTechnicalDegradationStatus(),
                                                                                response.getTechnicalDegradation()),
                                                                kpiScoringService.component(
                                                                                "user_impact",
                                                                                "Impacto en usuarios",
                                                                                response.getUserImpact(),
                                                                                response.getUserImpactStatus(),
                                                                                response.getUserImpact()))));
        }

        private List<AnalyticsComparePoint> loadOperationalImpactPoints(
                        String platform,
                        String period) {

                return getAnalysisSnapshots(period).stream()
                                .map(snapshot -> new AnalyticsComparePoint(
                                                snapshot.getTimestamp(),
                                                (double) safeInt(snapshot.getGlpiOperationalPressure()),
                                                platformSnapshotValue(snapshot, platform)))
                                .toList();
        }

        private double platformSnapshotValue(AnalysisSnapshot snapshot, String platform) {

                if ("Citrix".equalsIgnoreCase(platform)) {
                        return safeInt(snapshot.getCitrixHealth());
                }

                if ("Microsoft 365".equalsIgnoreCase(platform)) {
                        return safeInt(snapshot.getMicrosoft365Health());
                }

                return safeInt(snapshot.getArubaHealth());
        }

        private String normalizePlatform(String platform) {

                if ("citrix".equalsIgnoreCase(platform)) {
                        return "Citrix";
                }

                if ("microsoft365".equalsIgnoreCase(platform)
                                || "microsoft-365".equalsIgnoreCase(platform)) {

                        return "Microsoft 365";
                }

                return "Aruba";
        }

        private String platformCode(String platform) {

                if ("Citrix".equalsIgnoreCase(platform)) {
                        return TransversalKpiHistoryService.CITRIX_TECHNICAL_AFFECTION;
                }

                if ("Microsoft 365".equalsIgnoreCase(platform)) {
                        return TransversalKpiHistoryService.MICROSOFT365_TECHNICAL_AFFECTION;
                }

                return TransversalKpiHistoryService.ARUBA_NETWORK_AFFECTATION;
        }

        private int selectedPlatformAffection(
                        String platform,
                        int arubaAffection,
                        int citrixAffection,
                        int microsoft365Affection) {

                if ("Citrix".equalsIgnoreCase(platform)) {
                        return citrixAffection;
                }

                if ("Microsoft 365".equalsIgnoreCase(platform)) {
                        return microsoft365Affection;
                }

                return arubaAffection;
        }

        private String highestRelatedPlatform(
                        int arubaRelation,
                        int citrixRelation,
                        int microsoft365Relation) {

                if (citrixRelation >= arubaRelation && citrixRelation >= microsoft365Relation) {
                        return "Citrix";
                }

                if (microsoft365Relation >= arubaRelation
                                && microsoft365Relation >= citrixRelation) {
                        return "Microsoft 365";
                }

                return "Aruba";
        }

        /**
         * Genera una lectura prudente: habla de relación aparente y
         * co-ocurrencia, nunca de causalidad directa.
         */
        private String operationalInterpretation(
                        String platform,
                        int relation,
                        int estimatedImpact,
                        int averageGlpiIncrease,
                        int highHighCooccurrence,
                        int normalSnapshots,
                        int affectedSnapshots) {

                String base = "El analisis no busca demostrar causalidad directa, sino comprobar si la presion operativa de GLPI tiende a ser mayor cuando la plataforma tecnica seleccionada presenta mayor afeccion. ";

                if (normalSnapshots == NO_DATA_COUNT) {

                        return base
                                        + "No hay capturas suficientes con la plataforma en estado normal para calcular el incremento respecto a una situacion sin afeccion. Sin embargo, la co-ocurrencia alta-alta del "
                                        + highHighCooccurrence
                                        + "% indica en que porcentaje coinciden la afeccion tecnica y la presion operativa de GLPI.";
                }

                if (affectedSnapshots == NO_DATA_COUNT) {

                        return base
                                        + "No hay capturas suficientes con la plataforma afectada para calcular una comparacion fiable. La co-ocurrencia alta-alta disponible es del "
                                        + highHighCooccurrence
                                        + "%.";
                }

                if (averageGlpiIncrease > MEANINGFUL_GLPI_INCREASE_POINTS
                                && highHighCooccurrence >= STRONG_COOCCURRENCE_PERCENT) {

                        return base
                                        + "En las muestras analizadas, GLPI aumenta de media "
                                        + averageGlpiIncrease
                                        + " puntos cuando la plataforma esta afectada y la co-ocurrencia alta-alta es del "
                                        + highHighCooccurrence
                                        + "%. El indicio operativo mas claro apunta a "
                                        + platform
                                        + ". Impacto estimado: "
                                        + estimatedImpact + "%.";
                }

                if (averageGlpiIncrease > NO_COMPARISON_INCREASE
                                || relation >= affectedAffectionMin()) {

                        return base
                                        + "Se observa una relacion aparente moderada con "
                                        + platform
                                        + ": GLPI sube de media "
                                        + averageGlpiIncrease
                                        + " puntos cuando la plataforma pasa a estado afectado. La co-ocurrencia alta-alta es del "
                                        + highHighCooccurrence + "%.";
                }

                return base
                                + "No hay evidencias suficientes para afirmar un incremento fiable. La lectura debe apoyarse en la co-ocurrencia y en el volumen de snapshots disponibles.";
        }

        private int safeInt(Integer value) {

                return value != null ? value : minimumAffection();
        }

        private int safeCurrentScore(Map<String, Double> values, String code) {

                Double value = values.get(code);

                // El contrato del panel de analisis usa numeros. Si no hay
                // snapshot actual para una plataforma se usa el minimo solo
                // para no romper la respuesta; el historico transversal no
                // persiste ese falso 0 como dato real.
                return value != null
                                ? kpiScoringService.clampToInt(value)
                                : minimumAffection();
        }

        private int minimumAffection() {

                return kpiProperties.getAffection().getGreen();
        }

        private int normalAffectionMax() {

                return kpiProperties.getStatus().getYellowMin() - RANGE_BOUNDARY_OFFSET;
        }

        private int affectedAffectionMin() {

                return kpiProperties.getStatus().getYellowMin();
        }

        private int maximumAffection() {

                return kpiProperties.getStatus().getMax();
        }

        private String glpiOperationalPressureFormula() {

                KpiProperties.GlpiPressureWeights weights = kpiProperties.getWeights().getGlpiOperationalPressure();

                return "Tickets abiertos*"
                                + kpiProperties.formatWeight(weights.getOpenTickets())
                                + " + cierre diario*"
                                + kpiProperties.formatWeight(weights.getClosedTodayPercent())
                                + " + tickets criticos*"
                                + kpiProperties.formatWeight(weights.getCriticalTickets())
                                + " + cierre semanal*"
                                + kpiProperties.formatWeight(weights.getClosedWeekPercent());
        }
}
