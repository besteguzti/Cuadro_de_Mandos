package com.tfg.dashboard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.dashboard.model.ArubaSwitchClientUsage;

public interface ArubaSwitchClientUsageRepository
        extends JpaRepository<ArubaSwitchClientUsage, Long> {

    Optional<ArubaSwitchClientUsage> findByAssociatedDevice(
            String associatedDevice
    );

    List<ArubaSwitchClientUsage>
            findByWiredClientsLessThanOrderByWiredClientsAscAssociatedDeviceAsc(
                    int wiredClients
            );
}
