import { useEffect, useState } from "react";

import "../App.css";

import KpiCard from "../components/KpiCard";
import { API_BASE_URL } from "../config/api";

const citrixKpiInfo = {
  activeSessions: {
    description:
      "Representa el número de sesiones activas simuladas en el entorno Citrix. Aproxima la carga actual de usuarios conectados a escritorios o aplicaciones virtuales.",
    algorithm:
      "En esta fase se genera de forma simulada en CitrixService mediante un valor dinámico entre 250 y 449 sesiones. En una integración real se obtendría desde Citrix Monitor o Citrix Cloud.",
    interpretation:
      "Un valor alto indica mayor uso de la plataforma Citrix. No representa usuarios únicos, sino sesiones activas observadas."
  },
  activeLicenses: {
    description:
      "Indica el número de licencias activas o asignadas en el entorno Citrix simulado.",
    algorithm:
      "El valor se genera dinámicamente en CitrixService dentro de un rango controlado para simular disponibilidad de licenciamiento.",
    interpretation:
      "Permite valorar la capacidad disponible para usuarios Citrix. Un consumo elevado podría anticipar problemas de capacidad o necesidad de ampliación."
  },
  deliveryControllers: {
    description:
      "Indica cuántos Delivery Controllers están disponibles respecto al total configurado. Son componentes críticos para gestionar sesiones y publicar recursos Citrix.",
    algorithm:
      "Se calcula como availableDeliveryControllers / totalDeliveryControllers. En el mock actual el total se fija en 4 y los disponibles se generan dinámicamente entre 3 y 4.",
    interpretation:
      "Si todos los controllers están disponibles, el estado es correcto. Si alguno no está disponible, se considera una degradación importante del servicio."
  },
  disconnectedSessions: {
    description:
      "Representa sesiones que permanecen abiertas pero sin usuario conectado activamente.",
    algorithm:
      "Se genera de forma dinámica en CitrixService dentro de un rango controlado.",
    interpretation:
      "Un número elevado puede indicar sesiones huérfanas, consumo innecesario de recursos o necesidad de revisar políticas de cierre de sesión."
  },
  averageLogonDuration: {
    description:
      "Indica el tiempo medio de inicio de sesión en Citrix, expresado en segundos.",
    algorithm:
      "Se genera dinámicamente en CitrixService. El valor se evalúa con umbrales: correcto, advertencia o crítico.",
    interpretation:
      "Un tiempo alto puede indicar lentitud en perfiles, scripts de inicio, carga de servidores o problemas de infraestructura."
  },
  serverLoad: {
    description:
      "Representa la carga media simulada de los servidores Citrix.",
    algorithm:
      "Se genera en CitrixService como porcentaje dinámico. Se clasifica mediante umbrales de advertencia y criticidad.",
    interpretation:
      "Una carga elevada puede afectar al rendimiento de las sesiones y anticipar saturación de la plataforma."
  },
  failedLogons: {
    description:
      "Indica intentos de inicio de sesión fallidos en el entorno Citrix simulado.",
    algorithm:
      "Se genera dinámicamente en CitrixService y se usa como señal de degradación cuando supera determinados umbrales.",
    interpretation:
      "Un número elevado puede indicar problemas de autenticación, disponibilidad o acceso a recursos publicados."
  },
  citrixHealth: {
    description:
      "Resume el estado general del entorno Citrix mediante un semáforo GREEN, YELLOW o RED.",
    algorithm:
      "El índice se calcula mediante reglas heurísticas. Devuelve RED si algún Delivery Controller no está disponible, si el Average Logon Duration supera 40 segundos, si la carga de servidores supera el 85% o si los errores de inicio son mayores que 10. Devuelve YELLOW si el tiempo de inicio supera 25 segundos, la carga supera el 70% o los errores de inicio son mayores que 5. En caso contrario devuelve GREEN.",
    interpretation:
      "GREEN indica funcionamiento correcto, YELLOW indica degradación moderada y RED indica situación crítica que requiere revisión."
  }
};

