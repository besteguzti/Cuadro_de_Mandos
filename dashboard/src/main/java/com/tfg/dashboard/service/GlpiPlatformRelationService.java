package com.tfg.dashboard.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.AnalyticsComparePoint;
import com.tfg.dashboard.dto.OperationalImpactBucketDto;
import com.tfg.dashboard.dto.TechnicalPlatformRelationDto;
import com.tfg.dashboard.model.AnalysisSnapshot;

/**
 * Calcula relaciones operativas aparentes entre GLPI y plataformas técnicas.
 *
 * Agrupa snapshots por nivel de afección, calcula co-ocurrencias y evita
 * presentar causalidad directa: solo muestra indicios operativos.
 */
@Service
public class GlpiPlatformRelationService {

        private final KpiScoringService kpiScoringService;
        private final KpiProperties kpiProperties;

        public GlpiPlatformRelationService(KpiScoringService kpiScoringService,KpiProperties kpiProperties) {
                this.kpiScoringService = kpiScoringService;
                this.kpiProperties = kpiProperties;
        }

        /**
         * Agrupa puntos en afección baja, media y alta para comparar la presión
         * media de GLPI por nivel.
         */
        public List<OperationalImpactBucketDto> buildOperationalBuckets(List<AnalyticsComparePoint> points) {

                return List.of(
                                buildOperationalBucket("Afeccion baja", points, 0, kpiProperties.getStatus().getYellowMin() - 1),
                                buildOperationalBucket("Afeccion media", points, kpiProperties.getStatus().getYellowMin(),
                                                kpiProperties.getStatus().getRedMin() - 1),
                                buildOperationalBucket("Afeccion alta", points, kpiProperties.getStatus().getRedMin(),
                                                kpiProperties.getStatus().getMax()));
        }

        public int averageGlpiForPlatformRange(List<AnalyticsComparePoint> points, int min, int max) {

                return averageGlpiPressure(points.stream()
                                .filter(point -> point.getY() >= min && point.getY() <= max)
                                .toList());
        }

        public int countPlatformRange(
                        List<AnalyticsComparePoint> points,
                        int min,
                        int max) {

                return (int) points.stream().filter(point -> point.getY() >= min && point.getY() <= max).count();
        }

        /**
         * Calcula el porcentaje de snapshots donde coinciden plataforma
         * afectada y GLPI alto.
         */
        public int highHighCooccurrencePercentage(List<AnalyticsComparePoint> points) {

                if (points.isEmpty()) {

                        return 0;
                }

                long highHigh = points.stream()
                                .filter(point -> point.getX() >= kpiProperties.getStatus().getYellowMin()
                                                && point.getY() >= kpiProperties.getStatus().getYellowMin())
                                .count();

                return clampToInt(highHigh * 100.0 / points.size());
        }

        /**
         * Calcula co-afección entre plataformas técnicas.
         */
        public List<TechnicalPlatformRelationDto> buildTechnicalRelations(List<AnalysisSnapshot> snapshots) {

                return List.of(
                                technicalRelation("Aruba", "Citrix", snapshots),
                                technicalRelation("Aruba", "Microsoft 365", snapshots),
                                technicalRelation("Citrix", "Aruba", snapshots),
                                technicalRelation("Citrix", "Microsoft 365", snapshots),
                                technicalRelation("Microsoft 365", "Aruba", snapshots),
                                technicalRelation("Microsoft 365", "Citrix", snapshots));
        }

        private OperationalImpactBucketDto buildOperationalBucket(String label,List<AnalyticsComparePoint> points,int min,int max) {

                List<AnalyticsComparePoint> bucketPoints = points.stream().filter(point -> point.getY() >= min && point.getY() <= max).toList();
                OperationalImpactBucketDto bucket = new OperationalImpactBucketDto();

                bucket.setLevel(label);
                bucket.setMin(min);
                bucket.setMax(max);
                bucket.setSnapshots(bucketPoints.size());
                bucket.setAverageGlpiPressure(averageGlpiPressure(bucketPoints));
                bucket.setAverageGlpiPressureStatus(bucketPoints.isEmpty()
                                                ? KpiScoringService.NO_DATA
                                                : kpiScoringService.statusFromAffection(bucket.getAverageGlpiPressure()));
                bucket.setHighGlpiPercentage(highGlpiPercentage(bucketPoints));

                return bucket;
        }

