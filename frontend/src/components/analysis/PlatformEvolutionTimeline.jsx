import AnalysisErrorState from "./AnalysisErrorState";

// Línea temporal comparativa de afección técnica. El padre impide ocultar todas las series.
function PlatformEvolutionTimeline({ points, visibleSeries, onToggle, warning }) {
  const width = 920;
  const height = 360;
  const padding = 52;
  const ticks = [0, 25, 50, 75, 100];
  const series = [
    { key: "aruba", label: "Aruba", color: "#315c9c" },
    { key: "citrix", label: "Citrix", color: "#228b5a" },
    { key: "microsoft365", label: "Microsoft 365", color: "#b42318" }
  ];

  const scaleX = (index) => {
    if (points.length <= 1) {
      return padding;
    }

    return padding + (index / (points.length - 1)) * (width - padding * 2);
  };

  const scaleY = (value) =>
    height - padding - (value / 100) * (height - padding * 2);

  const polylinePoints = (key) =>
    points
      .map((point, index) => `${scaleX(index)},${scaleY(point[key])}`)
      .join(" ");

  return (
    <section className="dashboard-section">
      <h2>Evolucion temporal conjunta de afección técnica</h2>
      <AnalysisErrorState message={warning} />
      <div className="analysis-chart-card analysis-timeline-card">
        <div className="analysis-legend">
          {series.map((item) => (
            <button
              type="button"
              key={item.key}
              className={visibleSeries[item.key] ? "active" : ""}
              onClick={() => onToggle(item.key)}
            >
              <span style={{ backgroundColor: item.color }} />
              {item.label}
            </button>
          ))}
        </div>

        <svg
          className="analysis-chart"
          viewBox={`0 0 ${width} ${height}`}
          role="img"
          aria-label="Evolucion temporal conjunta de afección técnica"
        >
          <line x1={padding} y1={height - padding} x2={width - padding} y2={height - padding} />
          <line x1={padding} y1={padding} x2={padding} y2={height - padding} />

          {ticks.map((tick) => (
            <g key={`timeline-${tick}`}>
              <line
                className="analysis-grid-line"
                x1={padding}
                y1={scaleY(tick)}
                x2={width - padding}
                y2={scaleY(tick)}
              />
              <text x={padding - 12} y={scaleY(tick) + 4} textAnchor="end">
                {tick}
              </text>
            </g>
          ))}

          {series.map((item) =>
            visibleSeries[item.key] ? (
              <polyline
                key={item.key}
                className="analysis-timeline-line"
                points={polylinePoints(item.key)}
                style={{ stroke: item.color }}
              />
            ) : null
          )}

          {points.map((point, index) => (
            <text
              key={`timeline-date-${point.timestamp}-${index}`}
              x={scaleX(index)}
              y={height - padding + 24}
              textAnchor="middle"
            >
              {new Date(point.timestamp).toLocaleDateString()}
            </text>
          ))}
        </svg>
      </div>
    </section>
  );
}

export default PlatformEvolutionTimeline;
