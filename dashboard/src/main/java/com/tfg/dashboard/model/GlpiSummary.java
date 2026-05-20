package com.tfg.dashboard.model;

import java.time.LocalDateTime;

public class GlpiSummary {

    // =========================
    // Operación
    // =========================
    //
    // Tickets actualmente abiertos
    //

    private int openTickets;

    // =========================
    // Incidencias críticas
    // =========================
    //
    // Siempre deberá ser menor
    // o igual que tickets abiertos
    //

    private int criticalOpenTickets;

    // =========================
    // SLA
    // =========================
    //
    // Tickets que han superado
    // el tiempo objetivo
    //

    private int slaBreachedTickets;

    // =========================
    // Rendimiento
    // =========================
    //
    // Tiempo medio de resolución
    // expresado en horas
    //

    private int averageResolutionHours;

    // =========================
    // Actividad diaria
    // =========================

    private int createdToday;

    private int closedToday;

    // =========================
    // Actividad semanal
    // =========================

    private int createdThisWeek;

    private int closedThisWeek;

    // =========================
    // Backlog operativo
    // =========================
    //
    // Carga operativa pendiente
    //

    private int operationalBacklog;

    // =========================
    // Frescura de datos
    // =========================
    //
    // dataStatus indica si existe
    // snapshot y si está dentro del
    // margen esperado de sincronización.
    //

    private LocalDateTime lastUpdated;

    private String dataStatus;

    // =========================
    // Constructor vacío
    // =========================

    public GlpiSummary() {
    }

    // =========================
    // Getters / Setters
    // =========================

    public int getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(
            int openTickets
    ) {
        this.openTickets =
                openTickets;
    }

    public int getCriticalOpenTickets() {
        return criticalOpenTickets;
    }

    public void setCriticalOpenTickets(
            int criticalOpenTickets
    ) {
        this.criticalOpenTickets =
                criticalOpenTickets;
    }

    public int getSlaBreachedTickets() {
        return slaBreachedTickets;
    }

    public void setSlaBreachedTickets(
            int slaBreachedTickets
    ) {
        this.slaBreachedTickets =
                slaBreachedTickets;
    }

    public int getAverageResolutionHours() {
        return averageResolutionHours;
    }

    public void setAverageResolutionHours(
            int averageResolutionHours
    ) {
        this.averageResolutionHours =
                averageResolutionHours;
    }

    public int getCreatedToday() {
        return createdToday;
    }

    public void setCreatedToday(
            int createdToday
    ) {
        this.createdToday =
                createdToday;
    }

    public int getClosedToday() {
        return closedToday;
    }

    public void setClosedToday(
            int closedToday
    ) {
        this.closedToday =
                closedToday;
    }

    public int getCreatedThisWeek() {
        return createdThisWeek;
    }

    public void setCreatedThisWeek(
            int createdThisWeek
    ) {
        this.createdThisWeek =
                createdThisWeek;
    }

    public int getClosedThisWeek() {
        return closedThisWeek;
    }

    public void setClosedThisWeek(
            int closedThisWeek
    ) {
        this.closedThisWeek =
                closedThisWeek;
    }

    public int getOperationalBacklog() {
        return operationalBacklog;
    }

    public void setOperationalBacklog(
            int operationalBacklog
    ) {
        this.operationalBacklog =
                operationalBacklog;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(
            LocalDateTime lastUpdated
    ) {
        this.lastUpdated =
                lastUpdated;
    }

    public String getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(
            String dataStatus
    ) {
        this.dataStatus =
                dataStatus;
    }

}
