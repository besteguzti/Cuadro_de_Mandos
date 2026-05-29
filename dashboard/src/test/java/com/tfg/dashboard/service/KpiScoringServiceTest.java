package com.tfg.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.tfg.dashboard.config.properties.KpiProperties;

class KpiScoringServiceTest {

    private final KpiScoringService service =
            new KpiScoringService(new KpiProperties());

    @Test
    void statusFromAffectionUsesCommonThresholds() {

        assertThat(service.statusFromAffection(33))
                .isEqualTo("GREEN");
        assertThat(service.statusFromAffection(34))
                .isEqualTo("YELLOW");
        assertThat(service.statusFromAffection(67))
                .isEqualTo("RED");
    }

    @Test
    void clampToIntLimitsValuesBetweenZeroAndOneHundred() {

        assertThat(service.clampToInt(-10))
                .isZero();
        assertThat(service.clampToInt(42.4))
                .isEqualTo(42);
        assertThat(service.clampToInt(150))
                .isEqualTo(100);
    }

    @Test
    void staleFreshnessDoesNotKeepGreenStatus() {

        assertThat(service.statusFromFreshness("STALE", "GREEN"))
                .isEqualTo("YELLOW");
        assertThat(service.statusFromFreshness("NO_DATA", "GREEN"))
                .isEqualTo("NO_DATA");
    }
}
