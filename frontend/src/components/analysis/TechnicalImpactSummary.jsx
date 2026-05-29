function TechnicalImpactSummary({ analysis }) {
  return (
    <aside className="analysis-summary">
      <h2>Conversion tecnica-operativa</h2>
      <p>
        Este bloque responde si la degradacion tecnica se esta traduciendo en
        impacto real sobre usuarios.
      </p>
      <div className="analysis-summary-table">
        <span>Degradacion tecnica</span>
        <strong>{analysis.technicalDegradation}%</strong>
        <span>Impacto en usuarios</span>
        <strong>{analysis.userImpact}%</strong>
        <span>Conversion tecnica-operativa</span>
        <strong className="analysis-cooccurrence">
          {analysis.technicalOperationalConversion}%
        </strong>
      </div>
      <p>{analysis.technicalImpactInterpretation}</p>
    </aside>
  );
}

export default TechnicalImpactSummary;
