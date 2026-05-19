import { useEffect, useState } from 'react'

import './App.css'
import KpiCard from './components/KpiCard'

const API_BASE_URL = 'http://localhost:8080'

function App() {

  const [summary, setSummary] = useState(null)
  const [lastUpdated, setLastUpdated] = useState(null)
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
        setLastUpdated(new Date())
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
      { title: 'Total APs', value: summary.totalAps },
      { title: 'APs activos', value: summary.upAps },
      { title: 'APs caidos', value: summary.downAps },
      { title: 'Sites', value: summary.totalSites },
      { title: 'Swarms', value: summary.totalSwarms },
      { title: 'Firmware pendiente', value: summary.firmwareOutdated },
      { title: 'APs sin IP publica', value: summary.apsWithoutPublicIp },
      { title: 'APs inactivos', value: summary.inactiveAps }
    ]
    : []

  const switchCards = summary
    ? [
      { title: 'Total switches', value: summary.totalSwitches },
      {
        title: 'Switches apagados',
        value: summary.downSwitches,
        critical: summary.downSwitches > 0
      },
      {
        title: 'Switches con upgrade',
        value: summary.switchesFirmwareUpgradeRequired,
        critical: summary.switchesFirmwareUpgradeRequired > 0
      }
    ]
    : []

  const underusedSwitchCards = summary
    ? (summary.underusedSwitches ?? []).map(switchUsage => ({
      title: switchUsage.associatedDeviceName || switchUsage.associatedDevice,
      value: `${switchUsage.wiredClients} clientes`,
      critical: switchUsage.wiredClients === 0
    }))
    : []

  const wifiGroupCards = summary
    ? [
      { title: 'Total clientes WiFi', value: summary.totalWifiClients },
      { title: 'Clientes MUTUALIA-APs', value: summary.mutualiaApsClients },
      { title: 'Clientes MUTUALIA-WIFI', value: summary.mutualiaWifiClients }
    ]
    : []

  const wifiNetworkCards = summary
    ? [
      { title: 'MUTUALIA_LANGILEAK', value: summary.mutualiaLangileakClients },
      { title: 'MUTUALIA', value: summary.mutualiaClients },
      { title: 'MUTUALIA_RED_INTERNA', value: summary.mutualiaRedInternaClients },
      { title: 'MUTUALIA_RED_EXTERNA', value: summary.mutualiaRedExternaClients },
      { title: 'MUTUALIA_KORPORATIBOA', value: summary.mutualiaKorporatiboaClients },
      { title: 'WIFI_PACs', value: summary.wifiPacsClients },
      { title: 'MUT_VIDEO', value: summary.mutVideoClients }
    ]
    : []

  return (
    <main className="dashboard">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Monitorizacion Aruba</p>
          <h1>TFG Dashboard</h1>
        </div>

        {lastUpdated && (
          <p className="updated">
            Ultima actualizacion: {lastUpdated.toLocaleTimeString()}
          </p>
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
                  />
                ))
              ) : (
                <KpiCard
                  title="Switches infrautilizados"
                  value="0"
                />
              )}
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
                />
              ))}
            </div>
          </section>
        </>
      )}
    </main>
  )
}

export default App
