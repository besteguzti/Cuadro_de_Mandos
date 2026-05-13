import { useEffect, useState } from 'react'

import KpiCard from './components/KpiCard'
import KpiChart from './components/KpiChart'

function App() {

  // =========================
  // Estados React
  // =========================

  const [summary, setSummary] = useState(null)

  const [anomalies, setAnomalies] = useState([])
  const [wifiHistory, setWifiHistory] = useState([])

  const loadDashboard = () => {

  // =========================
  // Resumen Aruba
  // =========================

  fetch('http://localhost:8080/aruba/summary')
    .then(response => response.json())
    .then(data => {

      setSummary(data)
    })

  // =========================
  // Anomalías
  // =========================

  fetch('http://localhost:8080/kpis/anomalies')
    .then(response => response.json())
    .then(data => {

      setAnomalies(data)
    })

  // =========================
  // Histórico WiFi
  // =========================

  fetch('http://localhost:8080/kpis/name/wifiUsers')
    .then(response => response.json())
    .then(data => {

      const formattedData = data.map(item => ({

        ...item,

        value: Number(item.value)
      }))

      setWifiHistory(formattedData)
    })
}


   useEffect(() => {

  // Primera carga
  loadDashboard()

  // Refresco automático cada 5 segundos
  const interval = setInterval(() => {

    loadDashboard()

  }, 5000)

  // Limpiar interval al cerrar componente
  return () => clearInterval(interval)

}, [])

  // =========================
  // Pantalla carga
  // =========================

  if (!summary) {

    return <h1>Cargando dashboard...</h1>
  }

  // =========================
  // Render principal
  // =========================

  return (

    <div style={{ padding: '20px' }}>

      <h1>TFG Dashboard</h1>

      <h2>Estado Red: {summary.networkStatus}</h2>

      {/* =========================
           KPIs
      ========================= */}

      <div style={{

        display: 'flex',

        flexWrap: 'wrap',

        gap: '20px',

        alignItems: 'flex-start'
      }}>

        <KpiCard
          title="WiFi Users"
          value={summary.wifiUsers}
        />

        <KpiCard
          title="Remote Users"
          value={summary.remoteUsers}
        />

        <KpiCard
          title="APs degradados"
          value={summary.apsDegraded}
        />

        <KpiCard
          title="APs saturados"
          value={summary.apsSaturated}
        />

        <KpiCard
          title="APs caídos"
          value={summary.downAps}
        />

        <KpiCard
          title="Tráfico"
          value={summary.networkTraffic}
        />

      </div>

      {/* =========================
           Anomalías
      ========================= */}

      <h2 style={{ marginTop: '40px' }}>
        Anomalías detectadas
      </h2>

      {
        anomalies.length === 0

          ? <p>No hay anomalías</p>

          : anomalies.map((anomaly, index) => (

            <div
              key={index}

              style={{

                border: '1px solid red',

                borderRadius: '10px',

                padding: '10px',

                marginTop: '10px',

                width: '300px'
              }}
            >

              <h3>{anomaly.metric}</h3>

              <p>Valor: {anomaly.value}</p>

              <p>Severidad: {anomaly.severity}</p>

            </div>
          ))
      }

      {/* =========================
     HISTÓRICO
========================= */}

<h2 style={{ marginTop: '40px' }}>
  Histórico WiFi Users
</h2>

<KpiChart data={wifiHistory} />

    </div>
  )
}

export default App