package com.tfg.dashboard.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tfg.dashboard.dto.TechnicalTimelinePointDto;
import com.tfg.dashboard.model.AnalysisSnapshot;

/**
 * Prepara la evolución temporal conjunta de las plataformas técnicas.
 *
 * Convierte snapshots de análisis en puntos de serie temporal para Aruba,
 * Citrix y Microsoft 365.
 */
@Service
public class TechnicalTimelineService {

    /**
     * Genera puntos temporales en escala de afección 0-100.
     */
    public List<TechnicalTimelinePointDto> buildPlatformEvolution(
            List<AnalysisSnapshot> snapshots
    ) {

        return snapshots.stream()
                .map(snapshot -> {

                    TechnicalTimelinePointDto point =
                            new TechnicalTimelinePointDto();

                    point.setTimestamp(snapshot.getTimestamp());
                    point.setAruba((double) safeInt(snapshot.getArubaHealth()));
                    point.setCitrix(
                            (double) safeInt(snapshot.getCitrixHealth())
                    );
                    point.setMicrosoft365(
                            (double) safeInt(snapshot.getMicrosoft365Health())
                    );

                    return point;
                })
                .toList();
    }

    private int safeInt(Integer value) {

        return value != null
                ? value
                : 0;
    }
}
