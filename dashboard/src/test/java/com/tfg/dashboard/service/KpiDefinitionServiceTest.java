package com.tfg.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.tfg.dashboard.config.properties.KpiProperties;
import com.tfg.dashboard.dto.KpiDefinitionDto;

class KpiDefinitionServiceTest {

    private final KpiDefinitionService service =
            new KpiDefinitionService(new KpiProperties());

    @Test
    void returnsDefinitionsForPlatformGlobalAndAnalysisKpis() {

        var definitions =
                service.getDefinitions();

        assertThat(definitions)
                .hasSize(21);

        assertThat(definitions)
                .extracting("id")
                .contains(
                        "aruba_network_status",
                        "global_status",
                        "glpi_operational_pressure",
                        "high_high_cooccurrence"
                );
    }

    @Test
    void globalStatusDefinitionIncludesFormulaThresholdsAndSources() {

        var globalStatus =
                service.getDefinitions().stream()
                        .filter(definition -> "global_status".equals(
                                definition.getId()
                        ))
                        .findFirst()
                        .orElseThrow();

        assertThat(globalStatus.getFormula())
                .contains("Aruba estado de red * 0.40");
        assertThat(globalStatus.getThresholds().getGreen())
                .isEqualTo("0-33");
        assertThat(globalStatus.getSources())
                .contains("Citrix indice de salud Citrix");
    }

    @Test
    void globalStatusDefinitionUsesConfiguredWeights() {

        KpiProperties properties =
                new KpiProperties();
        properties.getWeights().setGlobalStatus(
                new KpiProperties.PlatformWeights(
                        0.41,
                        0.29,
                        0.21,
                        0.09
                )
        );

        KpiDefinitionDto globalStatus =
                findDefinition(
                        new KpiDefinitionService(properties),
                        "global_status"
                );

        assertThat(globalStatus.getFormula())
                .contains(
                        "Aruba estado de red * 0.41",
                        "Citrix indice de salud * 0.29",
                        "Microsoft 365 indice de salud * 0.21",
                        "GLPI indice de salud * 0.09"
                );
    }

    @Test
    void definitionsUseConfiguredCommonThresholds() {

        KpiProperties properties =
                new KpiProperties();
        properties.getStatus().setYellowMin(40);
        properties.getStatus().setRedMin(70);
        properties.getStatus().setMax(120);

        KpiDefinitionDto globalStatus =
                findDefinition(
                        new KpiDefinitionService(properties),
                        "global_status"
                );

        assertThat(globalStatus.getThresholds().getGreen())
                .isEqualTo("0-39");
        assertThat(globalStatus.getThresholds().getYellow())
                .isEqualTo("40-69");
        assertThat(globalStatus.getThresholds().getRed())
                .isEqualTo("70-120");
    }

    @Test
    void doesNotExposeOldMicrosoftOperationalRiskDefinition() {

        assertThat(service.getDefinitions())
                .allSatisfy(definition -> {
                    assertThat(definition.getId().toLowerCase())
                            .doesNotContain("operational_risk");
                    assertThat(definition.getName().toLowerCase())
                            .doesNotContain("riesgo operativo microsoft");
                    assertThat(definition.getFormula().toLowerCase())
                            .doesNotContain("riesgo operativo microsoft");
                });
    }

    @Test
    void microsoft365DefinitionUsesConfiguredThresholds() {

        KpiDefinitionDto microsoft365 =
                findDefinition(service, "microsoft365_health");

        assertThat(microsoft365.getFormula())
                .contains(
                        "SharePoint: > 90% rojo, >= 80% amarillo",
                        "Usuarios sin MFA: > 3 rojo, > 0 amarillo",
                        "Equipos no conformes: > 100 rojo, > 50 amarillo",
                        "Equipos sin cifrado: > 5 rojo"
                );
    }

    private KpiDefinitionDto findDefinition(
            KpiDefinitionService definitionService,
            String id
    ) {

        return definitionService.getDefinitions().stream()
                .filter(definition -> id.equals(definition.getId()))
                .findFirst()
                .orElseThrow();
    }
}
