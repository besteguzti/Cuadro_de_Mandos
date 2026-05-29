import GenericScatterChart from "./GenericScatterChart";

// Especialización de la gráfica de dispersión para degradación técnica vs impacto.
function TechnicalImpactChart({ points }) {
  return (
    <GenericScatterChart
      points={points}
      xLabel="Degradacion tecnica (%)"
      yLabel="Impacto en usuarios (%)"
      description="Esta grafica muestra si la degradacion tecnica se traduce en impacto observado sobre usuarios. Los cuadrantes ayudan a interpretar escenarios: normal, problema tecnico sin impacto visible, impacto por causa no detectada o impacto real."
      tooltipX="Degradacion tecnica"
      tooltipY="Impacto usuarios"
    />
  );
}

export default TechnicalImpactChart;
