import { cssTone } from "./analysisUtils";

function RelationshipBarChart({ analysis }) {
  const bars = [
    {
      label: "Aruba-GLPI",
      value: analysis.arubaGlpiRelation,
      status: analysis.arubaGlpiRelationStatus
    },
    {
      label: "Citrix-GLPI",
      value: analysis.citrixGlpiRelation,
      status: analysis.citrixGlpiRelationStatus
    },
    {
      label: "Microsoft365-GLPI",
      value: analysis.microsoft365GlpiRelation,
      status: analysis.microsoft365GlpiRelationStatus
    }
  ];

  return (
    <section className="dashboard-section">
      <h2>Comparativa de relaciones aparentes</h2>
      <div className="analysis-bar-card">
        {bars.map((bar) => (
          <div className="analysis-bar-row" key={bar.label}>
            <span>{bar.label}</span>
            <div className="analysis-bar-track">
              <div
                className={`analysis-bar-fill ${cssTone(bar.status)}`}
                style={{ width: `${bar.value}%` }}
              />
            </div>
            <strong>{bar.value}%</strong>
          </div>
        ))}
      </div>
    </section>
  );
}

export default RelationshipBarChart;
