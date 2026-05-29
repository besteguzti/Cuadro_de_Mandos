package com.tfg.dashboard.dto;

import java.time.LocalDateTime;

public class TechnicalTimelinePointDto {

    private LocalDateTime timestamp;
    private Double aruba;
    private Double citrix;
    private Double microsoft365;

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Double getAruba() {
        return aruba;
    }

    public Double getCitrix() {
        return citrix;
    }

    public Double getMicrosoft365() {
        return microsoft365;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setAruba(Double aruba) {
        this.aruba = aruba;
    }

    public void setCitrix(Double citrix) {
        this.citrix = citrix;
    }

    public void setMicrosoft365(Double microsoft365) {
        this.microsoft365 = microsoft365;
    }
}
