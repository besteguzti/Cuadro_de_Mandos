import { useEffect, useState } from "react";

import "../App.css";

import KpiCard from "../components/KpiCard";
import { API_BASE_URL } from "../config/api";

const mainKpiInfo = {
    globalHealth: {
        description:
            "Resume el estado general de la plataforma combinando señales de Aruba, Citrix, Microsoft 365 y GLPI.",
        algorithm:
            "Se calcula aplicando reglas de prioridad: si alguna plataforma presenta estado crítico se muestra RED; si existe alguna degradación o alerta moderada se muestra YELLOW; si no existen alertas se muestra GREEN.",
        interpretation:
            "Permite obtener una visión rápida del estado global de la infraestructura sin consultar cada plataforma por separado."
    },
    globalOperationalRisk: {
        description:
            "Índice agregado que resume el nivel de riesgo operativo observado en las distintas plataformas.",
        algorithm:
            "Combina señales como riesgo Microsoft 365, tickets críticos y SLA de GLPI, carga y errores de Citrix, y caídas o firmware pendiente de Aruba. El resultado se limita a una escala de 0 a 100.",
        interpretation:
            "Valores bajos indican situación estable. Valores medios indican degradación o riesgo moderado. Valores altos indican necesidad de revisión prioritaria."
    },
    servicesWithAlerts: {
        description:
            "Cuenta cuántas áreas o plataformas presentan alguna alerta activa.",
        algorithm:
            "Se incrementa si Aruba, Citrix, Microsoft 365 o GLPI presentan estado distinto de correcto o tienen incidencias relevantes.",
        interpretation:
            "Permite saber rápidamente cuántos dominios tecnológicos requieren atención."
    },
    totalActiveUsers: {
        description:
            "Representa la actividad agregada observada en las plataformas integradas.",
        algorithm:
            "Suma métricas de actividad como sesiones activas Citrix, usuarios activos Microsoft 365 y, si existe, clientes WiFi Aruba.",
        interpretation:
            "No representa usuarios únicos reales, ya que un mismo usuario puede aparecer en varias plataformas. Sirve como indicador agregado de actividad."
    },
    itemsRequiringAction: {
        description:
            "Cuenta elementos técnicos u operativos que requieren intervención.",
        algorithm:
            "Suma señales como firmware pendiente, equipos no conformes, dispositivos obsoletos, secretos próximos a caducar, SLA vencidos o componentes no disponibles.",
        interpretation:
            "Un valor elevado indica mayor carga de acciones correctivas pendientes."
    },
    criticalOpenTickets: {
        description:
            "Indica el número de tickets críticos abiertos registrados en GLPI.",
        algorithm:
            "Se obtiene del último snapshot de métricas GLPI almacenado en MySQL.",
        interpretation:
            "Un valor mayor que cero indica incidencias de alta prioridad que pueden afectar al servicio."
    },
    securityRiskItems: {
        description:
            "Agrega señales de seguridad principalmente procedentes de Microsoft 365.",
        algorithm:
            "Suma usuarios en riesgo, usuarios sin MFA, aplicaciones con permisos elevados y secretos próximos a caducar.",
        interpretation:
            "Un valor elevado indica mayor exposición de seguridad o necesidad de revisión."
    },
    capacityPressure: {
        description:
            "Índice que resume la presión sobre recursos y operación.",
        algorithm:
            "Combina carga de servidores Citrix, almacenamiento SharePoint y backlog operativo GLPI, normalizando el resultado en una escala de 0 a 100.",
        interpretation:
            "Valores altos indican mayor presión operativa o riesgo de saturación."
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

    const healthStatus =
        summary?.globalHealth === "RED"
            ? "danger"
            : summary?.globalHealth === "YELLOW"
                ? "warning"
                : "ok";

    const riskStatus =
        summary?.globalOperationalRisk >= 60
            ? "danger"
            : summary?.globalOperationalRisk >= 30
                ? "warning"
                : "ok";

    const capacityStatus =
        summary?.capacityPressure >= 80
            ? "danger"
            : summary?.capacityPressure >= 60
                ? "warning"
                : "ok";

    const cards = summary
        ? [
            {
                title: "Estado global",
                value: summary.globalHealth,
                status: healthStatus,
                info: mainKpiInfo.globalHealth
            },
            {
                title: "Riesgo operativo global",
                value: `${summary.globalOperationalRisk}%`,
                status: riskStatus,
                info: mainKpiInfo.globalOperationalRisk
            },
            {
                title: "Servicios con alerta",
                value: summary.servicesWithAlerts,
                status: summary.servicesWithAlerts > 0 ? "warning" : "ok",
                info: mainKpiInfo.servicesWithAlerts
            },
            {
                title: "Actividad agregada observada",
                value: summary.totalActiveUsers,
                status: "ok",
                info: mainKpiInfo.totalActiveUsers
            },
            {
                title: "Elementos que requieren accion",
                value: summary.itemsRequiringAction,
                status: summary.itemsRequiringAction > 0 ? "warning" : "ok",
                info: mainKpiInfo.itemsRequiringAction
            },
            {
                title: "Tickets criticos abiertos",
                value: summary.criticalOpenTickets,
                status: summary.criticalOpenTickets > 0 ? "danger" : "ok",
                info: mainKpiInfo.criticalOpenTickets
            },
            {
                title: "Riesgos de seguridad",
                value: summary.securityRiskItems,
                status: summary.securityRiskItems > 0 ? "warning" : "ok",
                info: mainKpiInfo.securityRiskItems
            },
            {
                title: "Presion de capacidad",
                value: `${summary.capacityPressure}%`,
                status: capacityStatus,
                info: mainKpiInfo.capacityPressure
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

export default MainPage;
