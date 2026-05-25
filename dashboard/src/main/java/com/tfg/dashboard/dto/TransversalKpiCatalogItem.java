package com.tfg.dashboard.dto;

import java.util.List;

public class TransversalKpiCatalogItem {

    private String code;

    private String name;

    private String description;

    private String unit;

    private Double currentValue;

    private List<String> relatedKpis;

    public TransversalKpiCatalogItem() {
    }

    public TransversalKpiCatalogItem(
            String code,
            String name,
            String description,
            String unit,
            Double currentValue,
            List<String> relatedKpis
    ) {

        this.code = code;
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.currentValue = currentValue;
        this.relatedKpis = relatedKpis;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public List<String> getRelatedKpis() {
        return relatedKpis;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    public void setRelatedKpis(List<String> relatedKpis) {
        this.relatedKpis = relatedKpis;
    }
}
