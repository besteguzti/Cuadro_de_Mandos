import { useEffect, useState } from "react";

import "../App.css";

import AnalysisErrorState from "../components/analysis/AnalysisErrorState";
import AnalysisHeader from "../components/analysis/AnalysisHeader";
import AnalysisLoadingState from "../components/analysis/AnalysisLoadingState";
import PlatformEvolutionTimeline from "../components/analysis/PlatformEvolutionTimeline";
import SpecificKpiRelationsSection from "../components/analysis/SpecificKpiRelationsSection";
import TechnicalRelationTable from "../components/analysis/TechnicalRelationTable";
import { API_BASE_URL } from "../config/api";

const periods = [
  { label: "7 días", value: "7d" },
  { label: "30 días", value: "30d" },
  { label: "90 días", value: "90d" }
];

function AnalysisPage() {
  const [period, setPeriod] = useState("30d");
  const [analysis, setAnalysis] = useState(null);
  const [visibleSeries, setVisibleSeries] = useState({
    aruba: true,
    citrix: true,
    microsoft365: true
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [visibilityWarning, setVisibilityWarning] = useState(null);

  // El panel se alimenta del endpoint agregado del backend: React no genera
  // datos demo ni calcula relaciones, solo renderiza la respuesta persistida.
  useEffect(() => {
    fetch(
      `${API_BASE_URL}/api/analysis/glpi-platform-relation?period=${period}`
    )
      .then((response) => {
        if (!response.ok) {
          throw new Error("No se pudo cargar el análisis");
        }

        return response.json();
      })
      .then((data) => {
        setAnalysis(data);
        setError(null);
        setLoading(false);
      })
      .catch(() => {
        setError("No se han podido cargar los datos del análisis.");
        setLoading(false);
      });
  }, [period]);

  

  const handlePeriodChange = (event) => {
    setLoading(true);
    setPeriod(event.target.value);
  };

  // La evolución temporal debe conservar al menos una serie visible para no
  // dejar la gráfica vacía.
  const toggleSeries = (serie) => {
    const activeCount = Object.values(visibleSeries).filter(Boolean).length;

    if (visibleSeries[serie] && activeCount === 1) {
      setVisibilityWarning("Debe quedar al menos una plataforma visible.");
      return;
    }

    setVisibilityWarning(null);
    setVisibleSeries((current) => ({
      ...current,
      [serie]: !current[serie]
    }));
  };

  return (
    <main className="dashboard">
      <AnalysisHeader />

      <AnalysisErrorState message={error} />

      {loading && <AnalysisLoadingState />}

      {analysis && !loading && (
        <>
          <TechnicalRelationTable relations={analysis.technicalRelations ?? []} />

          <PlatformEvolutionTimeline
            points={analysis.technicalTimeline ?? []}
            visibleSeries={visibleSeries}
            onToggle={toggleSeries}
            warning={visibilityWarning}
          />

          <SpecificKpiRelationsSection
            relations={analysis.specificKpiRelations ?? []}
            periods={periods}
            selectedPeriod={period}
            onPeriodChange={handlePeriodChange}
          />
        </>
      )}
    </main>
  );
}

export default AnalysisPage;
