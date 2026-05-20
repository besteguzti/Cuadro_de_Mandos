import { useEffect, useState } from "react";

import "../App.css";

import KpiCard from "../components/KpiCard";
import { API_BASE_URL } from "../config/api";

const glpiKpiInfo = {
  openTickets: {
    description:
      "Representa el número total de tickets abiertos en el entorno GLPI simulado.",
    algorithm:
      "Se genera dinámicamente en GlpiService como carga operativa actual.",
    interpretation:
      "Un valor alto indica mayor volumen de trabajo pendiente y puede requerir refuerzo operativo."
  },
  criticalOpenTickets: {
    description:
      "Indica tickets abiertos clasificados como críticos.",
    algorithm:
      "Se genera dinámicamente en GlpiService manteniendo coherencia con el total de tickets abiertos.",
    interpretation:
      "Cualquier valor mayor que cero requiere atención prioritaria por posible impacto en servicio."
  },
  slaBreachedTickets: {
    description:
      "Cuenta tickets que han superado el tiempo objetivo de resolución o atención.",
    algorithm:
      "Se genera dinámicamente en GlpiService y se clasifica con umbrales de advertencia y criticidad.",
    interpretation:
      "Un valor alto indica incumplimiento de compromisos de servicio y mayor riesgo operativo."
  },
  averageResolutionHours: {
    description:
      "Mide el tiempo medio de resolución de tickets, expresado en horas.",
    algorithm:
      "Se genera dinámicamente en GlpiService y se evalúa con umbrales de advertencia y criticidad.",
    interpretation:
      "Un valor alto indica lentitud en la resolución y posible saturación del equipo de soporte."
  },
  operationalBacklog: {
    description:
      "Representa la carga operativa pendiente acumulada.",
    algorithm:
      "Se genera dinámicamente en GlpiService a partir del comportamiento simulado de tickets.",
    interpretation:
      "Un backlog elevado indica más trabajo acumulado y mayor presión operativa."
  },
  createdToday: {
    description:
      "Indica tickets creados durante el día actual en la simulación.",
    algorithm:
      "Se genera dinámicamente en GlpiService como actividad diaria entrante.",
    interpretation:
      "Un valor alto indica mayor demanda de soporte durante el día."
  },
  closedToday: {
    description:
      "Indica tickets cerrados durante el día actual.",
    algorithm:
      "Se genera dinámicamente en GlpiService y se compara visualmente con los tickets creados hoy.",
    interpretation:
      "Si es menor que los tickets creados hoy, puede crecer el trabajo pendiente diario."
  },
  createdThisWeek: {
    description:
      "Indica tickets creados durante la semana actual.",
    algorithm:
      "Se genera dinámicamente en GlpiService como actividad semanal entrante.",
    interpretation:
      "Un valor alto indica mayor demanda semanal y ayuda a detectar picos de actividad."
  },
  closedThisWeek: {
    description:
      "Indica tickets cerrados durante la semana actual.",
    algorithm:
      "Se genera dinámicamente en GlpiService, manteniendo una relación coherente con los tickets creados semanalmente.",
    interpretation:
      "Si queda por debajo de los creados en la semana, el backlog puede aumentar."
  }
};

function GlpiPage() {
  // =========================
  // Estado resumen GLPI
  // =========================
  //
  // Guarda los KPIs recibidos
  // desde el backend.
  //
  const [summary, setSummary] = useState(null);

  // =========================
  // Estado de carga
  // =========================
  //
  // Permite mostrar un mensaje
  // mientras se consulta la API.
  //
  const [loading, setLoading] = useState(true);

  // =========================
  // Carga datos GLPI
  // =========================
  //
  // Consume:
  // GET /glpi/summary
  //
  useEffect(() => {
    fetch(`${API_BASE_URL}/glpi/summary`)
      .then((response) => response.json())
      .then((data) => {
        setSummary(data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error cargando GLPI:", error);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <main className="dashboard">
        <h1>GLPI</h1>
        <p className="loading">Cargando datos GLPI...</p>
      </main>
    );
  }

  if (!summary) {
    return (
      <main className="dashboard">
        <h1>GLPI</h1>
        <p className="loading">No se han podido cargar los datos de GLPI.</p>
      </main>
    );
  }

  return (
    <main className="dashboard">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Monitorizacion GLPI</p>
          <h1>GLPI</h1>
        </div>
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
      </header>

      <p>
        Supervisión simulada de tickets, actividad semanal, SLA y backlog
        operativo.
      </p>

      <section className="dashboard-section">
      <h2>Operación</h2>

      <div className="kpi-grid">
        <KpiCard
          title="Tickets abiertos"
          value={summary.openTickets}
          status={summary.openTickets > 150 ? "warning" : "ok"}
          info={glpiKpiInfo.openTickets}
        />

        <KpiCard
          title="Tickets críticos abiertos"
          value={summary.criticalOpenTickets}
          status={summary.criticalOpenTickets > 0 ? "danger" : "ok"}
          info={glpiKpiInfo.criticalOpenTickets}
        />

        <KpiCard
          title="Tickets vencidos SLA"
          value={summary.slaBreachedTickets}
          status={
            summary.slaBreachedTickets > 15
              ? "danger"
              : summary.slaBreachedTickets > 5
                ? "warning"
                : "ok"
          }
          info={glpiKpiInfo.slaBreachedTickets}
        />
      </div>

      </section>

      <section className="dashboard-section">
      <h2>Rendimiento</h2>

      <div className="kpi-grid">
        <KpiCard
          title="Tiempo medio resolución"
          value={`${summary.averageResolutionHours}h`}
          status={
            summary.averageResolutionHours > 24
              ? "danger"
              : summary.averageResolutionHours > 12
                ? "warning"
                : "ok"
          }
          info={glpiKpiInfo.averageResolutionHours}
        />

        <KpiCard
          title="Backlog operativo"
          value={summary.operationalBacklog}
          status={summary.operationalBacklog > 150 ? "warning" : "ok"}
          info={glpiKpiInfo.operationalBacklog}
        />
      </div>

      </section>

      <section className="dashboard-section">
      <h2>Actividad diaria</h2>

      <div className="kpi-grid">
        <KpiCard
          title="Tickets creados hoy"
          value={summary.createdToday}
          status="neutral"
          info={glpiKpiInfo.createdToday}
        />

        <KpiCard
          title="Tickets cerrados hoy"
          value={summary.closedToday}
          status={
            summary.closedToday < summary.createdToday
              ? "warning"
              : "ok"
          }
          info={glpiKpiInfo.closedToday}
        />
      </div>

      </section>

      <section className="dashboard-section">
      <h2>Actividad semanal</h2>

      <div className="kpi-grid">
        <KpiCard
          title="Tickets creados semana"
          value={summary.createdThisWeek}
          status="neutral"
          info={glpiKpiInfo.createdThisWeek}
        />

        <KpiCard
          title="Tickets cerrados semana"
          value={summary.closedThisWeek}
          status={
            summary.closedThisWeek < summary.createdThisWeek
              ? "warning"
              : "ok"
          }
          info={glpiKpiInfo.closedThisWeek}
        />
      </div>
      </section>
    </main>
  );
}

function formatSnapshotDate(value) {
  if (!value) {
    return "Sin datos";
  }

  return new Date(value).toLocaleString();
}

export default GlpiPage;
