package com.tfg.dashboard.dto;

import java.time.LocalDateTime;

/**
 * Punto de una relación específica entre indicadores.
 *
 * Mantiene timestamp, valores X/Y y si la muestra procede de un escenario demo.
 */
public class SpecificKpiRelationPointDto {

    private LocalDateTime timestamp;
    private Double x;
    private Double y;
    private int samplesUsed;
    private boolean generatedScenario;

    public SpecificKpiRelationPointDto() {
    }

    public SpecificKpiRelationPointDto(
            LocalDateTime timestamp,
            Double x,
            Double y,
            int samplesUsed,
            boolean generatedScenario) {

        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.samplesUsed = samplesUsed;
        this.generatedScenario = generatedScenario;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public int getSamplesUsed() {
        return samplesUsed;
    }

    public boolean isGeneratedScenario() {
        return generatedScenario;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public void setSamplesUsed(int samplesUsed) {
        this.samplesUsed = samplesUsed;
    }

    public void setGeneratedScenario(boolean generatedScenario) {
        this.generatedScenario = generatedScenario;
    }
}
