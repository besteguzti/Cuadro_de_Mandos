package com.tfg.dashboard.repository;

// JpaRepository proporciona operaciones CRUD automáticas
import org.springframework.data.jpa.repository.JpaRepository;

// Entidad KPI gestionada por este repository
import com.tfg.dashboard.entity.KpiEntity;

import java.util.List;
// Este repository permitirá trabajar con la tabla "kpis"
//
// KpiEntity -> entidad asociada
// Long -> tipo del campo ID
public interface KpiRepository extends JpaRepository<KpiEntity, Long> {

    // Obtiene los 10 últimos KPIs ordenados por fecha descendente
    List<KpiEntity> findTop100ByOrderByCreatedAtDesc();
    // KPIs filtrados por nombre ordenados por fecha
    List<KpiEntity> findByNameOrderByCreatedAtDesc(String name);
    //Tendencia de los últimos dos KPIs de un nombre específico
    List<KpiEntity> findTop2ByNameOrderByCreatedAtDesc(String name);
    // De momento no añadimos métodos personalizados.
    //
    // JpaRepository ya proporciona:
    //
    // save()
    // findAll()
    // findById()
    // delete()
    //
    // Más adelante podríamos añadir consultas como:
    //
    // findByPlatform()
    // findByName()
    // findByCreatedAtBetween()
}