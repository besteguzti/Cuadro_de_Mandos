package com.tfg.dashboard.model;

public class KpiTrend {

    // Nombre métrica
    private String metric;

    // Valor más reciente
    private int currentValue;

    // Valor anterior
    private int previousValue;

    // Tendencia calculada
    private String trend;

    // Constructor vacío
    public KpiTrend() {
    }

    // Constructor completo
    public KpiTrend(
            String metric,
            int currentValue,
            int previousValue,
            String trend
    ) {
        this.metric = metric;
        this.currentValue = currentValue;
        this.previousValue = previousValue;
        this.trend = trend;
    }

    // Getters

    public String getMetric() {
        return metric;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public int getPreviousValue() {
        return previousValue;
    }

    public String getTrend() {
        return trend;
    }
}