import { cssTone } from "./analysisUtils";

// Muestra la presión media de GLPI por nivel de afección de la plataforma seleccionada.
function GlpiPressureBuckets({ buckets, selectedPlatformLabel }) {
  return (
    <section className="dashboard-section">
      <h2>
        Presion operativa media de GLPI segun nivel de afeccion de{" "}
        {selectedPlatformLabel}
      </h2>
      <div className="analysis-bucket-grid">
        {buckets.map((bucket) => (
          <article className="analysis-bucket-card" key={bucket.level}>
            <span>{bucket.level}</span>
            <strong>
              {bucket.snapshots > 0
                ? `${bucket.averageGlpiPressure}%`
                : "Sin datos"}
            </strong>
            {bucket.snapshots > 0 ? (
              <div className="analysis-bar-track">
                <div
                  className={`analysis-bar-fill ${cssTone(bucket.averageGlpiPressureStatus)}`}
                  style={{ width: `${bucket.averageGlpiPressure}%` }}
                />
              </div>
            ) : (
              <p className="analysis-no-data">
                No hay capturas suficientes para calcular la presion media de
                GLPI.
              </p>
            )}
            <p>{bucket.snapshots} snapshots</p>
            {bucket.snapshots > 0 && (
              <p className="analysis-highlight">
                GLPI alto en {bucket.highGlpiPercentage}%
              </p>
            )}
            <small>
              Afeccion tecnica {bucket.min}-{bucket.max}%
            </small>
          </article>
        ))}
      </div>
    </section>
  );
}

export default GlpiPressureBuckets;
