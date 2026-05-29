package com.tfg.dashboard.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración de pesos y umbrales usados en el cálculo de KPIs.
 * Se usa para que las reglas principales del dashboard no queden repartidas en
 * varios servicios como números fijos. Así, los cálculos pueden reutilizar la
 * misma configuración y se reduce el riesgo de que una fórmula y su explicación
 * acaben diciendo cosas distintas.
 */
@Component
@ConfigurationProperties(prefix = "kpi")
public class KpiProperties {

        private Status status = new Status();
        private Affection affection = new Affection();
        private Weights weights = new Weights();
        private Citrix citrix = new Citrix();
        private Microsoft365 microsoft365 = new Microsoft365();
        private Glpi glpi = new Glpi();
        private Aruba aruba = new Aruba();
        private Executive executive = new Executive();

        public Status getStatus() {
                return status;
        }

        public void setStatus(Status status) {
                this.status = status;
        }

        public Affection getAffection() {
                return affection;
        }

        public void setAffection(Affection affection) {
                this.affection = affection;
        }

        public Weights getWeights() {
                return weights;
        }

        public void setWeights(Weights weights) {
                this.weights = weights;
        }

        public Citrix getCitrix() {
                return citrix;
        }

        public void setCitrix(Citrix citrix) {
                this.citrix = citrix;
        }

        public Microsoft365 getMicrosoft365() {
                return microsoft365;
        }

        public void setMicrosoft365(Microsoft365 microsoft365) {
                this.microsoft365 = microsoft365;
        }

        public Glpi getGlpi() {
                return glpi;
        }

        public void setGlpi(Glpi glpi) {
                this.glpi = glpi;
        }

        public Aruba getAruba() {
                return aruba;
        }

        public void setAruba(Aruba aruba) {
                this.aruba = aruba;
        }

        public Executive getExecutive() {
                return executive;
        }

        public void setExecutive(Executive executive) {
                this.executive = executive;
        }

        public int asWeightPercent(double weight) {

                return (int) Math.round(weight * 100);
        }

        public String formatWeight(double weight) {

                return String.format(java.util.Locale.US, "%.2f", weight);
        }

        public static class Status {

                private int yellowMin = 34;
                private int redMin = 67;
                private int max = 100;

                public int getYellowMin() {
                        return yellowMin;
                }

                public void setYellowMin(int yellowMin) {
                        this.yellowMin = yellowMin;
                }

                public int getRedMin() {
                        return redMin;
                }

                public void setRedMin(int redMin) {
                        this.redMin = redMin;
                }

                public int getMax() {
                        return max;
                }

                public void setMax(int max) {
                        this.max = max;
                }
        }

        public static class Affection {

                private int green = 0;
                private int yellow = 50;
                private int red = 100;

                public int getGreen() {
                        return green;
                }

                public void setGreen(int green) {
                        this.green = green;
                }

                public int getYellow() {
                        return yellow;
                }

                public void setYellow(int yellow) {
                        this.yellow = yellow;
                }

                public int getRed() {
                        return red;
                }

                public void setRed(int red) {
                        this.red = red;
                }
        }

        public static class Weights {

                private PlatformWeights globalStatus = new PlatformWeights(0.40, 0.30, 0.20, 0.10);
                private PlatformWeights availability = new PlatformWeights(0.45, 0.35, 0.15, 0.05);
                private PlatformWeights operationalPressure = new PlatformWeights(0.10, 0.20, 0.20, 0.50);
                private PlatformWeights technicalDegradation = new PlatformWeights(0.30, 0.30, 0.30, 0.10);
                private PlatformWeights slaRisk = new PlatformWeights(0.30, 0.35, 0.10, 0.25);
                private PlatformWeights operationalBacklog = new PlatformWeights(0.10, 0.05, 0.15, 0.70);
                private PlatformWeights userImpact = new PlatformWeights(0.35, 0.35, 0.20, 0.10);
                private PlatformWeights analysisTechnicalDegradation = new PlatformWeights(0.35, 0.35, 0.30, 0.0);
                private PlatformWeights analysisUserImpact = new PlatformWeights(0.30, 0.35, 0.20, 0.15);
                private GlpiPressureWeights glpiOperationalPressure =
                                new GlpiPressureWeights(0.40, 0.30, 0.20, 0.10);

                public PlatformWeights getGlobalStatus() {
                        return globalStatus;
                }

                public void setGlobalStatus(PlatformWeights globalStatus) {
                        this.globalStatus = globalStatus;
                }

                public PlatformWeights getAvailability() {
                        return availability;
                }

