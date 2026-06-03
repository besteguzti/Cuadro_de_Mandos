package com.tfg.dashboard.dto;

public class PlatformWeightsConfigurationDto {

    private Integer aruba;
    private Integer citrix;
    private Integer microsoft365;
    private Integer glpi;
    private Integer total;

    public PlatformWeightsConfigurationDto() {
    }

    public PlatformWeightsConfigurationDto(
            Integer aruba,
            Integer citrix,
            Integer microsoft365,
            Integer glpi) {

        this.aruba = aruba;
        this.citrix = citrix;
        this.microsoft365 = microsoft365;
        this.glpi = glpi;
        this.total = safe(aruba) + safe(citrix) + safe(microsoft365) + safe(glpi);
    }

    public Integer getAruba() {
        return aruba;
    }

    public Integer getCitrix() {
        return citrix;
    }

    public Integer getMicrosoft365() {
        return microsoft365;
    }

    public Integer getGlpi() {
        return glpi;
    }

    public Integer getTotal() {
        return total;
    }

    public void setAruba(Integer aruba) {
        this.aruba = aruba;
    }

    public void setCitrix(Integer citrix) {
        this.citrix = citrix;
    }

    public void setMicrosoft365(Integer microsoft365) {
        this.microsoft365 = microsoft365;
    }

    public void setGlpi(Integer glpi) {
        this.glpi = glpi;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    private static int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
