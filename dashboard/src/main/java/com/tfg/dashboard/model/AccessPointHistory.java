package com.tfg.dashboard.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "access_point_history")
public class AccessPointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String status;

    private String ipAddress;

    private String publicIpAddress;

    private String serial;

    private String site;

    private String firmwareVersion;

    private String macaddr;

    private String swarmName;

    private LocalDateTime collectedAt;

    // =========================
    // GETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public String getSerial() {
        return serial;
    }

    public String getSite() {
        return site;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getMacaddr() {
        return macaddr;
    }

    public String getSwarmName() {
        return swarmName;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    // =========================
    // SETTERS
    // =========================

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPublicIpAddress(String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public void setMacaddr(String macaddr) {
        this.macaddr = macaddr;
    }

    public void setSwarmName(String swarmName) {
        this.swarmName = swarmName;
    }

    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }
}