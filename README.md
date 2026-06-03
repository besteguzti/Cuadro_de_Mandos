# Cuadro de Mandos TFG

Dashboard multiproveedor para monitorizar Aruba, Citrix, Microsoft 365 y GLPI.

El proyecto combina un backend Spring Boot, una base de datos MySQL y un frontend React/Vite. Aruba se integra con datos reales de Aruba Central. Citrix, Microsoft 365 y GLPI usan datos simulados dinamicos que se guardan como snapshots en MySQL para alimentar históricos y KPIs transversales.

## Arquitectura

- `dashboard`: backend Spring Boot.
- `frontend`: interfaz React + Vite.
- MySQL: persistencia de inventario, snapshots e históricos.
- Aruba Central: fuente real para APs, switches, firmware, clientes WiFi y puertos.
- Citrix, Microsoft 365 y GLPI: datos simulados dinamicos persistidos cada minuto.

## Fuentes reales y simuladas

- Aruba Central es la unica integracion real del proyecto. Se consulta mediante API y se sincronizan APs, switches, firmware, clientes WiFi y puertos.
- Citrix, Microsoft 365 y GLPI son fuentes simuladas dinamicas. No representan integraciones reales de producción con esas plataformas.
- Las fuentes simuladas no son arrays fijos del frontend: se generan en el backend, se persisten como snapshots en MySQL y alimentan las vistas y KPIs.
- El dashboard de análisis trabaja con snapshots persistidos. Si no hay histórico suficiente, el sistema puede generar escenarios de demostracion persistidos e identificables mediante `generatedScenario`.
- El análisis exploratorio no demuestra causalidad directa. Muestra relaciónes aparentes, co-ocurrencias y patrones operativos que deben interpretarse como ayuda a la investigacion.

## Tablas principales

- `access_points`: APs Aruba, una fila por numero de serie.
- `aruba_switches`: switches Aruba, una fila por numero de serie.
- `aruba_dashboard_metrics`: KPIs agregados Aruba, como firmware y clientes WiFi.
- `aruba_switch_client_usage`: ultimo recuento de interfaces down por switch.
- `aruba_switch_interface_usage_history`: histórico de interfaces down por switch.
- `aruba_network_status_history`: histórico del estado de red Aruba calculado.
- `citrix_metrics_history`: snapshots históricos Citrix.
- `microsoft365_metrics_history`: snapshots históricos Microsoft 365.
- `glpi_metrics_history`: snapshots históricos GLPI.
- `analysis_snapshots`: snapshots históricos usados por el panel de análisis.
- `oauth_tokens`: token OAuth usado para Aruba Central.

## Requisitos

- Java 17.
- Maven.
- Node.js y npm.
- MySQL con una base de datos llamada `dashboard`.
- Credenciales Aruba Central.

## Variables de entorno

Backend:

```powershell
$env:DB_USERNAME="usuario_mysql"
$env:DB_PASSWORD="password_mysql"
$env:ARUBA_CLIENT_ID="client_id_aruba"
$env:ARUBA_CLIENT_SECRET="client_secret_aruba"
```

Frontend:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080"
```

Si no se define `VITE_API_BASE_URL`, el frontend usa `http://localhost:8080` por defecto. Hay un ejemplo en `frontend/.env.example`.

## Configuración relevante

`dashboard/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dashboard
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

aruba.base.url=https://apigw-eucentral3.central.arubanetworks.com
aruba.sync.initial-delay-ms=60000
aruba.sync.fixed-rate-ms=3600000
```

CORS se gestiona de forma global con `CorsConfig`.

## Arrancar backend

Desde `dashboard`:

```powershell
mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

## Arrancar frontend

Desde `frontend`:

```powershell
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

## Sincronizacion automatica

El proyecto tiene scheduling habilitado con `@EnableScheduling`.

### Aruba

`ArubaScheduler` ejecuta `arubaService.syncAll()` con estos parametros:

- `aruba.sync.initial-delay-ms`
- `aruba.sync.fixed-rate-ms`

La sincronizacion de Aruba:

- Guarda APs por numero de serie.
- Guarda switches por numero de serie.
- Actualiza firmware pendiente.
- Agrega clientes WiFi por grupo y red.
- Consulta puertos por switch y guarda interfaces en down.
- Inserta histórico de interfaces down.

`GET /aruba/summary` no consulta Aruba directamente; lee datos ya sincronizados desde MySQL.

