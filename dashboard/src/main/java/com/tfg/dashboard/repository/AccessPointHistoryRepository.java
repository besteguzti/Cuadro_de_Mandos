package com.tfg.dashboard.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tfg.dashboard.model.AccessPointHistory;

public interface AccessPointHistoryRepository
        extends JpaRepository<AccessPointHistory, Long> {

    @Query("""
        SELECT COUNT(DISTINCT a.serial)
        FROM AccessPointHistory a
        WHERE a.serial NOT IN (
            SELECT DISTINCT b.serial
            FROM AccessPointHistory b
            WHERE b.collectedAt >= :date
        )
    """)
    Long countInactiveSince(
            @Param("date")
            LocalDateTime date
    );
}