function CitrixPage() {
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadCitrixDashboard = () => {
    fetch(`${API_BASE_URL}/citrix/summary`)
      .then((response) => {
        if (!response.ok) {
          throw new Error("No se pudo cargar el resumen Citrix");
        }

        return response.json();
      })
      .then((data) => {
        setSummary(data);
        setError(null);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error cargando Citrix:", error);
        setError("No se pudo conectar con el backend de Citrix.");
        setLoading(false);
      });
  };

  useEffect(() => {
    loadCitrixDashboard();

    const interval = setInterval(() => {
      loadCitrixDashboard();
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return (
      <main className="dashboard">
        <h1>Citrix</h1>
        <p className="loading">Cargando datos Citrix...</p>
      </main>
    );
  }

  const citrixHealthDetails = summary?.citrixHealthDetails;
  const citrixHealth =
    citrixHealthDetails?.color ?? summary?.citrixHealth ?? "UNKNOWN";
  const citrixReasons = citrixHealthDetails?.reasons ?? [];

  return (
    <main className="dashboard">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Monitorizacion Citrix</p>
          <h1>Citrix</h1>
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

      {summary ? (
        <>
        <section className={`status status-${citrixHealth.toLowerCase()}`}>
          <div className="status-main">
            <span>Indice de salud Citrix</span>
            <strong>Afeccion: {citrixHealthDetails?.percentage ?? 0} %</strong>
            <p>Estado: {formatCitrixStatus(citrixHealth)}</p>
          </div>

          <div className="status-reasons">
            <span>Motivos</span>
            {citrixReasons.length > 0 ? (
              <ul>
                {citrixReasons.slice(0, 4).map((reason) => (
                  <li key={reason}>{reason}</li>
                ))}
              </ul>
            ) : (
              <p>Sin motivos activos</p>
            )}
          </div>
        </section>

        <section className="dashboard-section">
          <div className="kpi-grid">
            <KpiCard
              title="Sesiones activas"
              value={summary.activeSessions}
              info={citrixKpiInfo.activeSessions}
            />

            <KpiCard
              title="Licencias activas"
              value={summary.activeLicenses}
              info={citrixKpiInfo.activeLicenses}
            />

            <KpiCard
              title="Delivery Controllers disponibles"
              value={`${summary.availableDeliveryControllers}/${summary.totalDeliveryControllers}`}
              status={
                summary.availableDeliveryControllers <
                summary.totalDeliveryControllers
                  ? "danger"
                  : "ok"
              }
              info={citrixKpiInfo.deliveryControllers}
            />

            <KpiCard
              title="Sesiones desconectadas"
              value={summary.disconnectedSessions}
              status={summary.disconnectedSessions > 25 ? "warning" : "ok"}
              info={citrixKpiInfo.disconnectedSessions}
            />

            <KpiCard
              title="Average Logon Duration"
              value={`${summary.averageLogonDurationSeconds}s`}
              status={
                summary.averageLogonDurationSeconds > 40
                  ? "danger"
                  : summary.averageLogonDurationSeconds > 25
                    ? "warning"
                    : "ok"
              }
              info={citrixKpiInfo.averageLogonDuration}
            />

            <KpiCard
              title="Carga de servidores"
              value={`${summary.serverLoadPercent}%`}
              status={
                summary.serverLoadPercent > 85
                  ? "danger"
                  : summary.serverLoadPercent > 70
                    ? "warning"
                    : "ok"
              }
              info={citrixKpiInfo.serverLoad}
            />

            <KpiCard
              title="Errores de inicio"
              value={summary.failedLogons}
              status={
                summary.failedLogons > 10
                  ? "danger"
                  : summary.failedLogons > 5
                    ? "warning"
                    : "ok"
              }
              info={citrixKpiInfo.failedLogons}
            />

          </div>
        </section>
        </>
      ) : (
        <p className="loading">No se han podido cargar los datos Citrix.</p>
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

function formatCitrixStatus(status) {
  if (status === "GREEN") {
    return "Verde";
  }

  if (status === "YELLOW") {
    return "Amarillo";
  }

  if (status === "RED") {
    return "Rojo";
  }

  return status;
}

export default CitrixPage;
