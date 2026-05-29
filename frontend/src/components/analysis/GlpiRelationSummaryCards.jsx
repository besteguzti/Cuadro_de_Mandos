import KpiCard from "../KpiCard";

function GlpiRelationSummaryCards({ analysis }) {
  if (!analysis) {
    return null;
  }

  const cards = [
    {
      title: "Presion operativa GLPI",
      value: `${analysis.glpiOperationalPressure}%`,
      status: analysis.glpiOperationalPressureColor,
      info: {
        description:
          "Representa la consecuencia operativa observada en GLPI.",
        algorithm:
          "Tickets abiertos 40%, cierre diario 30%, tickets criticos 20% y cierre semanal 10%.",
        interpretation:
          "Cuanto mas alto, mayor presion operativa sobre el area tecnica."
      }
    },
    {
      title: "Incremento medio GLPI",
      value: `${analysis.averageGlpiIncreaseWhenAffected}%`,
      status: analysis.averageGlpiIncreaseStatus,
      info: {
        description:
          "Compara la presion media de GLPI cuando la plataforma esta normal frente a cuando esta afectada.",
        algorithm:
          "Media GLPI con plataforma afectada menos media GLPI con plataforma normal.",
        interpretation:
          "Un valor positivo indica que GLPI tiende a estar mas presionado cuando la plataforma seleccionada presenta afeccion."
      }
    },
    {
      title: "Co-ocurrencia alta-alta",
      value: `${analysis.highHighCooccurrencePercentage}%`,
      status: analysis.highHighCooccurrenceStatus,
      info: {
        description:
          "Porcentaje de snapshots donde coinciden presion GLPI y afeccion tecnica.",
        algorithm:
          "Cuenta snapshots con plataforma >= 34% y GLPI >= 34%, dividido entre el total.",
        interpretation:
          "Un valor alto indica que ambas senales aparecen juntas con frecuencia."
      }
    },
    {
      title: "Relacion operativa aparente",
      value: `${analysis.apparentOperationalRelation}%`,
      status: analysis.apparentOperationalRelationStatus,
      info: {
        description:
          "Resume si GLPI tiende a aumentar cuando la plataforma tecnica esta afectada.",
        algorithm:
          "Combina el incremento medio de GLPI y la co-ocurrencia alta-alta.",
        interpretation:
          "No demuestra causalidad, pero ayuda a priorizar investigaciones operativas."
      }
    },
    {
      title: "Mayor relacion aparente",
      value: analysis.highestRelatedPlatform,
      status: analysis.highestRelationStatus,
      info: {
        description:
          "Selecciona la plataforma tecnica con mayor relacion aparente frente a GLPI.",
        algorithm:
          "Compara Relacion Aruba-GLPI, Relacion Citrix-GLPI y Relacion Microsoft365-GLPI.",
        interpretation:
          "No implica causalidad directa. Senala donde conviene revisar primero."
      }
    },
    {
      title: "Impacto operativo estimado",
      value: `${analysis.estimatedOperationalImpact}%`,
      status: analysis.estimatedOperationalImpactStatus,
      info: {
        description:
          "Combina presion GLPI y la mayor afeccion tecnica detectada.",
        algorithm:
          "Presion operativa GLPI 50% y mayor afeccion tecnica 50%.",
        interpretation:
          "Cuanto mas alto, mayor indicio de impacto operativo transversal."
      }
    }
  ];

  return (
    <section className="dashboard-section">
      <div className="kpi-grid">
        {cards.map((card) => (
          <KpiCard
            key={card.title}
            title={card.title}
            value={card.value}
            status={card.status}
            info={card.info}
          />
        ))}
      </div>
    </section>
  );
}

export default GlpiRelationSummaryCards;
