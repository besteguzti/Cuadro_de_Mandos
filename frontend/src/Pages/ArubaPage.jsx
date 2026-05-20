import { useEffect, useState } from 'react'

import "../App.css";
import KpiCard from '../components/KpiCard'
import { API_BASE_URL } from '../config/api'

const arubaKpiInfo = {
  totalAps: {
    description: 'Número total de Access Points guardados en el dashboard.',
    algorithm: 'Se obtiene desde /aruba/summary a partir de los APs almacenados en MySQL tras la sincronización de Aruba.',
    interpretation: 'Permite conocer el tamaño del parque WiFi gestionado. Cambios bruscos pueden indicar altas, bajas o problemas de sincronización.'
  },
  upAps: {
    description: 'Access Points que aparecen como activos.',
    algorithm: 'Se calcula en backend a partir del estado de los APs almacenados y se expone en /aruba/summary.',
    interpretation: 'Un valor cercano al total indica buena disponibilidad de la red WiFi.'
  },
  downAps: {
    description: 'Access Points detectados como caídos o no operativos.',
    algorithm: 'Se calcula en backend comparando estados de AP almacenados y se expone en /aruba/summary.',
    interpretation: 'Si es mayor que cero conviene revisar cobertura, alimentación, conectividad o estado del equipo.'
  },
  firmwareOutdated: {
    description: 'APs con firmware pendiente de actualización.',
    algorithm: 'Se calcula con los datos de firmware sincronizados desde Aruba y guardados para el resumen.',
    interpretation: 'Un valor mayor que cero indica equipos pendientes de mantenimiento o actualización.'
  },
  inactiveAps: {
    description: 'APs que no han sido vistos recientemente según el histórico disponible.',
    algorithm: 'El backend calcula el valor a partir de la última actividad registrada por número de serie.',
    interpretation: 'Un valor mayor que cero puede indicar APs retirados, apagados o con problemas de comunicación.'
  },
  totalSwitches: {
    description: 'Número total de switches Aruba guardados en MySQL.',
    algorithm: 'Se obtiene desde /aruba/summary a partir de la tabla de switches sincronizada con Aruba.',
    interpretation: 'Permite conocer el inventario de switches gestionados por el dashboard.'
  },
  downSwitches: {
    description: 'Switches que aparecen apagados o no disponibles.',
    algorithm: 'Se calcula en backend a partir del estado de los switches almacenados.',
    interpretation: 'Si es mayor que cero debe revisarse disponibilidad, alimentación o conectividad de esos switches.'
  },
  switchesFirmwareUpgradeRequired: {
    description: 'Switches que necesitan actualización de firmware.',
    algorithm: 'Se calcula con los datos de firmware de switches sincronizados desde Aruba.',
    interpretation: 'Un valor mayor que cero indica mantenimiento pendiente en la capa de switching.'
  },
  underusedSwitches: {
    description: 'Switches considerados infrautilizados por interfaces down persistentes.',
    algorithm: 'El backend usa el histórico de interfaces down y muestra switches que superan el umbral definido durante el periodo analizado.',
    interpretation: 'Ayuda a detectar switches con poca utilización sostenida para revisar capacidad o redistribución.'
  },
  totalWifiClients: {
    description: 'Total de clientes WiFi observados por Aruba.',
    algorithm: 'Se calcula desde los clientes WiFi obtenidos por Aruba y agregados en el resumen almacenado.',
    interpretation: 'Indica actividad inalámbrica total observada en el momento de la sincronización.'
  },
  mutualiaApsClients: {
    description: 'Clientes WiFi asociados al grupo MUTUALIA-APs.',
    algorithm: 'Se filtran los clientes WiFi por group_name MUTUALIA-APs en los datos recibidos de Aruba.',
    interpretation: 'Permite separar actividad del grupo de APs respecto al resto de redes WiFi.'
  },
  mutualiaWifiClients: {
    description: 'Clientes WiFi asociados al grupo MUTUALIA-WIFI.',
    algorithm: 'Se filtran los clientes WiFi por group_name MUTUALIA-WIFI en los datos recibidos de Aruba.',
    interpretation: 'Permite ver la actividad del grupo principal de redes WiFi Mutualia.'
  },
  mutualiaLangileakClients: {
    description: 'Clientes conectados a la red MUTUALIA_LANGILEAK.',
    algorithm: 'Se filtran clientes del grupo MUTUALIA-WIFI por el campo network MUTUALIA_LANGILEAK.',
    interpretation: 'Un valor alto indica mayor uso de esta red específica.'
  },
  mutualiaClients: {
    description: 'Clientes conectados a la red MUTUALIA.',
    algorithm: 'Se filtran clientes del grupo MUTUALIA-WIFI por el campo network MUTUALIA.',
    interpretation: 'Permite analizar la actividad concreta de la red MUTUALIA.'
  },
  mutualiaRedInternaClients: {
    description: 'Clientes conectados a MUTUALIA_RED_INTERNA.',
    algorithm: 'Se filtran clientes del grupo MUTUALIA-WIFI por el campo network MUTUALIA_RED_INTERNA.',
    interpretation: 'Ayuda a controlar el uso de la red interna.'
  },
  mutualiaRedExternaClients: {
    description: 'Clientes conectados a MUTUALIA_RED_EXTERNA.',
    algorithm: 'Se filtran clientes del grupo MUTUALIA-WIFI por el campo network MUTUALIA_RED_EXTERNA.',
    interpretation: 'Ayuda a controlar el uso de la red externa.'
  },
  mutualiaKorporatiboaClients: {
    description: 'Clientes conectados a MUTUALIA_KORPORATIBOA.',
    algorithm: 'Se filtran clientes del grupo MUTUALIA-WIFI por el campo network MUTUALIA_KORPORATIBOA.',
    interpretation: 'Permite analizar el uso de esta red corporativa específica.'
  },
  wifiPacsClients: {
    description: 'Clientes conectados a la red WIFI_PACs.',
    algorithm: 'Se filtran clientes del grupo MUTUALIA-WIFI por el campo network WIFI_PACs.',
    interpretation: 'Permite controlar la actividad de la red PACS.'
  },
  mutVideoClients: {
    description: 'Clientes conectados a la red MUT_VIDEO.',
    algorithm: 'Se filtran clientes del grupo MUTUALIA-WIFI por el campo network MUT_VIDEO.',
    interpretation: 'Permite controlar la actividad de la red de vídeo.'
  }
}

