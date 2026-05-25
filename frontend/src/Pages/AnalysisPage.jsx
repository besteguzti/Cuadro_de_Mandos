import { useEffect, useMemo, useState } from "react";

import "../App.css";

import { API_BASE_URL } from "../config/api";

const periods = [
  { label: "7 dias", value: "7d" },
  { label: "30 dias", value: "30d" },
  { label: "90 dias", value: "90d" }
];

function AnalysisPage() {
  const [kpis, setKpis] = useState([]);
  const [selectedX, setSelectedX] = useState(null);
  const [selectedY, setSelectedY] = useState(null);
  const [period, setPeriod] = useState("30d");
  const [comparison, setComparison] = useState(null);
  const [loading, setLoading] = useState(true);
  const [chartLoading, setChartLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch(`${API_BASE_URL}/api/analytics/transversal-kpis`)
      .then((response) => {
        if (!response.ok) {
          throw new Error("No se pudieron cargar los KPIs transversales");
        }

        return response.json();
      })
      .then((data) => {
        setKpis(data);
        setError(null);
        setLoading(false);
      })
      .catch(() => {
        setError("No se han podido cargar los datos del analisis.");
        setLoading(false);
      });
  }, []);

  useEffect(() => {
    if (!selectedX || !selectedY) {
      setComparison(null);
      return;
    }

    setChartLoading(true);

    fetch(
      `${API_BASE_URL}/api/analytics/compare?kpiX=${selectedX.code}&kpiY=${selectedY.code}&period=${period}`
    )
      .then((response) => {
        if (!response.ok) {
          throw new Error("No se pudo cargar la comparacion");
        }

        return response.json();
      })
      .then((data) => {
        setComparison(data);
        setError(null);
        setChartLoading(false);
      })
      .catch(() => {
        setError("No se han podido cargar los datos del analisis.");
        setChartLoading(false);
      });
  }, [selectedX, selectedY, period]);

  const selectedXRelatedCodes = useMemo(() => {
    if (!selectedX) {
      return [];
    }

    return selectedX.relatedKpis ?? [];
  }, [selectedX]);

  const handleKpiClick = (kpi) => {
    if (!selectedX) {
      setSelectedX(kpi);
      return;
    }

    if (selectedX.code === kpi.code) {
      resetSelection();
      return;
    }

    if (selectedY?.code === kpi.code) {
      setSelectedY(null);
      return;
    }

    if (!selectedXRelatedCodes.includes(kpi.code)) {
      return;
    }

    setSelectedY(kpi);
  };

  const resetSelection = () => {
    setSelectedX(null);
    setSelectedY(null);
    setComparison(null);
  };

  if (loading) {
    return (
      <main className="dashboard">
        <h1>Análisis exploratorio de KPIs transversales</h1>
        <p className="loading">Cargando KPIs transversales...</p>
      </main>
    );
  }

  return (
    <main className="dashboard">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Modulo de análisis</p>
          <h1>Análisis exploratorio de KPIs transversales</h1>
        </div>

        <div className="analysis-actions">
          <label>
            Periodo
            <select
              value={period}
              onChange={(event) => setPeriod(event.target.value)}
            >
              {periods.map((item) => (
                <option key={item.value} value={item.value}>
                  {item.label}
                </option>
              ))}
            </select>
          </label>

          <button type="button" onClick={resetSelection}>
            Reiniciar analisis
          </button>
        </div>
      </header>

      {error && (
        <section className="alert" role="alert">
          {error}
        </section>
      )}

      <section className="dashboard-section">
        <h2>KPIs transversales disponibles</h2>

        <div className="analysis-kpi-grid">
          {kpis.map((kpi) => (
            <KpiSelectorCard
              key={kpi.code}
              kpi={kpi}
              selectedX={selectedX}
              selectedY={selectedY}
              isRelated={
                !selectedX || selectedXRelatedCodes.includes(kpi.code)
              }
              onClick={() => handleKpiClick(kpi)}
            />
          ))}
        </div>
      </section>

      <section className="analysis-selection-panel">
        <span>Eje X: {selectedX?.name ?? "Sin seleccionar"}</span>
        <span>Eje Y: {selectedY?.name ?? "Sin seleccionar"}</span>
      </section>

      {selectedX && selectedY && (
        <section className="dashboard-section">
          {chartLoading ? (
            <p className="loading">Cargando comparacion...</p>
          ) : comparison?.points?.length > 0 ? (
            <div className="analysis-layout">
              <KpiRelationshipChart
                xName={comparison.kpiXName}
                yName={comparison.kpiYName}
                xUnit={selectedX.unit}
                yUnit={selectedY.unit}
                points={comparison.points}
              />

              <AnalysisSummary
                comparison={comparison}
                selectedX={selectedX}
                selectedY={selectedY}
              />
            </div>
          ) : (
            <p className="loading">
              No hay datos suficientes para generar la comparacion seleccionada.
            </p>
          )}
        </section>
      )}
    </main>
  );
}

