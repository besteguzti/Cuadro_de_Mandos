import { useEffect, useState } from "react";

import "../App.css";

import KpiCard from "../components/KpiCard";
import { API_BASE_URL } from "../config/api";

const mainKpiInfo = {
    globalHealth: {
        description:
            "Resume la afeccion general de la plataforma combinando Aruba, Citrix, Microsoft 365 y GLPI.",
        algorithm:
            "Se calcula como Aruba 40%, Citrix 30%, Microsoft 365 20% y GLPI 10%, usando una escala comun de afeccion 0-100.",
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
            "Combina disponibilidad Aruba 45%, Citrix 35%, Microsoft 365 15% y GLPI 5%.",
        interpretation:
            "Cuanto mas alto, mayor riesgo de que los servicios no esten disponibles."
    },
    operationalPressure: {
        description:
            "Mide la carga de trabajo tecnica y operativa acumulada.",
        algorithm:
            "Combina GLPI 50%, Citrix 20%, Microsoft 365 20% y Aruba 10%.",
        interpretation:
            "Un valor alto indica mas presion sobre el area tecnica."
    },
    technicalDegradation: {
        description:
            "Mide deterioro tecnico aunque no exista una caida total.",
        algorithm:
            "Combina Aruba, Citrix y Microsoft 365 al 30% cada uno, y GLPI al 10%.",
        interpretation:
            "Valores altos indican degradacion tecnica que conviene revisar."
    },
    slaRisk: {
        description:
            "Mide el riesgo de incumplir niveles de servicio.",
        algorithm:
            "Combina Citrix 35%, Aruba 30%, GLPI 25% y Microsoft 365 10%.",
        interpretation:
            "Un valor alto indica mayor probabilidad de incumplimiento o degradacion percibida."
    },
    operationalBacklog: {
        description:
            "Mide el trabajo pendiente acumulado.",
        algorithm:
            "Combina GLPI 70%, Microsoft 365 15%, Aruba 10% y Citrix 5%.",
        interpretation:
            "Un valor alto indica acumulacion de trabajo pendiente o acciones tecnicas."
    },
    userImpact: {
        description:
            "Mide la afeccion que pueden percibir los usuarios.",
        algorithm:
            "Combina Citrix 35%, Aruba 35%, Microsoft 365 20% y GLPI 10%.",
        interpretation:
            "Un valor alto indica mayor probabilidad de impacto visible para usuarios."
    },
    affectedServices: {
        description:
            "Mide cuantas plataformas estan afectadas.",
        algorithm:
            "Cada plataforma en amarillo o rojo suma un 25%: Aruba, Citrix, Microsoft 365 y GLPI.",
        interpretation:
            "0% significa ninguna plataforma afectada. 100% significa las cuatro plataformas afectadas."
    }
};

function MainPage() {

    // =========================
    // Estado dashboard general
    // =========================
    //
    // La vista principal consume
    // KPIs transversales calculados
    // por el backend.
    //

    const [summary, setSummary] =
        useState(null);

    const [error, setError] =
        useState(null);

    // =========================
    // Carga resumen transversal
    // =========================
    //
    // GET /dashboard/summary
    //

    const loadDashboard = () => {

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
                status: statusFromPercentage(summary.globalHealthPercentage),
                info: mainKpiInfo.globalHealth
            },
            {
                title: "Criticidad global",
                value: `${summary.globalCriticality}%`,
                status: statusFromPercentage(summary.globalCriticality),
                info: mainKpiInfo.globalCriticality
            },
            {
                title: "Disponibilidad global",
                value: `${summary.globalAvailability}%`,
                status: statusFromPercentage(summary.globalAvailability),
                info: mainKpiInfo.globalAvailability
            },
            {
                title: "Presion operativa",
                value: `${summary.operationalPressure}%`,
                status: statusFromPercentage(summary.operationalPressure),
                info: mainKpiInfo.operationalPressure
            },
            {
                title: "Degradacion tecnica",
                value: `${summary.technicalDegradation}%`,
                status: statusFromPercentage(summary.technicalDegradation),
                info: mainKpiInfo.technicalDegradation
            },
            {
                title: "Riesgo SLA",
                value: `${summary.slaRisk}%`,
                status: statusFromPercentage(summary.slaRisk),
                info: mainKpiInfo.slaRisk
            },
            {
                title: "Backlog operativo",
                value: `${summary.operationalBacklog}%`,
                status: statusFromPercentage(summary.operationalBacklog),
                info: mainKpiInfo.operationalBacklog
            },
            {
                title: "Impacto en usuarios",
                value: `${summary.userImpact}%`,
                status: statusFromPercentage(summary.userImpact),
                info: mainKpiInfo.userImpact
            },
            {
                title: "Servicios afectados",
                value: `${summary.affectedServicesPercent}%`,
                status: statusFromPercentage(summary.affectedServicesPercent),
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

function formatSnapshotDate(value) {
    if (!value) {
        return "Sin datos";
    }

    return new Date(value).toLocaleString();
}

function statusFromPercentage(value) {
    if (value >= 67) {
        return "danger";
    }

    if (value >= 34) {
        return "warning";
    }

    return "ok";
}

export default MainPage;
