import AnalysisEmptyState from "./AnalysisEmptyState";
import { formatValue } from "./analysisUtils";

// Nube de puntos de co-ocurrencia: GLPI en X y plataforma técnica seleccionada en Y.
function GlpiPlatformScatterChart({ platform, points }) {
  const width = 720;
  const height = 420;
  const padding = 56;
  const ticks = [0, 25, 50, 75, 100];
  const threshold = 34;

  const scaleX = (value) =>
    padding + (value / 100) * (width - padding * 2);

  const scaleY = (value) =>
    height - padding - (value / 100) * (height - padding * 2);

  return (
    <div className="analysis-chart-card">
      <p className="analysis-chart-explanation">
        Cada punto representa una captura historica. El eje X muestra la
        presion operativa de GLPI y el eje Y muestra la afeccion tecnica de la
        plataforma seleccionada. La nube sirve para ver co-ocurrencia,
        especialmente el cuadrante superior derecho, no una correlacion lineal.
      </p>

      {points.length < 2 ? (
        <AnalysisEmptyState />
      ) : (
        <svg
          className="analysis-chart"
          viewBox={`0 0 ${width} ${height}`}
          role="img"
          aria-label={`Grafica de dispersion GLPI frente a ${platform}`}
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

          <line
            className="analysis-threshold-line"
            x1={scaleX(threshold)}
            y1={padding}
            x2={scaleX(threshold)}
            y2={height - padding}
          />
          <line
            className="analysis-threshold-line"
            x1={padding}
            y1={scaleY(threshold)}
            x2={width - padding}
            y2={scaleY(threshold)}
          />

          <text x={width / 2} y={height - 14} textAnchor="middle">
            Presion operativa GLPI (%)
          </text>
          <text
            x={18}
            y={height / 2}
            textAnchor="middle"
            transform={`rotate(-90 18 ${height / 2})`}
          >
            Afeccion {platform} (%)
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

          {points.map((point, index) => (
            <circle
              key={`${point.timestamp}-${index}`}
              className={point.x >= threshold && point.y >= threshold ? "high-high" : ""}
              cx={scaleX(point.x)}
              cy={scaleY(point.y)}
              r="6"
            >
              <title>
                {`${new Date(point.timestamp).toLocaleString()} | GLPI: ${formatValue(point.x)}% | ${platform}: ${formatValue(point.y)}%`}
              </title>
            </circle>
          ))}
        </svg>
      )}
    </div>
  );
}

export default GlpiPlatformScatterChart;
