import { useEffect, useState } from "react";

import "../App.css";

import KpiCard from "../components/KpiCard";
import { API_BASE_URL } from "../config/api";

const mainKpiInfo = {
    globalHealth: {
        description:
            "Resume la afeccion general de la plataforma combinando Aruba, Citrix, Microsoft 365 y GLPI.",
        algorithm:
            "Se calcula ponderando los indices principales de Aruba, Citrix, Microsoft 365 y GLPI segun la configuracion definida en backend.",
        interpretation:
            "0-33 es verde, 34-66 amarillo y 67-100 rojo. Cuanto mas alto, mayor afeccion global."
    },
    globalCriticality: {
        description:
            "Mide la presencia de condiciones criticas en rojo dentro de las plataformas.",
        algorithm:
            "Promedia indicadores normalizados: correcto 0, advertencia 50 y critico 100.",
        interpretation:
            "Un valor alto indica que existen senales criticas repartidas por varias plataformas."
    },
    globalAvailability: {
        description:
            "Mide la afeccion sobre la disponibilidad de los servicios principales.",
        algorithm:
            "Combina senales de disponibilidad de Aruba, Citrix, Microsoft 365 y GLPI usando los pesos configurados en backend.",
        interpretation:
            "Cuanto mas alto, mayor riesgo de que los servicios no esten disponibles."
    },
    operationalPressure: {
        description:
            "Mide la carga de trabajo tecnica y operativa acumulada.",
        algorithm:
            "Combina senales de GLPI, Citrix, Microsoft 365 y Aruba usando los pesos configurados en backend.",
        interpretation:
            "Un valor alto indica mas presion sobre el area tecnica."
    },
    technicalDegradation: {
        description:
            "Mide deterioro tecnico aunque no exista una caida total.",
        algorithm:
            "Combina indicadores tecnicos de Aruba, Citrix, Microsoft 365 y GLPI segun la configuracion del backend.",
        interpretation:
            "Valores altos indican degradacion tecnica que conviene revisar."
    },
    slaRisk: {
        description:
            "Mide el riesgo de incumplir niveles de servicio.",
        algorithm:
            "Combina senales de Citrix, Aruba, GLPI y Microsoft 365 con los pesos definidos en backend.",
        interpretation:
            "Un valor alto indica mayor probabilidad de incumplimiento o degradacion percibida."
    },
    operationalBacklog: {
        description:
            "Mide el trabajo pendiente acumulado.",
        algorithm:
            "Combina trabajo pendiente de GLPI con senales de Microsoft 365, Aruba y Citrix segun pesos configurados en backend.",
        interpretation:
            "Un valor alto indica acumulacion de trabajo pendiente o acciones tecnicas."
    },
    userImpact: {
        description:
            "Mide la afeccion que pueden percibir los usuarios.",
        algorithm:
            "Combina senales de impacto de Citrix, Aruba, Microsoft 365 y GLPI usando la configuracion del backend.",
        interpretation:
            "Un valor alto indica mayor probabilidad de impacto visible para usuarios."
    },
    affectedServices: {
        description:
            "Mide cuantas plataformas estan afectadas.",
        algorithm:
            "Calcula la proporcion de plataformas que estan en amarillo o rojo entre Aruba, Citrix, Microsoft 365 y GLPI.",
        interpretation:
            "0% significa ninguna plataforma afectada. 100% significa las cuatro plataformas afectadas."
    }
};

