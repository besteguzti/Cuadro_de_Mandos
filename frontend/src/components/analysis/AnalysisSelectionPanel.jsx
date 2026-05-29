function AnalysisSelectionPanel({ selectedPlatformLabel, selectedPlatformAffection }) {
  return (
    <section className="analysis-selection-panel">
      <span>Consecuencia operativa: GLPI</span>
      <span>Origen tecnico seleccionado: {selectedPlatformLabel}</span>
      <span>
        Afeccion actual {selectedPlatformLabel}: {selectedPlatformAffection}%
      </span>
    </section>
  );
}

export default AnalysisSelectionPanel;
