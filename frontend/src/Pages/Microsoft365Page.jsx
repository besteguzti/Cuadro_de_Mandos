import { useEffect, useState } from "react";

import "../App.css";

import KpiCard from "../components/KpiCard";
import { API_BASE_URL } from "../config/api";

const microsoft365KpiInfo = {
  activeUsers: {
    description:
      "Representa usuarios activos simulados en Microsoft 365.",
    algorithm:
      "Se obtiene como dato dinámico desde Microsoft365Service para simular actividad de usuarios en la plataforma.",
    interpretation:
      "Un valor alto indica mayor actividad general. No implica necesariamente usuarios únicos conectados en tiempo real."
  },
  unassignedLicenses: {
    description:
      "Indica licencias disponibles que no están asignadas a usuarios.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service dentro de un rango controlado.",
    interpretation:
      "Un valor bajo puede indicar presión de licenciamiento. Un valor alto puede indicar capacidad disponible o licencias infrautilizadas."
  },
  outlookStatus: {
    description:
      "Muestra el estado simulado del servicio Outlook.",
    algorithm:
      "Microsoft365Service genera un estado dinámico: HEALTHY, DEGRADED o INCIDENT.",
    interpretation:
      "HEALTHY indica funcionamiento correcto, DEGRADED degradación parcial e INCIDENT una incidencia relevante."
  },
  teamsStatus: {
    description:
      "Muestra el estado simulado del servicio Teams.",
    algorithm:
      "Microsoft365Service genera un estado dinámico: HEALTHY, DEGRADED o INCIDENT.",
    interpretation:
      "HEALTHY indica funcionamiento correcto, DEGRADED degradación parcial e INCIDENT una incidencia relevante."
  },
  sharePointStatus: {
    description:
      "Muestra el estado simulado del servicio SharePoint.",
    algorithm:
      "Microsoft365Service genera un estado dinámico: HEALTHY, DEGRADED o INCIDENT.",
    interpretation:
      "HEALTHY indica funcionamiento correcto, DEGRADED degradación parcial e INCIDENT una incidencia relevante."
  },
  nearlyFullMailboxes: {
    description:
      "Indica buzones próximos a quedarse sin capacidad.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y se marca como advertencia cuando supera el umbral usado por la tarjeta.",
    interpretation:
      "Un valor alto puede anticipar incidencias de recepción o envío de correo por falta de espacio."
  },
  emailsQuarantined: {
    description:
      "Muestra correos simulados retenidos en cuarentena.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y se considera advertencia cuando el volumen supera 100.",
    interpretation:
      "Un valor alto puede indicar campañas maliciosas, filtros más restrictivos o mayor exposición a correo sospechoso."
  },
  sharePointStoragePercent: {
    description:
      "Representa el porcentaje simulado de almacenamiento usado en SharePoint.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y se clasifica con umbrales: warning por encima de 80% y danger por encima de 90%.",
    interpretation:
      "Valores altos indican presión de capacidad y posible necesidad de limpieza o ampliación."
  },
  riskyUsers: {
    description:
      "Cuenta usuarios simulados con señales de riesgo de identidad.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y cualquier valor superior a cero se considera crítico en la tarjeta.",
    interpretation:
      "Un valor mayor que cero requiere revisión de identidad, actividad sospechosa o controles de acceso."
  },
  failedSignIns: {
    description:
      "Indica intentos de inicio de sesión fallidos.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y se marca como advertencia cuando supera 400.",
    interpretation:
      "Un valor alto puede indicar errores de usuario, ataques de fuerza bruta o problemas de autenticación."
  },
  usersWithoutMfa: {
    description:
      "Cuenta usuarios simulados sin autenticación multifactor.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y se marca como crítico cuando supera 20.",
    interpretation:
      "Un valor alto aumenta la exposición ante robo de credenciales y debería reducirse."
  },
  appsSecretsExpiringSoon: {
    description:
      "Indica secretos de aplicaciones próximos a caducar.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y cualquier valor superior a cero se muestra como advertencia.",
    interpretation:
      "Un valor alto puede anticipar interrupciones en integraciones si no se renuevan los secretos."
  },
  unusedApplications: {
    description:
      "Cuenta aplicaciones empresariales simuladas sin uso relevante.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y se marca como advertencia cuando supera 10.",
    interpretation:
      "Un valor alto puede indicar sobreconfiguración o aplicaciones que conviene revisar o retirar."
  },
  highPrivilegeApplications: {
    description:
      "Cuenta aplicaciones con permisos elevados.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y se marca como crítico cuando supera 5.",
    interpretation:
      "Un valor alto incrementa el riesgo de seguridad y requiere revisión de permisos concedidos."
  },
  nonCompliantDevices: {
    description:
      "Indica equipos que no cumplen las políticas simuladas de Intune.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y se marca como crítico cuando supera 20.",
    interpretation:
      "Un valor alto puede implicar dispositivos con configuración insegura o fuera de estándar."
  },
  outdatedWindowsDevices: {
    description:
      "Cuenta dispositivos Windows simulados con versiones desactualizadas.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y se marca como advertencia cuando supera 15.",
    interpretation:
      "Un valor alto aumenta riesgo operativo y de seguridad por falta de parches o versiones antiguas."
  },
  devicesWithoutEncryption: {
    description:
      "Indica equipos sin cifrado de disco.",
    algorithm:
      "Se obtiene desde Microsoft365Service y cualquier valor superior a cero se considera crítico.",
    interpretation:
      "Un valor mayor que cero supone riesgo de exposición de datos si se pierde o roba un equipo."
  },
  staleDevices: {
    description:
      "Cuenta dispositivos sin check-in durante más de 90 días.",
    algorithm:
      "Se genera dinámicamente en Microsoft365Service y se marca como advertencia cuando supera 10.",
    interpretation:
      "Un valor alto puede indicar inventario obsoleto, equipos fuera de uso o dispositivos que han dejado de reportar."
  },
  microsoft365Health: {
    description:
      "Resume el estado general simulado de Microsoft 365 mediante GREEN, YELLOW o RED.",
    algorithm:
      "Microsoft365Service calcula un riesgo operativo y lo traduce a GREEN, YELLOW o RED según umbrales.",
    interpretation:
      "GREEN indica estabilidad, YELLOW degradación moderada y RED una situación que requiere atención prioritaria."
  },
  microsoft365OperationalRisk: {
    description:
      "Índice simulado de riesgo operativo de Microsoft 365 en escala 0-100.",
    algorithm:
      "Combina señales de servicios, identidad, seguridad, aplicaciones, dispositivos y almacenamiento en Microsoft365Service.",
    interpretation:
      "Valores bajos indican estabilidad, valores medios requieren seguimiento y valores altos indican riesgo operativo elevado."
  }
};