function ArubaPage() {

  const [summary, setSummary] = useState(null)
  const [error, setError] = useState(null)

  const loadDashboard = () => {

    fetch(`${API_BASE_URL}/aruba/summary`)
      .then(response => {

        if (!response.ok) {

          throw new Error('No se pudo cargar el resumen Aruba')
        }

        return response.json()
      })
      .then(data => {

        setSummary(data)
        setError(null)
      })
      .catch(() => {

        setError('No se pudo conectar con el backend o Aruba Central.')
      })
  }

  useEffect(() => {

    loadDashboard()

    const interval = setInterval(() => {

      loadDashboard()

    }, 30000)

    return () => clearInterval(interval)

  }, [])

  if (!summary && !error) {

    return (
      <main className="dashboard">
        <h1>TFG Dashboard</h1>
        <p className="loading">Cargando dashboard...</p>
      </main>
    )
  }

  const status = summary?.networkStatus ?? 'UNKNOWN'

  const apCards = summary
    ? [
      { title: 'Total APs', value: summary.totalAps, info: arubaKpiInfo.totalAps },
      { title: 'APs activos', value: summary.upAps, info: arubaKpiInfo.upAps },
      { title: 'APs caidos', value: summary.downAps, info: arubaKpiInfo.downAps },
      {
        title: 'Firmware pendiente',
        value: summary.firmwareOutdated,
        critical: summary.firmwareOutdated > 0,
        info: arubaKpiInfo.firmwareOutdated
      },
      {
        title: 'APs inactivos',
        value: summary.inactiveAps,
        critical: summary.inactiveAps > 0,
        info: arubaKpiInfo.inactiveAps
      }
    ]
    : []

  const switchCards = summary
    ? [
      { title: 'Total switches', value: summary.totalSwitches, info: arubaKpiInfo.totalSwitches },
      {
        title: 'Switches apagados',
        value: summary.downSwitches,
        critical: summary.downSwitches > 0,
        info: arubaKpiInfo.downSwitches
      },
      {
        title: 'Switches con upgrade',
        value: summary.switchesFirmwareUpgradeRequired,
        critical: summary.switchesFirmwareUpgradeRequired > 0,
        info: arubaKpiInfo.switchesFirmwareUpgradeRequired
      }
    ]
    : []

  const underusedSwitchCards = summary
    ? (summary.underusedSwitches ?? []).map(switchUsage => ({
      title: switchUsage.associatedDeviceName || switchUsage.associatedDevice,
      value: `${switchUsage.downInterfaces} interfaces down`,
      critical: switchUsage.downInterfaces > 0,
      info: arubaKpiInfo.underusedSwitches
    }))
    : []

  const wifiGroupCards = summary
    ? [
      { title: 'Total clientes WiFi', value: summary.totalWifiClients, info: arubaKpiInfo.totalWifiClients },
      { title: 'Clientes MUTUALIA-APs', value: summary.mutualiaApsClients, info: arubaKpiInfo.mutualiaApsClients },
      { title: 'Clientes MUTUALIA-WIFI', value: summary.mutualiaWifiClients, info: arubaKpiInfo.mutualiaWifiClients }
    ]
    : []

  const wifiNetworkCards = summary
    ? [
      { title: 'MUTUALIA_LANGILEAK', value: summary.mutualiaLangileakClients, info: arubaKpiInfo.mutualiaLangileakClients },
      { title: 'MUTUALIA', value: summary.mutualiaClients, info: arubaKpiInfo.mutualiaClients },
      { title: 'MUTUALIA_RED_INTERNA', value: summary.mutualiaRedInternaClients, info: arubaKpiInfo.mutualiaRedInternaClients },
      { title: 'MUTUALIA_RED_EXTERNA', value: summary.mutualiaRedExternaClients, info: arubaKpiInfo.mutualiaRedExternaClients },
      { title: 'MUTUALIA_KORPORATIBOA', value: summary.mutualiaKorporatiboaClients, info: arubaKpiInfo.mutualiaKorporatiboaClients },
      { title: 'WIFI_PACs', value: summary.wifiPacsClients, info: arubaKpiInfo.wifiPacsClients },
      { title: 'MUT_VIDEO', value: summary.mutVideoClients, info: arubaKpiInfo.mutVideoClients }
    ]
    : []

  return (
    <main className="dashboard">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Monitorizacion Aruba</p>
          <h1>TFG Dashboard</h1>
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
        <>
          <section className={`status status-${status.toLowerCase()}`}>
            <span>Estado red</span>
            <strong>{status}</strong>
          </section>

          <section className="dashboard-section">
            <h2>Access Points</h2>

            <div className="kpi-grid">
              {apCards.map(card => (
                <KpiCard
                  key={card.title}
                  title={card.title}
                  value={card.value}
                  critical={card.critical}
                  info={card.info}
                />
              ))}
            </div>
          </section>

          <section className="dashboard-section">
            <h2>Clientes WiFi por grupo</h2>

            <div className="kpi-grid">
              {wifiGroupCards.map(card => (
                <KpiCard
                  key={card.title}
                  title={card.title}
                  value={card.value}
                  info={card.info}
                />
              ))}
            </div>
          </section>

          <section className="dashboard-section">
            <h2>Clientes MUTUALIA-WIFI por red</h2>

            <div className="kpi-grid">
              {wifiNetworkCards.map(card => (
                <KpiCard
                  key={card.title}
                  title={card.title}
                  value={card.value}
                  info={card.info}
                />
              ))}
            </div>
          </section>

          <section className="dashboard-section">
            <h2>Switches</h2>

            <div className="kpi-grid">
              {switchCards.map(card => (
                <KpiCard
                  key={card.title}
                  title={card.title}
                  value={card.value}
                  critical={card.critical}
                  info={card.info}
                />
              ))}
            </div>
          </section>

          <section className="dashboard-section">
            <h2>Switches infrautilizados</h2>

            <div className="kpi-grid">
              {underusedSwitchCards.length > 0 ? (
                underusedSwitchCards.map(card => (
                  <KpiCard
                    key={card.title}
                    title={card.title}
                    value={card.value}
                    critical={card.critical}
                    info={card.info}
                  />
                ))
              ) : (
                <KpiCard
                  title="Switches infrautilizados"
                  value="0"
                  info={arubaKpiInfo.underusedSwitches}
                />
              )}
            </div>
          </section>
        </>
      )}
    </main>
  )
}

function formatSnapshotDate(value) {
  if (!value) {
    return "Sin datos";
  }

  return new Date(value).toLocaleString();
}

export default ArubaPage
