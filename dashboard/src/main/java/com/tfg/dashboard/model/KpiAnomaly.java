package com.tfg.dashboard.model;

public class KpiAnomaly {

    // Métrica afectada
    private String metric;

    // Valor detectado
    private int value;

    // Nivel severidad
    private String severity;

    // Constructor vacío
    public KpiAnomaly() {
    }

    // Constructor completo
    public KpiAnomaly(
            String metric,
            int value,
            String severity
    ) {
        this.metric = metric;
        this.value = value;
        this.severity = severity;
    }

    // Getters

    public String getMetric() {
        return metric;
    }

    public int getValue() {
        return value;
    }

    public String getSeverity() {
        return severity;
    }
}