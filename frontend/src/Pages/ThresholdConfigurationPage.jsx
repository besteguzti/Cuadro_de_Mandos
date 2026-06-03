import { useEffect, useMemo, useState } from "react";

import "../App.css";

import { API_BASE_URL } from "../config/api";

function ThresholdConfigurationPage() {
  const [thresholds, setThresholds] = useState(null);
  const [weights, setWeights] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const weightTotal = useMemo(() => {
    if (!weights) {
      return 0;
    }

    return ["aruba", "citrix", "microsoft365", "glpi"]
      .map((key) => Number(weights[key] || 0))
      .reduce((total, value) => total + value, 0);
  }, [weights]);

  const loadConfiguration = async () => {
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const data = await fetchConfiguration();
      setThresholds(data.thresholds);
      setWeights(data.weights);
    } catch {
      setError("No se pudo cargar la configuración de umbrales.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let cancelled = false;

    fetchConfiguration()
      .then((data) => {
        if (!cancelled) {
          setThresholds(data.thresholds);
          setWeights(data.weights);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setError("No se pudo cargar la configuración de umbrales.");
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const updateThresholdValue = (sectionKey, valueKey, nextValue) => {
    setThresholds((current) => ({
      ...current,
      sections: current.sections.map((section) => {
        if (section.key !== sectionKey) {
          return section;
        }

        return {
          ...section,
          values: section.values.map((value) => {
            if (value.key !== valueKey) {
              return value;
            }

            return {
              ...value,
              value: normalizeNumberInput(nextValue)
            };
          })
        };
      })
    }));
  };

  const updateWeight = (key, nextValue) => {
    setWeights((current) => ({
      ...current,
      [key]: normalizeNumberInput(nextValue)
    }));
  };

  const saveThresholds = async () => {
    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await fetch(`${API_BASE_URL}/api/config/thresholds`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(prepareThresholdPayload(thresholds))
      });

      if (!response.ok) {
        throw new Error(await readError(response));
      }

      setThresholds(await response.json());
      setSuccess("Umbrales guardados correctamente.");
    } catch (err) {
      setError(err.message || "No se pudieron guardar los umbrales.");
    } finally {
      setSaving(false);
    }
  };

  const saveWeights = async () => {
    if (weightTotal !== 100) {
      setError("Los pesos globales deben sumar 100.");
      setSuccess(null);
      return;
    }

    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await fetch(`${API_BASE_URL}/api/config/platform-weights`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(prepareWeightsPayload(weights))
      });

      if (!response.ok) {
        throw new Error(await readError(response));
      }

      setWeights(await response.json());
      setSuccess("Pesos globales guardados correctamente.");
    } catch (err) {
      setError(err.message || "No se pudieron guardar los pesos globales.");
    } finally {
      setSaving(false);
    }
  };

  const resetConfiguration = async () => {
    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await fetch(`${API_BASE_URL}/api/config/thresholds/reset`, {
        method: "POST"
      });

      if (!response.ok) {
        throw new Error(await readError(response));
      }

      await loadConfiguration();
      setSuccess("Configuración restaurada a valores por defecto.");
    } catch (err) {
      setError(err.message || "No se pudo restaurar la configuración.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <main className="dashboard">
        <p className="loading">Cargando configuración...</p>
      </main>
    );
  }

  return (
    <main className="dashboard">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Modelo de KPIs</p>
          <h1>Configuración de umbrales</h1>
        </div>
        <button
          className="secondary-action"
          type="button"
          onClick={resetConfiguration}
          disabled={saving}
        >
          Restaurar valores por defecto
        </button>
      </header>

      <section className="alert config-note">
        Esta pantalla modifica solo la configuración del modelo de scoring. No cambia datos reales,
        datos simulados ni snapshots historicos.
      </section>

      {error && <section className="alert">{error}</section>}
      {success && <section className="success-message">{success}</section>}

      {weights && (
        <section className="dashboard-section config-section">
          <div className="config-section-header">
            <div>
              <h2>Pesos globales</h2>
              <p>
                Pesos usados para calcular el KPI Estado global. La suma debe ser 100.
              </p>
            </div>
            <button type="button" onClick={saveWeights} disabled={saving || weightTotal !== 100}>
              Guardar pesos
            </button>
          </div>

          <div className="config-grid">
            <NumericField label="Aruba" unit="%" value={weights.aruba} onChange={(value) => updateWeight("aruba", value)} />
            <NumericField label="Citrix" unit="%" value={weights.citrix} onChange={(value) => updateWeight("citrix", value)} />
            <NumericField label="Microsoft 365" unit="%" value={weights.microsoft365} onChange={(value) => updateWeight("microsoft365", value)} />
            <NumericField label="GLPI" unit="%" value={weights.glpi} onChange={(value) => updateWeight("glpi", value)} />
          </div>

          <p className={weightTotal === 100 ? "config-total ok" : "config-total warning"}>
            Total pesos: {weightTotal} %
          </p>
        </section>
      )}

      {thresholds?.sections?.map((section) => (
        <section className="dashboard-section config-section" key={section.key}>
          <div className="config-section-header">
            <div>
              <h2>{section.title}</h2>
              <p>{section.description}</p>
            </div>
            <button type="button" onClick={saveThresholds} disabled={saving}>
              Guardar umbrales
            </button>
          </div>

          <div className="config-grid">
            {section.values.map((value) => (
              <NumericField
                key={value.key}
                valueKey={value.key}
                label={value.label}
                unit={value.unit}
                value={value.value}
                description={value.description}
                defaultValue={value.defaultValue}
                onChange={(nextValue) => updateThresholdValue(section.key, value.key, nextValue)}
              />
            ))}
          </div>
        </section>
      ))}
    </main>
  );
}

function NumericField({ valueKey, label, value, unit, description, defaultValue, onChange }) {
  const limits = numericFieldLimits(valueKey);

  return (
    <label className="config-field">
      <span>{label}</span>
      <div className="config-input-row">
        <input
          type="number"
          min={limits.min}
          max={limits.max}
          step="1"
          value={value ?? ""}
          onChange={(event) => onChange(event.target.value)}
        />
        {unit && <strong>{unit}</strong>}
      </div>
      {description && <small>{description}</small>}
      {defaultValue !== undefined && defaultValue !== null && (
        <small>Por defecto: {defaultValue}{unit ? ` ${unit}` : ""}</small>
      )}
    </label>
  );
}

function numericFieldLimits(valueKey) {
  if (valueKey === "aruba.inactiveApDaysThreshold") {
    return { min: 1, max: 365 };
  }

  return { min: 0, max: undefined };
}

function normalizeNumberInput(value) {
  if (value === "") {
    return "";
  }

  const parsed = Number(value);
  return Number.isNaN(parsed) ? "" : parsed;
}

function prepareThresholdPayload(thresholds) {
  return {
    sections: thresholds.sections.map((section) => ({
      ...section,
      values: section.values.map((value) => ({
        ...value,
        value: value.value === "" ? null : Number(value.value)
      }))
    }))
  };
}

function prepareWeightsPayload(weights) {
  return {
    aruba: Number(weights.aruba),
    citrix: Number(weights.citrix),
    microsoft365: Number(weights.microsoft365),
    glpi: Number(weights.glpi)
  };
}

async function readError(response) {
  try {
    const data = await response.json();
    return data.message || data.error || "La configuración no es valida.";
  } catch {
    return "La configuración no es valida.";
  }
}

async function fetchConfiguration() {
  const [thresholdResponse, weightResponse] = await Promise.all([
    fetch(`${API_BASE_URL}/api/config/thresholds`),
    fetch(`${API_BASE_URL}/api/config/platform-weights`)
  ]);

  if (!thresholdResponse.ok || !weightResponse.ok) {
    throw new Error("No se pudo cargar la configuración.");
  }

  return {
    thresholds: await thresholdResponse.json(),
    weights: await weightResponse.json()
  };
}

export default ThresholdConfigurationPage;
