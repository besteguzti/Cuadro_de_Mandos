import AnalysisSummary from "./AnalysisSummary";
import GlpiPlatformScatterChart from "./GlpiPlatformScatterChart";

// Mantiene juntas la nube de puntos y la lectura operativa: la gráfica apoya la
// co-ocurrencia, pero la interpretación principal viene del backend.
function GlpiPlatformRelationSection({ analysis }) {
  return (
    <section className="dashboard-section">
      <div className="analysis-layout">
        <GlpiPlatformScatterChart
          platform={analysis.selectedPlatform}
          points={analysis.points ?? []}
        />

        <AnalysisSummary analysis={analysis} />
      </div>
    </section>
  );
}

export default GlpiPlatformRelationSection;