### Citrix, Microsoft 365 y GLPI

`MetricsSyncService` guarda un snapshot por plataforma cada minuto:

- Citrix.
- Microsoft 365.
- GLPI.

Cada plataforma se sincroniza de forma independiente. Si una falla, las demas siguen guardando snapshots.

También aplica retencion de 90 dias sobre:

- `citrix_metrics_history`
- `microsoft365_metrics_history`
- `glpi_metrics_history`

## Frescura de datos

Los summaries exponen:

- `lastUpdated`
- `dataStatus`

Valores de `dataStatus`:

- `OK`: datos recientes.
- `STALE`: datos antiguos.
- `NO_DATA`: no existen datos.

Criterios actuales:

- Citrix, Microsoft 365 y GLPI: 2 minutos.
- Aruba: 10 minutos.

Los KPIs visuales principales del dashboard no deben aparecer como `GREEN` si alguna fuente esta `STALE` o `NO_DATA`.

## Configuración editable de umbrales y pesos

El panel de configuración permite editar umbrales y pesos persistidos en MySQL. Al arrancar o leer la configuración, el backend valida las claves y valores guardados:

- Si no existe configuración, carga los valores por defecto.
- Si la configuración persistida es completa y valida, se mantiene.
- Si la configuración esta incompleta, contiene valores nulos o no supera las validaciónes, se restaura automaticamente a valores por defecto y se registra un warning.

Cada KPI transversal tiene su propia clave de umbral. `Disponibilidad global` es un KPI de tipo `HEALTH`: un valor alto indica mayor disponibilidad estimada y un valor bajo indica mayor afección sobre la disponibilidad.

## Endpoints

### Endpoints consumidos directamente por React

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

### Endpoints auxiliares disponibles en backend

Estos endpoints están disponibles en el backend para consulta, validación técnica y pruebas, pero no forman parte del flujo directo que React consume en producción.

```http
GET /api/kpis/definitions
GET /api/analysis/technical-degradation-impact
GET /api/analysis/platform-evolution
GET /api/analysis/snapshots
```

### Endpoints administrativos o de sincronización Aruba

Estos endpoints se usan para sincronización manual, diagnóstico o pruebas locales de Aruba Central:

```http
POST /aruba/sync-all
POST /aruba/sync-aps
POST /aruba/sync-switches
POST /aruba/sync-switch-client-usage
GET /aruba/wifi-clients/diagnostics
```

También existen otros endpoints de diagnóstico o sincronización de Aruba que ayudan al soporte y a mantener los datos sincronizados.

## Vistas React

El frontend tiene estas páginas:

- Principal: KPIs transversales.
- Análisis: relación operativa aparente entre GLPI y plataformas técnicas.
- Banco de pruebas: evaluacion manual de escenarios sin persistir datos reales.
- Aruba: APs, clientes WiFi, switches y switches infrautilizados.
- Citrix: KPIs simulados persistidos.
- Microsoft 365: KPIs simulados persistidos.
- GLPI: KPIs simulados persistidos.

Las tarjetas KPI pueden mostrar información explicativa desplegable cuando tienen configurada la prop `info`.

## Módulo de análisis exploratorio

La página `Análisis` se centra en comprobar relaciónes operativas aparentes, no causalidad directa. GLPI se interpreta como consecuencia operativa y Aruba, Citrix y Microsoft 365 como posibles orígenes técnicos.

El panel actual muestra:

- Tabla de relación técnica aparente entre plataformas.
- Evolución temporal conjunta de Aruba, Citrix y Microsoft 365.
- Relaciones específicas entre indicadores concretos de distintas plataformas.

Endpoint consumido por el panel:

```http
GET http://localhost:8080/api/analysis/glpi-platform-relation?period=30d
```

Endpoints auxiliares de consulta:

```http
GET http://localhost:8080/api/analysis/technical-degradation-impact?period=30d
GET http://localhost:8080/api/analysis/platform-evolution?period=30d
GET http://localhost:8080/api/analysis/snapshots?period=30d
```

El panel actual usa exclusivamente los endpoints bajo `/api/analysis`.

El endpoint agregado `/api/analysis/glpi-platform-relation` devuelve los bloques que pinta React en el panel actual: `technicalRelations`, `technicalTimeline` y `specificKpiRelations`.

Los snapshots de análisis se guardan en:

```text
analysis_snapshots
```

## Diagnóstico operativo

El dashboard principal muestra una tarjeta superior de diagnóstico operativo alimentada por:

