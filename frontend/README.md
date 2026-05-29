# Frontend

Interfaz React del dashboard multiproveedor del TFG. El frontend muestra el
dashboard principal, el diagnostico operativo, las vistas de Aruba, Citrix,
Microsoft 365 y GLPI, y el panel de analisis exploratorio basado en snapshots
persistidos.

## Tecnologias

- React.
- Vite.
- CSS modularizado en hojas del proyecto.

## Arranque

Desde la carpeta `frontend`:

```powershell
npm install
npm run dev
```

Por defecto Vite sirve la aplicacion en:

```text
http://localhost:5173
```

Para generar una build de produccion:

```powershell
npm run build
```

## Configuracion

El frontend centraliza la URL del backend en `src/config/api.js`.

Variable opcional:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080"
```

Si no se define `VITE_API_BASE_URL`, se usa `http://localhost:8080`.

## Paginas principales

- Dashboard principal: KPIs transversales normalizados.
- Resumen operativo: diagnostico ejecutivo para responsable IT.
- Aruba: estado de red, APs, clientes WiFi, switches y switches infrautilizados.
- Citrix: indicadores simulados persistidos en MySQL.
- Microsoft 365: indicadores simulados persistidos en MySQL.
- GLPI: indicadores simulados persistidos en MySQL.
- Panel de analisis: relacion operativa aparente entre GLPI y plataformas tecnicas.

## Endpoints consumidos directamente por React

```http
GET /dashboard/summary
GET /api/dashboard/executive-summary
GET /aruba/summary
GET /citrix/summary
GET /microsoft365/summary
GET /glpi/summary
GET /api/analysis/glpi-platform-relation
```

El endpoint `/api/analysis/glpi-platform-relation` alimenta el panel de
analisis actual. La respuesta agrupa la relacion GLPI-plataforma, la lectura
tecnica-operativa y la evolucion temporal que necesita la pantalla.

## Endpoints disponibles en backend para consulta y validacion

Estos endpoints pueden usarse para pruebas tecnicas, documentacion o validacion
del backend, pero no se consumen directamente desde React en el flujo actual.

```http
GET /api/analysis/technical-degradation-impact
GET /api/analysis/platform-evolution
GET /api/analysis/snapshots
GET /api/kpis/definitions
```

## Analisis exploratorio

El panel de analisis no intenta demostrar causa raiz automatica. Compara
snapshots historicos para observar relaciones aparentes, co-ocurrencias y
patrones operativos entre la presion de GLPI y la afeccion tecnica de Aruba,
Citrix y Microsoft 365.

El panel actual usa `/api/analysis` y la lectura principal se basa en presion
media, co-ocurrencia y snapshots persistidos.
