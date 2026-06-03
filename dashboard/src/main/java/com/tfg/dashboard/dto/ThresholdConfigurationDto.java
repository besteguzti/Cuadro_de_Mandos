package com.tfg.dashboard.dto;

import java.util.ArrayList;
import java.util.List;

public class ThresholdConfigurationDto {

    private List<ThresholdSectionDto> sections = new ArrayList<>();

    public ThresholdConfigurationDto() {
    }

    public ThresholdConfigurationDto(List<ThresholdSectionDto> sections) {
        this.sections = sections;
    }

    public List<ThresholdSectionDto> getSections() {
        return sections;
    }

    public void setSections(List<ThresholdSectionDto> sections) {
        this.sections = sections;
    }
}
