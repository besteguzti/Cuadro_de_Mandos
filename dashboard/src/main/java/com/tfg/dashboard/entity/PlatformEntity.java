package com.tfg.dashboard.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "platforms")
public class PlatformEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type;

    private String description;

    public PlatformEntity() {
    }

    public PlatformEntity(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}