import {
    formatImpactLevel,
    formatPriority,
    formatTrend
} from "../utils/statusFormatters";

function OperationalSummaryPanel({ summary, contextualNotice }) {
    if (!summary) {
        return null;
    }

    return (
        <section className={`executive-summary executive-summary-${(summary.priority ?? "LOW").toLowerCase()}`}>
            <div className="executive-summary-main">
                <p className="eyebrow">Diagnóstico operativo</p>
                <h2>Resumen operativo</h2>
                {contextualNotice && (
                    <p>{contextualNotice}</p>
                )}
                <p>{summary.summaryText}</p>
            </div>

            <div className="executive-summary-grid">
                <ExecutiveField
                    label="Servicios afectados"
                    value={
                        summary.affectedServices?.length > 0
                            ? summary.affectedServices.join(", ")
                            : "Sin servicios afectados"
                    }
                />
                <ExecutiveField
                    label="Plataforma principal"
                    value={summary.mainAffectedPlatform}
                />
                <ExecutiveField
                    label="Origen probable"
                    value={summary.probableOrigin}
                />
                <ExecutiveField
                    label="Impacto"
                    value={formatImpactLevel(summary.impactLevel)}
                />
                <ExecutiveField
                    label="Usuarios potencialmente afectados"
                    value={summary.estimatedAffectedUsers}
                />
                <ExecutiveField
                    label="Prioridad"
                    value={formatPriority(summary.priority)}
                />
                <ExecutiveField
                    label="Tendencia"
                    value={formatTrend(summary.trend)}
                />
                <ExecutiveField
                    label="Primera acción"
                    value={summary.firstAction}
                />
            </div>
        </section>
    );
}

function ExecutiveField({ label, value }) {
    return (
        <div className="executive-summary-field">
            <span>{label}</span>
            <strong>{value ?? "Sin datos"}</strong>
        </div>
    );
}

export default OperationalSummaryPanel;