```http
GET http://localhost:8080/api/dashboard/executive-summary
```

Este endpoint devuelve una lectura ejecutiva con estado global, servicios afectados, plataforma principal, origen probable, impacto, prioridad, primera accion recomendada y tendencia.

## Banco de pruebas

El Banco de pruebas permite introducir valores manuales para Aruba, Citrix,
Microsoft 365 y GLPI y evaluar el escenario sin guardar datos en base de datos.

Los tickets abiertos totales de GLPI no se envian manualmente: se calculan como
suma de tickets abiertos Aruba, tickets abiertos Citrix y tickets abiertos
Microsoft 365. El resultado usa la misma construccion de KPIs transversales y
el mismo resumen operativo visual que el dashboard principal. La tendencia se
muestra como no disponible porque el escenario manual no tiene histórico real.

## Definiciones de KPIs

Las definiciones documentales de KPIs se exponen en:

```http
GET http://localhost:8080/api/kpis/definitions
```

Devuelve una lista de KPIs con identificador, nombre, tipo, plataforma, descripcion, formula, umbrales y fuentes. Este endpoint no calcula valores actuales; sirve para explicar los indicadores usados por el dashboard.

## KPIs principales

### Dashboard principal

- Estado global.
- Criticidad global.
- Disponibilidad global.
- Presión operativa.
- Degradación técnica.
- Riesgo SLA.
- Backlog operativo.
- Impacto en usuarios.
- Servicios afectados.

### Aruba

- Total APs.
- APs activos.
- APs caidos.
- Firmware pendiente.
- APs inactivos.
- Clientes WiFi por grupo.
- Clientes `MUTUALIA-WIFI` por red.
- Total switches.
- Switches apagados.
- Switches con upgrade.
- Switches infrautilizados.

Los switches infrautilizados son los que han estado `Up` y con mas de 17 interfaces en `down` durante los ultimos 30 dias disponibles.

### Citrix

- Sesiones activas.
- Licencias activas.
- Delivery Controllers disponibles.
- Sesiones desconectadas.
- Average Logon Duration.
- Carga de servidores.
- Errores de inicio.
- Indice salud Citrix.

### Microsoft 365

- Usuarios activos.
- Licencias no asignadas.
- Estado Outlook, Teams y SharePoint.
- Buzones casi llenos.
- Emails en cuarentena.
- Almacenamiento SharePoint.
- Riesgos de identidad, MFA, aplicaciones y dispositivos.
- Indice salud Microsoft 365.

### GLPI

- Tickets abiertos.
- Tickets críticos abiertos.
- Tickets vencidos SLA.
- Tiempo medio de resolucion.
- Backlog operativo.
- Tickets creados/cerrados hoy.
- Tickets creados/cerrados semana.

## Comprobaciones MySQL

```sql
SELECT * FROM dashboard.access_points;
SELECT * FROM dashboard.aruba_switches;
SELECT * FROM dashboard.aruba_dashboard_metrics;
SELECT * FROM dashboard.aruba_switch_client_usage;
SELECT * FROM dashboard.aruba_switch_interface_usage_history ORDER BY observed_at DESC;

SELECT * FROM dashboard.citrix_metrics_history ORDER BY collected_at DESC;
SELECT * FROM dashboard.microsoft365_metrics_history ORDER BY collected_at DESC;
SELECT * FROM dashboard.glpi_metrics_history ORDER BY collected_at DESC;
```

## Verificacion

Backend:

```powershell
cd dashboard
mvn clean test
mvn clean install
```

Frontend:

```powershell
cd frontend
npm install
npm run build
```

Comprobaciones útiles:

```powershell
curl.exe http://localhost:8080/dashboard/summary
curl.exe http://localhost:8080/api/dashboard/executive-summary
curl.exe http://localhost:8080/api/kpis/definitions
curl.exe "http://localhost:8080/api/analysis/glpi-platform-relation?period=30d"
curl.exe http://localhost:8080/aruba/summary
curl.exe http://localhost:8080/citrix/summary
curl.exe http://localhost:8080/microsoft365/summary
curl.exe http://localhost:8080/glpi/summary
```

## Notas para repositorio

No subir artefactos generados ni dependencias descargadas:

- `node_modules/`
- `target/`
- `dist/`
- `build/`
- `.env`
- `*.log`
- `*.tmp`
- `*.zip`
- `.git/` dentro del ZIP de entrega
