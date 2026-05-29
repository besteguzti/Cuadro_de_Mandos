import TechnicalImpactChart from "./TechnicalImpactChart";
import TechnicalImpactSummary from "./TechnicalImpactSummary";

// Representa si la degradación técnica se traduce en impacto observable para usuarios.
function TechnicalImpactSection({ analysis }) {
  return (
    <section className="dashboard-section">
      <h2>Degradacion tecnica frente a impacto en usuarios</h2>
      <div className="analysis-layout">
        <TechnicalImpactChart points={analysis.technicalImpactPoints ?? []} />

        <TechnicalImpactSummary analysis={analysis} />
      </div>
    </section>
  );
}

export default TechnicalImpactSection;
