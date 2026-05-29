function AnalysisSummary({ analysis }) {
  return (
    <aside className="analysis-summary">
      <h2>Lectura operativa</h2>
      <p>
        GLPI se interpreta como consecuencia operativa: tickets abiertos,
        tickets criticos y capacidad de cierre.
      </p>
      <p>
        Aruba, Citrix y Microsoft 365 se interpretan como posibles origenes
        tecnicos. El panel muestra relacion aparente, no causalidad directa.
      </p>
      <p className="analysis-key-reading">
        El analisis no busca demostrar causalidad directa ni una correlacion
        lineal perfecta. Su objetivo es comprobar si la presion operativa de
        GLPI tiende a ser mayor cuando la plataforma tecnica seleccionada
        presenta afeccion. La metrica mas importante es la co-ocurrencia entre
        plataforma afectada y GLPI alto.
      </p>
      <p>
        <strong>Mayor relacion aparente:</strong>{" "}
        {analysis.highestRelatedPlatform} ({analysis.highestRelationValue}%)
      </p>
      <p>
        <strong>Impacto operativo estimado:</strong>{" "}
        {analysis.estimatedOperationalImpact}%
      </p>
      <div className="analysis-summary-table">
        <span>GLPI medio plataforma normal</span>
        <strong>
          {analysis.normalSnapshots > 0
            ? `${analysis.averageGlpiWhenPlatformNormal}%`
            : "sin datos suficientes"}
        </strong>
        <span>GLPI medio plataforma afectada</span>
        <strong>
          {analysis.affectedSnapshots > 0
            ? `${analysis.averageGlpiWhenPlatformAffected}%`
            : "sin datos suficientes"}
        </strong>
        <span>Diferencia</span>
        <strong>
          {analysis.normalSnapshots > 0 && analysis.affectedSnapshots > 0
            ? `${analysis.averageGlpiIncreaseWhenAffected}%`
            : "sin datos suficientes"}
        </strong>
        <span>Co-ocurrencia alta-alta</span>
        <strong className="analysis-cooccurrence">
          {analysis.highHighCooccurrencePercentage}%
        </strong>
      </div>
      {analysis.demoData && (
        <p className="analysis-demo-note">
          Los datos mostrados son de demostracion cuando no existe historico
          suficiente para esa relacion. La interpretacion definitiva dependera
          del historico real.
        </p>
      )}
      <p>{analysis.interpretation}</p>
    </aside>
  );
}

export default AnalysisSummary;
