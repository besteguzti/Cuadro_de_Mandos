package com.tfg.dashboard.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "aruba_switch_client_usage",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_switch_client_usage_associated_device",
                columnNames = "associated_device"
        )
)
public class ArubaSwitchClientUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "associated_device", nullable = false)
    private String associatedDevice;

    @Column(name = "associated_device_name")
    private String associatedDeviceName;

    @Column(name = "associated_device_mac")
    private String associatedDeviceMac;

    @Column(name = "wired_clients")
    private int wiredClients;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getAssociatedDevice() {
        return associatedDevice;
    }

    public String getAssociatedDeviceName() {
        return associatedDeviceName;
    }

    public String getAssociatedDeviceMac() {
        return associatedDeviceMac;
    }

    public int getWiredClients() {
        return wiredClients;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setAssociatedDevice(String associatedDevice) {
        this.associatedDevice = associatedDevice;
    }

    public void setAssociatedDeviceName(String associatedDeviceName) {
        this.associatedDeviceName = associatedDeviceName;
    }

    public void setAssociatedDeviceMac(String associatedDeviceMac) {
        this.associatedDeviceMac = associatedDeviceMac;
    }

    public void setWiredClients(int wiredClients) {
        this.wiredClients = wiredClients;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