function Microsoft365Page() {
  // =========================
  // Estado resumen M365
  // =========================
  //
  // Aquí se almacenan los KPIs
  // recibidos desde el backend.
  //
  const [summary, setSummary] = useState(null);

  // =========================
  // Estado de carga
  // =========================
  //
  // Permite mostrar mensaje
  // mientras se consulta la API.
  //
  const [loading, setLoading] = useState(true);

  // =========================
  // Carga datos Microsoft 365
  // =========================
  //
  // Consume:
  // GET /microsoft365/summary
  //
  useEffect(() => {
    fetch(`${API_BASE_URL}/microsoft365/summary`)
      .then((response) => response.json())
      .then((data) => {
        setSummary(data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error cargando Microsoft 365:", error);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <main className="dashboard">
        <h1>Microsoft 365</h1>
        <p className="loading">Cargando datos Microsoft 365...</p>
      </main>
    );
  }

  if (!summary) {
    return (
      <main className="dashboard">
        <h1>Microsoft 365</h1>
        <p className="loading">No se han podido cargar los datos de Microsoft 365.</p>
      </main>
    );
  }

  const microsoft365HealthDetails = summary.microsoft365HealthDetails;
  const microsoft365Health =
    microsoft365HealthDetails?.color ?? summary.microsoft365Health ?? "UNKNOWN";
  const microsoft365Reasons = microsoft365HealthDetails?.reasons ?? [];

  return (
    <main className="dashboard">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Monitorizacion Microsoft 365</p>
          <h1>Microsoft 365</h1>
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

      <section className={`status status-${microsoft365Health.toLowerCase()}`}>
        <div className="status-main">
          <span>Indice de salud Microsoft 365</span>
          <strong>Afeccion: {microsoft365HealthDetails?.percentage ?? 0} %</strong>
          <p>Estado: {formatMicrosoft365Status(microsoft365Health)}</p>
        </div>

        <div className="status-reasons">
          <span>Motivos</span>
          {microsoft365Reasons.length > 0 ? (
            <ul>
              {microsoft365Reasons.slice(0, 4).map((reason) => (
                <li key={reason}>{reason}</li>
              ))}
            </ul>
          ) : (
            <p>Sin motivos activos</p>
          )}
        </div>
      </section>

      <section className="dashboard-section">

      <p>
        Supervisión simulada de servicios, seguridad, aplicaciones,
        licenciamiento e Intune.
      </p>

      <h2>Uso y licenciamiento</h2>

      <div className="kpi-grid">
        <KpiCard
          title="Usuarios activos"
          value={summary.activeUsers}
          status="neutral"
          info={microsoft365KpiInfo.activeUsers}
        />

        <KpiCard
          title="Licencias no asignadas"
          value={summary.unassignedLicenses}
          status={summary.unassignedLicenses < 20 ? "warning" : "ok"}
          info={microsoft365KpiInfo.unassignedLicenses}
        />
      </div>

      </section>

      <section className="dashboard-section">
      <h2>Estado de servicios</h2>

      <div className="kpi-grid">
        <KpiCard
          title="Outlook"
          value={summary.outlookStatus}
          status={getServiceStatus(summary.outlookStatus)}
          info={microsoft365KpiInfo.outlookStatus}
        />

        <KpiCard
          title="Teams"
          value={summary.teamsStatus}
          status={getServiceStatus(summary.teamsStatus)}
          info={microsoft365KpiInfo.teamsStatus}
        />

        <KpiCard
          title="SharePoint"
          value={summary.sharePointStatus}
          status={getServiceStatus(summary.sharePointStatus)}
          info={microsoft365KpiInfo.sharePointStatus}
        />
      </div>

      </section>

      <section className="dashboard-section">
      <h2>Exchange / SharePoint</h2>

      <div className="kpi-grid">
        <KpiCard
          title="Buzones casi llenos"
          value={summary.nearlyFullMailboxes}
          status={summary.nearlyFullMailboxes > 20 ? "warning" : "ok"}
          info={microsoft365KpiInfo.nearlyFullMailboxes}
        />

        <KpiCard
          title="Emails en cuarentena"
          value={summary.emailsQuarantined}
          status={summary.emailsQuarantined > 100 ? "warning" : "ok"}
          info={microsoft365KpiInfo.emailsQuarantined}
        />

        <KpiCard
          title="Almacenamiento SharePoint"
          value={`${summary.sharePointStoragePercent}%`}
          status={
            summary.sharePointStoragePercent > 90
              ? "danger"
              : summary.sharePointStoragePercent > 80
                ? "warning"
                : "ok"
          }
          info={microsoft365KpiInfo.sharePointStoragePercent}
        />
      </div>

      </section>

      <section className="dashboard-section">
      <h2>Seguridad e identidad</h2>

      <div className="kpi-grid">
        <KpiCard
          title="Usuarios en riesgo"
          value={summary.riskyUsers}
          status={summary.riskyUsers > 0 ? "danger" : "ok"}
          info={microsoft365KpiInfo.riskyUsers}
        />

        <KpiCard
          title="Inicios fallidos"
          value={summary.failedSignIns}
          status={summary.failedSignIns > 400 ? "warning" : "ok"}
          info={microsoft365KpiInfo.failedSignIns}
        />

        <KpiCard
          title="Usuarios sin MFA"
          value={summary.usersWithoutMfa}
          status={summary.usersWithoutMfa > 20 ? "danger" : "ok"}
          info={microsoft365KpiInfo.usersWithoutMfa}
        />
      </div>

      </section>

      <section className="dashboard-section">
      <h2>Aplicaciones empresariales</h2>

      <div className="kpi-grid">
        <KpiCard
          title="Secrets próximos a caducar"
          value={summary.appsSecretsExpiringSoon}
          status={summary.appsSecretsExpiringSoon > 0 ? "warning" : "ok"}
          info={microsoft365KpiInfo.appsSecretsExpiringSoon}
        />

        <KpiCard
          title="Aplicaciones sin uso"
          value={summary.unusedApplications}
          status={summary.unusedApplications > 10 ? "warning" : "ok"}
          info={microsoft365KpiInfo.unusedApplications}
        />

        <KpiCard
          title="Apps permisos elevados"
          value={summary.highPrivilegeApplications}
          status={summary.highPrivilegeApplications > 5 ? "danger" : "ok"}
          info={microsoft365KpiInfo.highPrivilegeApplications}
        />
      </div>

      </section>

      <section className="dashboard-section">
      <h2>Intune / Endpoint Manager</h2>

      <div className="kpi-grid">
        <KpiCard
          title="Equipos no conformes"
          value={summary.nonCompliantDevices}
          status={summary.nonCompliantDevices > 20 ? "danger" : "ok"}
          info={microsoft365KpiInfo.nonCompliantDevices}
        />

        <KpiCard
          title="Windows desactualizados"
          value={summary.outdatedWindowsDevices}
          status={summary.outdatedWindowsDevices > 15 ? "warning" : "ok"}
          info={microsoft365KpiInfo.outdatedWindowsDevices}
        />

        <KpiCard
          title="Equipos sin cifrado"
          value={summary.devicesWithoutEncryption}
          status={summary.devicesWithoutEncryption > 0 ? "danger" : "ok"}
          info={microsoft365KpiInfo.devicesWithoutEncryption}
        />

        <KpiCard
          title="Sin check-in >90 días"
          value={summary.staleDevices}
          status={summary.staleDevices > 10 ? "warning" : "ok"}
          info={microsoft365KpiInfo.staleDevices}
        />
      </div>

      </section>

      {false && (
      <section className="dashboard-section">
      <h2>KPIs compuestos</h2>

      <div className="kpi-grid">
        <KpiCard
          title="Índice salud Microsoft 365"
          value={summary.microsoft365Health}
          status={
            summary.microsoft365Health === "RED"
              ? "danger"
              : summary.microsoft365Health === "YELLOW"
                ? "warning"
                : "ok"
          }
          info={microsoft365KpiInfo.microsoft365Health}
        />

        <KpiCard
          title="Riesgo operativo Microsoft"
          value={`${summary.microsoft365OperationalRisk}%`}
          status={
            summary.microsoft365OperationalRisk >= 60
              ? "danger"
              : summary.microsoft365OperationalRisk >= 30
                ? "warning"
                : "ok"
          }
          info={microsoft365KpiInfo.microsoft365OperationalRisk}
        />
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

function formatMicrosoft365Status(status) {
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

// =========================
// Estado visual servicios
// =========================
//
// Convierte el estado lógico
// de Microsoft 365 al estilo
// visual de las tarjetas.
//
function getServiceStatus(status) {
  if (status === "INCIDENT") {
    return "danger";
  }

  if (status === "DEGRADED") {
    return "warning";
  }

  return "ok";
}

export default Microsoft365Page;