                public void setAvailability(PlatformWeights availability) {
                        this.availability = availability;
                }

                public PlatformWeights getOperationalPressure() {
                        return operationalPressure;
                }

                public void setOperationalPressure(PlatformWeights operationalPressure) {
                        this.operationalPressure = operationalPressure;
                }

                public PlatformWeights getTechnicalDegradation() {
                        return technicalDegradation;
                }

                public void setTechnicalDegradation(PlatformWeights technicalDegradation) {
                        this.technicalDegradation = technicalDegradation;
                }

                public PlatformWeights getSlaRisk() {
                        return slaRisk;
                }

                public void setSlaRisk(PlatformWeights slaRisk) {
                        this.slaRisk = slaRisk;
                }

                public PlatformWeights getOperationalBacklog() {
                        return operationalBacklog;
                }

                public void setOperationalBacklog(PlatformWeights operationalBacklog) {
                        this.operationalBacklog = operationalBacklog;
                }

                public PlatformWeights getUserImpact() {
                        return userImpact;
                }

                public void setUserImpact(PlatformWeights userImpact) {
                        this.userImpact = userImpact;
                }

                public PlatformWeights getAnalysisTechnicalDegradation() {
                        return analysisTechnicalDegradation;
                }

                public void setAnalysisTechnicalDegradation(PlatformWeights analysisTechnicalDegradation) {
                        this.analysisTechnicalDegradation = analysisTechnicalDegradation;
                }

                public PlatformWeights getAnalysisUserImpact() {
                        return analysisUserImpact;
                }

                public void setAnalysisUserImpact(PlatformWeights analysisUserImpact) {
                        this.analysisUserImpact = analysisUserImpact;
                }

                public GlpiPressureWeights getGlpiOperationalPressure() {
                        return glpiOperationalPressure;
                }

                public void setGlpiOperationalPressure(GlpiPressureWeights glpiOperationalPressure) {
                        this.glpiOperationalPressure = glpiOperationalPressure;
                }
        }

        public static class PlatformWeights {

                private double aruba;
                private double citrix;
                private double microsoft365;
                private double glpi;

                public PlatformWeights() {
                }

                public PlatformWeights(double aruba,double citrix,double microsoft365,double glpi) {
                        this.aruba = aruba;
                        this.citrix = citrix;
                        this.microsoft365 = microsoft365;
                        this.glpi = glpi;
                }

                public double getAruba() {
                        return aruba;
                }

                public void setAruba(double aruba) {
                        this.aruba = aruba;
                }

                public double getCitrix() {
                        return citrix;
                }

                public void setCitrix(double citrix) {
                        this.citrix = citrix;
                }

                public double getMicrosoft365() {
                        return microsoft365;
                }

                public void setMicrosoft365(double microsoft365) {
                        this.microsoft365 = microsoft365;
                }

                public double getGlpi() {
                        return glpi;
                }

                public void setGlpi(double glpi) {
                        this.glpi = glpi;
                }
        }

        public static class GlpiPressureWeights {

                private double openTickets;
                private double closedTodayPercent;
                private double criticalTickets;
                private double closedWeekPercent;

                public GlpiPressureWeights() {
                }

                public GlpiPressureWeights(
                                double openTickets,
                                double closedTodayPercent,
                                double criticalTickets,
                                double closedWeekPercent) {
                        this.openTickets = openTickets;
                        this.closedTodayPercent = closedTodayPercent;
                        this.criticalTickets = criticalTickets;
                        this.closedWeekPercent = closedWeekPercent;
                }

                public double getOpenTickets() {
                        return openTickets;
                }

                public void setOpenTickets(double openTickets) {
                        this.openTickets = openTickets;
                }

                public double getClosedTodayPercent() {
                        return closedTodayPercent;
                }

                public void setClosedTodayPercent(double closedTodayPercent) {
                        this.closedTodayPercent = closedTodayPercent;
                }

                public double getCriticalTickets() {
                        return criticalTickets;
                }

                public void setCriticalTickets(double criticalTickets) {
                        this.criticalTickets = criticalTickets;
                }

                public double getClosedWeekPercent() {
                        return closedWeekPercent;
                }

                public void setClosedWeekPercent(double closedWeekPercent) {
                        this.closedWeekPercent = closedWeekPercent;
                }
        }

        public static class Citrix {

                private int deliveryControllerYellowBelowPercent = 50;
                private int logonDurationYellowAboveSeconds = 20;
                private int logonDurationRedAboveSeconds = 60;
                private int serverLoadYellowMin = 34;
                private int serverLoadRedMin = 67;
                private int failedLogonsYellowAbove = 10;
                private int failedLogonsRedAbove = 30;

