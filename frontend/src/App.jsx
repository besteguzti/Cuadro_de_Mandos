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

  const cards = summary
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

          <section className="kpi-grid">
            {cards.map(card => (
              <KpiCard
                key={card.title}
                title={card.title}
                value={card.value}
              />
            ))}
          </section>
        </>
      )}
    </main>
  )
}

export default App