function KpiSelectorCard({
  kpi,
  selectedX,
  selectedY,
  isRelated,
  onClick
}) {
  const isSelectedX = selectedX?.code === kpi.code;
  const isSelectedY = selectedY?.code === kpi.code;
  const disabled = selectedX && !isSelectedX && !isSelectedY && !isRelated;

  return (
    <button
      type="button"
      className={[
        "analysis-kpi-card",
        isSelectedX ? "selected-x" : "",
        isSelectedY ? "selected-y" : "",
        disabled ? "disabled" : ""
      ].join(" ")}
      onClick={onClick}
      disabled={disabled}
    >
      <span className="analysis-kpi-name">{kpi.name}</span>
      <span className="analysis-kpi-description">{kpi.description}</span>
      <strong>
        {formatValue(kpi.currentValue)}
        {kpi.unit ? ` ${kpi.unit}` : ""}
      </strong>
      {isSelectedX && <span className="axis-badge">Eje X</span>}
      {isSelectedY && <span className="axis-badge">Eje Y</span>}
    </button>
  );
}

function KpiRelationshipChart({ xName, yName, xUnit, yUnit, points }) {
  const width = 720;
  const height = 420;
  const padding = 56;
  const ticks = [0, 25, 50, 75, 100];
  const trendLine = calculateTrendLine(points);

  const scaleX = (value) =>
    padding + (value / 100) * (width - padding * 2);

  const scaleY = (value) =>
    height - padding - (value / 100) * (height - padding * 2);

  return (
    <div className="analysis-chart-card">
      <p className="analysis-chart-explanation">
        Cada punto representa una captura histórica. El eje X muestra el valor
        del KPI seleccionado como variable principal y el eje Y muestra el valor
        del KPI relacionado en esa misma captura.
      </p>

      <svg
        className="analysis-chart"
        viewBox={`0 0 ${width} ${height}`}
        role="img"
        aria-label={`Grafica de dispersion ${xName} frente a ${yName}`}
      >
        <line x1={padding} y1={height - padding} x2={width - padding} y2={height - padding} />
        <line x1={padding} y1={padding} x2={padding} y2={height - padding} />

        {ticks.map((tick) => (
          <g key={`grid-${tick}`}>
            <line
              className="analysis-grid-line"
              x1={scaleX(tick)}
              y1={padding}
              x2={scaleX(tick)}
              y2={height - padding}
            />
            <line
              className="analysis-grid-line"
              x1={padding}
              y1={scaleY(tick)}
              x2={width - padding}
              y2={scaleY(tick)}
            />
          </g>
        ))}

        <text x={width / 2} y={height - 14} textAnchor="middle">
          {xName} ({xUnit})
        </text>
        <text
          x={18}
          y={height / 2}
          textAnchor="middle"
          transform={`rotate(-90 18 ${height / 2})`}
        >
          {yName} ({yUnit})
        </text>

        {ticks.map((tick) => (
          <text
            key={`x-tick-${tick}`}
            x={scaleX(tick)}
            y={height - padding + 24}
            textAnchor="middle"
          >
            {tick}
          </text>
        ))}

        {ticks.map((tick) => (
          <text
            key={`y-tick-${tick}`}
            x={padding - 12}
            y={scaleY(tick) + 4}
            textAnchor="end"
          >
            {tick}
          </text>
        ))}

        {trendLine && (
          <line
            className="analysis-trend-line"
            x1={scaleX(trendLine.x1)}
            y1={scaleY(trendLine.y1)}
            x2={scaleX(trendLine.x2)}
            y2={scaleY(trendLine.y2)}
          />
        )}

        {points.map((point, index) => (
          <circle
            key={`${point.timestamp}-${index}`}
            cx={scaleX(point.x)}
            cy={scaleY(point.y)}
            r="6"
          >
            <title>
              {`${new Date(point.timestamp).toLocaleString()} | ${xName}: ${formatWithUnit(point.x, xUnit)} | ${yName}: ${formatWithUnit(point.y, yUnit)}`}
            </title>
          </circle>
        ))}
      </svg>

      <div className="analysis-table-wrapper">
        <table className="analysis-points-table">
          <thead>
            <tr>
              <th>Fecha</th>
              <th>{xName}</th>
              <th>{yName}</th>
            </tr>
          </thead>
          <tbody>
            {points.map((point, index) => (
              <tr key={`row-${point.timestamp}-${index}`}>
                <td>{new Date(point.timestamp).toLocaleString()}</td>
                <td>{formatWithUnit(point.x, xUnit)}</td>
                <td>{formatWithUnit(point.y, yUnit)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function AnalysisSummary({ comparison, selectedX, selectedY }) {
  return (
    <aside className="analysis-summary">
      <h2>Resumen de la comparacion</h2>
      <p>
        <strong>Eje X:</strong> {selectedX.name}
      </p>
      <p>
        <strong>Eje Y:</strong> {selectedY.name}
      </p>
      <p>
        <strong>Puntos analizados:</strong> {comparison.points.length}
      </p>
      <p>
        <strong>Correlacion:</strong>{" "}
        {comparison.correlation ?? "No calculable"}
      </p>
      <p>
        <strong>Lectura:</strong> {comparison.correlationLabel}
      </p>
      {comparison.demoData && (
        <p className="analysis-demo-note">
          Los datos mostrados son de demostración y sirven para validar el
          funcionamiento del módulo. La interpretación definitiva dependerá del
          histórico real de KPIs transversales.
        </p>
      )}
      <p>{comparison.interpretation}</p>
    </aside>
  );
}

function formatValue(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return "Sin datos";
  }

  return Number(value).toLocaleString(undefined, {
    maximumFractionDigits: 2
  });
}

function formatWithUnit(value, unit) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return "Sin datos";
  }

  return `${formatValue(value)} ${unit ?? ""}`.trim();
}

function calculateTrendLine(points) {
  if (!points || points.length < 2) {
    return null;
  }

  const meanX =
    points.reduce((total, point) => total + point.x, 0) / points.length;
  const meanY =
    points.reduce((total, point) => total + point.y, 0) / points.length;

  const denominator = points.reduce(
    (total, point) => total + (point.x - meanX) ** 2,
    0
  );

  if (denominator === 0) {
    return null;
  }

  const slope =
    points.reduce(
      (total, point) => total + (point.x - meanX) * (point.y - meanY),
      0
    ) / denominator;

  const intercept = meanY - slope * meanX;

  return {
    x1: 0,
    y1: clamp(intercept),
    x2: 100,
    y2: clamp(intercept + slope * 100)
  };
}

function clamp(value) {
  if (value < 0) {
    return 0;
  }

  if (value > 100) {
    return 100;
  }

  return value;
}

export default AnalysisPage;
