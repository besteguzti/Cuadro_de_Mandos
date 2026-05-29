import { useEffect, useState } from "react";

import "../App.css";

import AnalysisErrorState from "../components/analysis/AnalysisErrorState";
import AnalysisHeader from "../components/analysis/AnalysisHeader";
import AnalysisLoadingState from "../components/analysis/AnalysisLoadingState";
import AnalysisSelectionPanel from "../components/analysis/AnalysisSelectionPanel";
import GlpiPlatformRelationSection from "../components/analysis/GlpiPlatformRelationSection";
import GlpiPressureBuckets from "../components/analysis/GlpiPressureBuckets";
import GlpiRelationSummaryCards from "../components/analysis/GlpiRelationSummaryCards";
import PeriodSelector from "../components/analysis/PeriodSelector";
import PlatformEvolutionTimeline from "../components/analysis/PlatformEvolutionTimeline";
import PlatformSelector from "../components/analysis/PlatformSelector";
import RelationshipBarChart from "../components/analysis/RelationshipBarChart";
import TechnicalImpactSection from "../components/analysis/TechnicalImpactSection";
import TechnicalRelationTable from "../components/analysis/TechnicalRelationTable";
import { API_BASE_URL } from "../config/api";

const periods = [
  { label: "7 dias", value: "7d" },
  { label: "30 dias", value: "30d" },
  { label: "90 dias", value: "90d" }
];

const platforms = [
  { label: "Aruba", value: "aruba" },
  { label: "Citrix", value: "citrix" },
  { label: "Microsoft 365", value: "microsoft365" }
];

function AnalysisPage() {
  const [platform, setPlatform] = useState("aruba");
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
      `${API_BASE_URL}/api/analysis/glpi-platform-relation?platform=${platform}&period=${period}`
    )
      .then((response) => {
        if (!response.ok) {
          throw new Error("No se pudo cargar el analisis");
        }

        return response.json();
      })
      .then((data) => {
        setAnalysis(data);
        setError(null);
        setLoading(false);
      })
      .catch(() => {
        setError("No se han podido cargar los datos del analisis.");
        setLoading(false);
      });
  }, [platform, period]);

  const selectedPlatformLabel =
    platforms.find((item) => item.value === platform)?.label ?? "Aruba";

  const handlePlatformChange = (event) => {
    setLoading(true);
    setPlatform(event.target.value);
  };

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
      <AnalysisHeader>
        <div className="analysis-actions">
          <PlatformSelector
            platforms={platforms}
            selectedPlatform={platform}
            onChange={handlePlatformChange}
          />

          <PeriodSelector
            periods={periods}
            selectedPeriod={period}
            onChange={handlePeriodChange}
          />
        </div>
      </AnalysisHeader>

      <AnalysisErrorState message={error} />

      {loading && <AnalysisLoadingState />}

      {analysis && !loading && (
        <>
          <GlpiRelationSummaryCards analysis={analysis} />

          <AnalysisSelectionPanel
            selectedPlatformLabel={selectedPlatformLabel}
            selectedPlatformAffection={analysis.selectedPlatformAffection}
          />

          <GlpiPressureBuckets
            buckets={analysis.buckets ?? []}
            selectedPlatformLabel={selectedPlatformLabel}
          />

          <GlpiPlatformRelationSection analysis={analysis} />

          <RelationshipBarChart analysis={analysis} />

          <TechnicalRelationTable relations={analysis.technicalRelations ?? []} />

          <TechnicalImpactSection analysis={analysis} />

          <PlatformEvolutionTimeline
            points={analysis.technicalTimeline ?? []}
            visibleSeries={visibleSeries}
            onToggle={toggleSeries}
            warning={visibilityWarning}
          />
        </>
      )}
    </main>
  );
}

export default AnalysisPage;
