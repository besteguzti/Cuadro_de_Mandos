package com.tfg.dashboard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.dashboard.entity.KpiEntity;

public interface KpiRepository
        extends JpaRepository<KpiEntity, Long> {

    // Obtiene los 100 últimos KPIs
    List<KpiEntity> findTop100ByOrderByCreatedAtDesc();

    // KPIs filtrados por nombre
    List<KpiEntity> findByNameOrderByCreatedAtDesc(
            String name);

    // Últimos 2 KPIs para tendencias
    List<KpiEntity> findTop2ByNameOrderByCreatedAtDesc(
            String name);

    
}