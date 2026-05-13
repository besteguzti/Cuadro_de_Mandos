package com.tfg.dashboard.repository;

// Optional se utiliza porque una plataforma puede existir o no en la BD
import java.util.Optional;

// JpaRepository proporciona automáticamente operaciones CRUD
import org.springframework.data.jpa.repository.JpaRepository;

// Entidad que este repository gestionará
import com.tfg.dashboard.entity.PlatformEntity;

// JpaRepository<Entidad, TipoID>
//
// PlatformEntity -> entidad asociada
// Long -> tipo del campo @Id de la entidad
public interface PlatformRepository extends JpaRepository<PlatformEntity, Long> {

    // Spring genera automáticamente una consulta equivalente a:
    //
    // SELECT * FROM platforms WHERE name = ?
    //
    // Se utiliza Optional porque puede que la plataforma exista
    // o puede que todavía no esté creada.
    Optional<PlatformEntity> findByName(String name);
}