        private int averageGlpiPressure(List<AnalyticsComparePoint> points) {

                if (points.isEmpty()) {

                        return 0;
                }

                return clampToInt(points.stream()
                                                .mapToDouble(AnalyticsComparePoint::getX)
                                                .average()
                                                .orElse(0));
        }

        private int highGlpiPercentage(List<AnalyticsComparePoint> points) {

                if (points.isEmpty()) {

                        return 0;
                }

                long highGlpi = points.stream()
                                .filter(point -> point.getX() >= kpiProperties.getStatus().getYellowMin())
                                .count();

                return clampToInt(highGlpi * 100.0 / points.size());
        }

        private TechnicalPlatformRelationDto technicalRelation(
                        String origin,
                        String target,
                        List<AnalysisSnapshot> snapshots) {

                List<AnalysisSnapshot> originAffected = snapshots.stream()
                                .filter(snapshot -> platformValue(snapshot, origin) >= kpiProperties.getStatus().getYellowMin())
                                .toList();

                List<AnalysisSnapshot> originNormal = snapshots.stream()
                                .filter(snapshot -> platformValue(snapshot, origin) < kpiProperties.getStatus().getYellowMin())
                                .toList();

                Integer cooccurrence = null;

                if (!originAffected.isEmpty()) {

                        long targetAlsoAffected = originAffected.stream()
                                        .filter(snapshot -> platformValue(
                                                        snapshot,
                                                        target) >= kpiProperties.getStatus().getYellowMin())
                                        .count();

                        cooccurrence = clampToInt(targetAlsoAffected * 100.0
                                        / originAffected.size());
                }

                Integer increase = null;

                if (!originAffected.isEmpty() && !originNormal.isEmpty()) {

                        increase = clampToInt(averagePlatform(originAffected, target) - averagePlatform(originNormal, target));
                }

                TechnicalPlatformRelationDto relation = new TechnicalPlatformRelationDto();

                relation.setOrigin(origin);
                relation.setTarget(target);
                relation.setRelation(origin + " -> " + target);
                relation.setCooccurrencePercentage(cooccurrence);
                relation.setAverageIncrease(increase);
                relation.setReading(kpiScoringService.relationReading(cooccurrence, increase));
                relation.setReadingStatus(relationStatus(relation.getReading()));
                relation.setOriginAffectedSnapshots(originAffected.size());
                relation.setOriginNormalSnapshots(originNormal.size());

                return relation;
        }

        private String relationStatus(String reading) {

                if ("Alta".equalsIgnoreCase(reading)) {

                        return KpiScoringService.RED;
                }

                if ("Moderada".equalsIgnoreCase(reading)) {

                        return KpiScoringService.YELLOW;
                }

                if ("Baja".equalsIgnoreCase(reading)) {

                        return KpiScoringService.GREEN;
                }

                return KpiScoringService.NO_DATA;
        }

        private double averagePlatform(List<AnalysisSnapshot> snapshots,String platform) {

                return snapshots.stream()
                                .mapToDouble(snapshot -> platformValue(snapshot, platform))
                                .average()
                                .orElse(0);
        }

        private double platformValue(AnalysisSnapshot snapshot,String platform) {

                if ("Citrix".equalsIgnoreCase(platform)) {

                        return safeInt(snapshot.getCitrixHealth());
                }

                if ("Microsoft 365".equalsIgnoreCase(platform)) {

                        return safeInt(snapshot.getMicrosoft365Health());
                }

                return safeInt(snapshot.getArubaHealth());
        }

        private int clampToInt(double value) {

                return (int) Math.round(clamp(value));
        }

        private double clamp(double value) {

                if (value < 0) {

                        return 0;
                }

                if (value > kpiProperties.getStatus().getMax()) {

                        return kpiProperties.getStatus().getMax();
                }

                return value;
        }

        private int safeInt(Integer value) {

                return value != null
                                ? value
                                : 0;
        }
}