function MainPage() {
    const [summary, setSummary] =
        useState(null);

    const [executiveSummary, setExecutiveSummary] =
        useState(null);

    const [error, setError] =
        useState(null);

    const loadDashboard = () => {
        // La lógica de KPIs y diagnóstico vive en backend; aquí solo se
        // recuperan los DTOs para renderizar tarjetas y resumen operativo.
        fetch(`${API_BASE_URL}/dashboard/summary`)
            .then((response) => {

                if (!response.ok) {

                    throw new Error(
                        "No se pudo cargar el dashboard general"
                    );
                }

                return response.json();
            })
            .then((data) => {

                setSummary(data);
                setError(null);
            })
            .catch(() => {

                setError(
                    "No se pudo conectar con el backend del dashboard."
                );
            });

        fetch(`${API_BASE_URL}/api/dashboard/executive-summary`)
            .then((response) => {

                if (!response.ok) {

                    throw new Error(
                        "No se pudo cargar el diagnostico operativo"
                    );
                }

                return response.json();
            })
            .then((data) => {

                setExecutiveSummary(data);
            })
            .catch(() => {

                setExecutiveSummary(null);
            });
    };

    useEffect(() => {

        loadDashboard();

        const interval =
            setInterval(() => {

                loadDashboard();

            }, 30000);

        return () => clearInterval(interval);

    }, []);

    if (!summary && !error) {

        return (
            <main className="dashboard">
                <h1>Dashboard General</h1>
                <p className="loading">Cargando dashboard general...</p>
            </main>
        );
    }

    const cards = summary
        ? [
            {
                title: "Estado global",
                value: `${summary.globalHealthPercentage}%`,
                status: summary.globalHealthStatus,
                info: mainKpiInfo.globalHealth
            },
            {
                title: "Criticidad global",
                value: `${summary.globalCriticality}%`,
                status: summary.globalCriticalityStatus,
                info: mainKpiInfo.globalCriticality
            },
            {
                title: "Disponibilidad global",
                value: `${summary.globalAvailability}%`,
                status: summary.globalAvailabilityStatus,
                info: mainKpiInfo.globalAvailability
            },
            {
                title: "Presion operativa",
                value: `${summary.operationalPressure}%`,
                status: summary.operationalPressureStatus,
                info: mainKpiInfo.operationalPressure
            },
            {
                title: "Degradacion tecnica",
                value: `${summary.technicalDegradation}%`,
                status: summary.technicalDegradationStatus,
                info: mainKpiInfo.technicalDegradation
            },
            {
                title: "Riesgo SLA",
                value: `${summary.slaRisk}%`,
                status: summary.slaRiskStatus,
                info: mainKpiInfo.slaRisk
            },
            {
                title: "Backlog operativo",
                value: `${summary.operationalBacklog}%`,
                status: summary.operationalBacklogStatus,
                info: mainKpiInfo.operationalBacklog
            },
            {
                title: "Impacto en usuarios",
                value: `${summary.userImpact}%`,
                status: summary.userImpactStatus,
                info: mainKpiInfo.userImpact
            },
            {
                title: "Servicios afectados",
                value: `${summary.affectedServicesPercent}%`,
                status: summary.affectedServicesStatus,
                info: mainKpiInfo.affectedServices
            }
        ]
        : [];

    return (
        <main className="dashboard">
            <header className="dashboard-header">
                <div>
                    <p className="eyebrow">Vista principal</p>
                    <h1>Dashboard General</h1>
                </div>

                {summary && (
                    <div className="freshness">
                        <p className="updated">
                            Ultima actualizacion: {formatSnapshotDate(summary.lastUpdated)}
                        </p>
                        <p className="updated">
                            Estado de datos:{" "}
                            <span className={`freshness-status freshness-status-${(summary.dataStatus ?? "NO_DATA").toLowerCase()}`}>
                                {summary.dataStatus ?? "NO_DATA"}
                            </span>
                        </p>
                    </div>
                )}
            </header>

            {error && (
                <section className="alert" role="alert">
                    {error}
                </section>
            )}

            {executiveSummary && (
                <section className={`executive-summary executive-summary-${(executiveSummary.priority ?? "LOW").toLowerCase()}`}>
                    <div className="executive-summary-main">
                        <p className="eyebrow">Diagnostico operativo</p>
                        <h2>Resumen operativo</h2>
                        <p>{executiveSummary.summaryText}</p>
                    </div>

                    <div className="executive-summary-grid">
                        <ExecutiveField
                            label="Servicios afectados"
                            value={
                                executiveSummary.affectedServices?.length > 0
                                    ? executiveSummary.affectedServices.join(", ")
                                    : "Sin servicios afectados"
                            }
                        />
                        <ExecutiveField
                            label="Plataforma principal"
                            value={executiveSummary.mainAffectedPlatform}
                        />
                        <ExecutiveField
                            label="Origen probable"
                            value={executiveSummary.probableOrigin}
                        />
                        <ExecutiveField
                            label="Impacto"
                            value={executiveSummary.impactLevel}
                        />
                        <ExecutiveField
                            label="Usuarios potencialmente afectados"
                            value={executiveSummary.estimatedAffectedUsers}
                        />
                        <ExecutiveField
                            label="Prioridad"
                            value={executiveSummary.priority}
                        />
                        <ExecutiveField
                            label="Tendencia"
                            value={executiveSummary.trend}
                        />
                        <ExecutiveField
                            label="Primera accion"
                            value={executiveSummary.firstAction}
                        />
                    </div>
                </section>
            )}

            {summary && (
                <section className="dashboard-section">
                    <div className="kpi-grid">
                        {cards.map((card) => (
                            <KpiCard
                                key={card.title}
                                title={card.title}
                                value={card.value}
                                status={card.status}
                                info={card.info}
                            />
                        ))}
                    </div>
                </section>
            )}
        </main>
    );
}

function ExecutiveField({ label, value }) {
    return (
        <div className="executive-summary-field">
            <span>{label}</span>
            <strong>{value}</strong>
        </div>
    );
}

function formatSnapshotDate(value) {
    if (!value) {
        return "Sin datos";
    }

    return new Date(value).toLocaleString();
}

export default MainPage;
