# Frontend

Interfaz React del dashboard multiproveedor del TFG. El frontend muestra el
dashboard principal, el diagnóstico operativo, las vistas de Aruba, Citrix,
Microsoft 365 y GLPI, y el panel de análisis exploratorio basado en snapshots
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

Para generar una build de producción:

```powershell
npm run build
```

## Configuración

El frontend centraliza la URL del backend en `src/config/api.js`.

Variable opcional:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080"
```

Si no se define `VITE_API_BASE_URL`, se usa `http://localhost:8080`.

## Paginas principales

- Dashboard principal: KPIs transversales normalizados.
- Resumen operativo: diagnóstico ejecutivo para responsable IT.
- Aruba: estado de red, APs, clientes WiFi, switches y switches infrautilizados.
- Citrix: indicadores simulados persistidos en MySQL.
- Microsoft 365: indicadores simulados persistidos en MySQL.
- GLPI: indicadores simulados persistidos en MySQL.
- Panel de análisis: relación operativa aparente entre GLPI y plataformas técnicas.
- Banco de pruebas: evaluacion manual de escenarios sin persistir datos reales.

## Endpoints consumidos directamente por React

React usa estos endpoints en el flujo actual del frontend:

```http
GET /dashboard/summary
GET /api/dashboard/executive-summary
GET /aruba/summary
GET /citrix/summary
GET /microsoft365/summary
GET /glpi/summary
GET /api/analysis/glpi-platform-relation
POST /api/test-scenarios/evaluate
```

El endpoint `/api/analysis/glpi-platform-relation` alimenta el panel de
análisis actual y devuelve los bloques `technicalRelations`, `technicalTimeline`
y `specificKpiRelations`.

## Endpoints auxiliares disponibles en backend

Estos endpoints están disponibles en el backend para consulta, validación técnica
y pruebas, pero no son consumidos directamente por React en el flujo actual.

```http
GET /api/analysis/technical-degradation-impact
GET /api/analysis/platform-evolution
GET /api/analysis/snapshots
GET /api/kpis/definitions
```

## Endpoints administrativos o de sincronización Aruba

Estos endpoints se usan para sincronización manual, diagnóstico o pruebas locales
relaciónadas con Aruba Central:

```http
POST /aruba/sync-all
POST /aruba/sync-aps
POST /aruba/sync-switches
POST /aruba/sync-switch-client-usage
GET /aruba/wifi-clients/diagnostics
```

## Análisis exploratorio

El panel de análisis no intenta demostrar causa raíz automática. Compara
snapshots históricos para observar relaciónes aparentes, co-ocurrencias y
patrones operativos entre la presión de GLPI y la afección técnica de Aruba,
Citrix y Microsoft 365.

El panel actual consume principalmente `/api/analysis/glpi-platform-relation`.
La respuesta agrupa co-ocurrencias, tabla técnica, evolución temporal y
relaciónes específicas entre indicadores.

## Banco de pruebas

El Banco de pruebas envía escenarios manuales al backend mediante
`POST /api/test-scenarios/evaluate`. React no calcula KPIs ni resumen operativo:
solo muestra la respuesta del backend.

Los tickets abiertos totales se calculan como suma de tickets por plataforma:
Aruba, Citrix y Microsoft 365. Ese total puede mostrarse en pantalla como dato
calculado, pero no forma parte del payload enviado al backend.

El resumen operativo del Banco de pruebas reutiliza el mismo componente visual
que el dashboard principal. La tendencia se muestra como no disponible porque
el escenario manual no tiene histórico real.
