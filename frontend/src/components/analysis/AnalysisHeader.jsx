function AnalysisHeader({ children }) {
  return (
    <header className="dashboard-header">
      <div>
        <p className="eyebrow">Modulo de analisis</p>
        <h1>Analisis exploratorio de KPIs transversales</h1>
      </div>

      {children}
    </header>
  );
}

export default AnalysisHeader;