                public int getDeliveryControllerYellowBelowPercent() {
                        return deliveryControllerYellowBelowPercent;
                }

                public void setDeliveryControllerYellowBelowPercent(int deliveryControllerYellowBelowPercent) {
                        this.deliveryControllerYellowBelowPercent = deliveryControllerYellowBelowPercent;
                }

                public int getLogonDurationYellowAboveSeconds() {
                        return logonDurationYellowAboveSeconds;
                }

                public void setLogonDurationYellowAboveSeconds(int logonDurationYellowAboveSeconds) {
                        this.logonDurationYellowAboveSeconds = logonDurationYellowAboveSeconds;
                }

                public int getLogonDurationRedAboveSeconds() {
                        return logonDurationRedAboveSeconds;
                }

                public void setLogonDurationRedAboveSeconds(int logonDurationRedAboveSeconds) {
                        this.logonDurationRedAboveSeconds = logonDurationRedAboveSeconds;
                }

                public int getServerLoadYellowMin() {
                        return serverLoadYellowMin;
                }

                public void setServerLoadYellowMin(int serverLoadYellowMin) {
                        this.serverLoadYellowMin = serverLoadYellowMin;
                }

                public int getServerLoadRedMin() {
                        return serverLoadRedMin;
                }

                public void setServerLoadRedMin(int serverLoadRedMin) {
                        this.serverLoadRedMin = serverLoadRedMin;
                }

                public int getFailedLogonsYellowAbove() {
                        return failedLogonsYellowAbove;
                }

                public void setFailedLogonsYellowAbove(int failedLogonsYellowAbove) {
                        this.failedLogonsYellowAbove = failedLogonsYellowAbove;
                }

                public int getFailedLogonsRedAbove() {
                        return failedLogonsRedAbove;
                }

                public void setFailedLogonsRedAbove(int failedLogonsRedAbove) {
                        this.failedLogonsRedAbove = failedLogonsRedAbove;
                }
        }

        public static class Microsoft365 {

                private int sharePointYellowMin = 80;
                private int sharePointRedAbove = 90;
                private int usersWithoutMfaYellowAbove = 0;
                private int usersWithoutMfaRedAbove = 3;
                private int secretsYellowAbove = 0;
                private int nonCompliantDevicesYellowAbove = 50;
                private int nonCompliantDevicesRedAbove = 100;
                private int outdatedWindowsYellowAbove = 0;
                private int devicesWithoutEncryptionRedAbove = 5;

                public int getSharePointYellowMin() {
                        return sharePointYellowMin;
                }

                public void setSharePointYellowMin(int sharePointYellowMin) {
                        this.sharePointYellowMin = sharePointYellowMin;
                }

                public int getSharePointRedAbove() {
                        return sharePointRedAbove;
                }

                public void setSharePointRedAbove(int sharePointRedAbove) {
                        this.sharePointRedAbove = sharePointRedAbove;
                }

                public int getUsersWithoutMfaYellowAbove() {
                        return usersWithoutMfaYellowAbove;
                }

                public void setUsersWithoutMfaYellowAbove(int usersWithoutMfaYellowAbove) {
                        this.usersWithoutMfaYellowAbove = usersWithoutMfaYellowAbove;
                }

                public int getUsersWithoutMfaRedAbove() {
                        return usersWithoutMfaRedAbove;
                }

                public void setUsersWithoutMfaRedAbove(int usersWithoutMfaRedAbove) {
                        this.usersWithoutMfaRedAbove = usersWithoutMfaRedAbove;
                }

                public int getSecretsYellowAbove() {
                        return secretsYellowAbove;
                }

                public void setSecretsYellowAbove(int secretsYellowAbove) {
                        this.secretsYellowAbove = secretsYellowAbove;
                }

                public int getNonCompliantDevicesYellowAbove() {
                        return nonCompliantDevicesYellowAbove;
                }

                public void setNonCompliantDevicesYellowAbove(int nonCompliantDevicesYellowAbove) {
                        this.nonCompliantDevicesYellowAbove = nonCompliantDevicesYellowAbove;
                }

                public int getNonCompliantDevicesRedAbove() {
                        return nonCompliantDevicesRedAbove;
                }

                public void setNonCompliantDevicesRedAbove(int nonCompliantDevicesRedAbove) {
                        this.nonCompliantDevicesRedAbove = nonCompliantDevicesRedAbove;
                }

