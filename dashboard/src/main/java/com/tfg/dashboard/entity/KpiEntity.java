package com.tfg.dashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpis")
public class KpiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String value;

    private LocalDateTime createdAt;

    @ManyToOne
@JoinColumn(
    name = "platform_id",
    foreignKey = @ForeignKey(name = "fk_kpi_platform")
)
    private PlatformEntity platform;

    public KpiEntity() {
    }

    public KpiEntity(String name, String value, LocalDateTime createdAt, PlatformEntity platform) {
        this.name = name;
        this.value = value;
        this.createdAt = createdAt;
        this.platform = platform;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public PlatformEntity getPlatform() {
        return platform;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setPlatform(PlatformEntity platform) {
        this.platform = platform;
    }
}