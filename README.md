# Cuadro de Mandos TFG

Dashboard multiproveedor para monitorizar Aruba, Citrix, Microsoft 365 y GLPI.

El proyecto combina un backend Spring Boot, una base de datos MySQL y un frontend React/Vite. Aruba se integra con datos reales de Aruba Central. Citrix, Microsoft 365 y GLPI usan datos simulados dinamicos que se guardan como snapshots en MySQL para alimentar historicos y KPIs transversales.

## Arquitectura

- `dashboard`: backend Spring Boot.
- `frontend`: interfaz React + Vite.
- MySQL: persistencia de inventario, snapshots e historicos.
- Aruba Central: fuente real para APs, switches, firmware, clientes WiFi y puertos.
- Citrix, Microsoft 365 y GLPI: datos simulados dinamicos persistidos cada minuto.

## Tablas principales

- `access_points`: APs Aruba, una fila por numero de serie.
- `aruba_switches`: switches Aruba, una fila por numero de serie.
- `aruba_dashboard_metrics`: KPIs agregados Aruba, como firmware y clientes WiFi.
- `aruba_switch_client_usage`: ultimo recuento de interfaces down por switch.
- `aruba_switch_interface_usage_history`: historico de interfaces down por switch.
- `citrix_metrics_history`: snapshots historicos Citrix.
- `microsoft365_metrics_history`: snapshots historicos Microsoft 365.
- `glpi_metrics_history`: snapshots historicos GLPI.
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

## Configuracion relevante

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
- Inserta historico de interfaces down.

`GET /aruba/summary` no consulta Aruba directamente; lee datos ya sincronizados desde MySQL.

### Citrix, Microsoft 365 y GLPI

`MetricsSyncService` guarda un snapshot por plataforma cada minuto:

- Citrix.
- Microsoft 365.
- GLPI.

Cada plataforma se sincroniza de forma independiente. Si una falla, las demas siguen guardando snapshots.

Tambien aplica retencion de 90 dias sobre:

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

El dashboard principal no debe aparecer como `GREEN` si alguna fuente esta `STALE` o `NO_DATA`.

## Endpoints principales

Dashboard principal:

```http
GET http://localhost:8080/dashboard/summary
```

Aruba:

```http
GET  http://localhost:8080/aruba/summary
GET  http://localhost:8080/aruba/aps
GET  http://localhost:8080/aruba/stored-aps
GET  http://localhost:8080/aruba/switches
GET  http://localhost:8080/aruba/stored-switches
GET  http://localhost:8080/aruba/switch-client-usage
GET  http://localhost:8080/aruba/wifi-clients
GET  http://localhost:8080/aruba/wifi-clients/diagnostics
POST http://localhost:8080/aruba/sync-aps
POST http://localhost:8080/aruba/sync-switches
POST http://localhost:8080/aruba/sync-switch-client-usage
POST http://localhost:8080/aruba/sync-all
```

Citrix, Microsoft 365 y GLPI:

```http
GET http://localhost:8080/citrix/summary
GET http://localhost:8080/microsoft365/summary
GET http://localhost:8080/glpi/summary
```

## Vistas React

El frontend tiene estas paginas:

- Principal: KPIs transversales.
- Analisis: comparacion exploratoria entre KPIs transversales.
- Aruba: APs, clientes WiFi, switches y switches infrautilizados.
- Citrix: KPIs simulados persistidos.
- Microsoft 365: KPIs simulados persistidos.
- GLPI: KPIs simulados persistidos.

Las tarjetas KPI pueden mostrar informacion explicativa desplegable cuando tienen configurada la prop `info`.

## Modulo de analisis exploratorio

La pagina `Análisis` permite seleccionar un KPI transversal como eje X y despues un KPI relacionado como eje Y. Los KPIs no relacionados se opacan y no se pueden seleccionar. Cuando hay dos KPIs seleccionados, se muestra una grafica de dispersion, correlacion de Pearson e interpretacion automatica.

Endpoints:

```http
GET http://localhost:8080/api/analytics/transversal-kpis
GET http://localhost:8080/api/analytics/compare?kpiX=global_criticality&kpiY=global_health&period=30d
```

Los snapshots transversales se guardan en:

```text
transversal_kpi_history
```

## KPIs principales

### Dashboard principal

- Estado global.
- Riesgo operativo global.
- Servicios con alerta.
- Actividad agregada observada.
- Elementos que requieren accion.
- Tickets criticos abiertos.
- Riesgos de seguridad.
- Presion de capacidad.

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
- Riesgo operativo Microsoft.

### GLPI

- Tickets abiertos.
- Tickets criticos abiertos.
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

Comprobaciones utiles:

```powershell
curl.exe http://localhost:8080/dashboard/summary
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