                public int getOutdatedWindowsYellowAbove() {
                        return outdatedWindowsYellowAbove;
                }

                public void setOutdatedWindowsYellowAbove(int outdatedWindowsYellowAbove) {
                        this.outdatedWindowsYellowAbove = outdatedWindowsYellowAbove;
                }

                public int getDevicesWithoutEncryptionRedAbove() {
                        return devicesWithoutEncryptionRedAbove;
                }

                public void setDevicesWithoutEncryptionRedAbove(int devicesWithoutEncryptionRedAbove) {
                        this.devicesWithoutEncryptionRedAbove = devicesWithoutEncryptionRedAbove;
                }
        }

        public static class Glpi {

                private int openTicketsYellowMin = 101;
                private int openTicketsRedMin = 201;
                private int criticalTicketsYellowAbove = 0;
                private int criticalTicketsRedAbove = 10;
                private int closedPercentGreenMin = 50;

                public int getOpenTicketsYellowMin() {
                        return openTicketsYellowMin;
                }

                public void setOpenTicketsYellowMin(int openTicketsYellowMin) {
                        this.openTicketsYellowMin = openTicketsYellowMin;
                }

                public int getOpenTicketsRedMin() {
                        return openTicketsRedMin;
                }

                public void setOpenTicketsRedMin(int openTicketsRedMin) {
                        this.openTicketsRedMin = openTicketsRedMin;
                }

                public int getCriticalTicketsYellowAbove() {
                        return criticalTicketsYellowAbove;
                }

                public void setCriticalTicketsYellowAbove(int criticalTicketsYellowAbove) {
                        this.criticalTicketsYellowAbove = criticalTicketsYellowAbove;
                }

                public int getCriticalTicketsRedAbove() {
                        return criticalTicketsRedAbove;
                }

                public void setCriticalTicketsRedAbove(int criticalTicketsRedAbove) {
                        this.criticalTicketsRedAbove = criticalTicketsRedAbove;
                }

                public int getClosedPercentGreenMin() {
                        return closedPercentGreenMin;
                }

                public void setClosedPercentGreenMin(int closedPercentGreenMin) {
                        this.closedPercentGreenMin = closedPercentGreenMin;
                }
        }

        public static class Aruba {

                private int freshnessMinutes = 10;
                private int underusedSwitchDownInterfaceLimit = 17;
                private int underusedSwitchDays = 30;
                private int accessPointDownRedPercent = 50;
                private int switchDownYellowMin = 2;
                private int blockYellowContribution = 25;
                private int blockRedContribution = 50;

                public int getFreshnessMinutes() {
                        return freshnessMinutes;
                }

                public void setFreshnessMinutes(int freshnessMinutes) {
                        this.freshnessMinutes = freshnessMinutes;
                }

                public int getUnderusedSwitchDownInterfaceLimit() {
                        return underusedSwitchDownInterfaceLimit;
                }

                public void setUnderusedSwitchDownInterfaceLimit(int underusedSwitchDownInterfaceLimit) {
                        this.underusedSwitchDownInterfaceLimit = underusedSwitchDownInterfaceLimit;
                }

                public int getUnderusedSwitchDays() {
                        return underusedSwitchDays;
                }

                public void setUnderusedSwitchDays(int underusedSwitchDays) {
                        this.underusedSwitchDays = underusedSwitchDays;
                }

                public int getAccessPointDownRedPercent() {
                        return accessPointDownRedPercent;
                }

                public void setAccessPointDownRedPercent(int accessPointDownRedPercent) {
                        this.accessPointDownRedPercent = accessPointDownRedPercent;
                }

                public int getSwitchDownYellowMin() {
                        return switchDownYellowMin;
                }

                public void setSwitchDownYellowMin(int switchDownYellowMin) {
                        this.switchDownYellowMin = switchDownYellowMin;
                }

                public int getBlockYellowContribution() {
                        return blockYellowContribution;
                }

                public void setBlockYellowContribution(int blockYellowContribution) {
                        this.blockYellowContribution = blockYellowContribution;
                }

                public int getBlockRedContribution() {
                        return blockRedContribution;
                }

                public void setBlockRedContribution(int blockRedContribution) {
                        this.blockRedContribution = blockRedContribution;
                }
        }

        public static class Executive {

                private int trendDifferenceThreshold = 10;

                public int getTrendDifferenceThreshold() {
                        return trendDifferenceThreshold;
                }

                public void setTrendDifferenceThreshold(int trendDifferenceThreshold) {
                        this.trendDifferenceThreshold = trendDifferenceThreshold;
                }
        }
}
